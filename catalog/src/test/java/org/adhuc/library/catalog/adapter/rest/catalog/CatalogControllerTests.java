package org.adhuc.library.catalog.adapter.rest.catalog;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Combinators;
import org.adhuc.library.catalog.adapter.rest.PaginationSerializationConfiguration;
import org.adhuc.library.catalog.adapter.rest.authors.AuthorModelAssembler;
import org.adhuc.library.catalog.adapter.rest.editions.EditionModelAssembler;
import org.adhuc.library.catalog.adapter.rest.support.validation.openapi.RequestValidationConfiguration;
import org.adhuc.library.catalog.authors.Author;
import org.adhuc.library.catalog.books.Book;
import org.adhuc.library.catalog.editions.CatalogService;
import org.adhuc.library.catalog.editions.Edition;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.stream.Collectors.toSet;
import static net.jqwik.api.Arbitraries.integers;
import static org.adhuc.library.catalog.adapter.rest.authors.AuthorsAssertions.assertResponseContainsAllEmbeddedAuthors;
import static org.adhuc.library.catalog.editions.EditionsMother.editions;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("unused")
@WebMvcTest(controllers = {CatalogController.class, EditionModelAssembler.class, AuthorModelAssembler.class})
@Import({RequestValidationConfiguration.class, PaginationSerializationConfiguration.class})
@DisplayName("Catalog controller should")
class CatalogControllerTests {

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private CatalogService catalogService;
    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    @Test
    @DisplayName("provide an empty page when no edition can be found in the catalog")
    void emptyDefaultPage() throws Exception {
        var request = PageRequest.of(0, 50);
        when(catalogService.getPage(any())).thenReturn(Page.empty(request));

        mvc.perform(get("/api/v1/catalog").accept("application/hal+json"))
                .andExpect(status().isPartialContent())
                .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                .andExpect(jsonPath("page.size", equalTo(50)))
                .andExpect(jsonPath("page.total_elements", equalTo(0)))
                .andExpect(jsonPath("page.total_pages", equalTo(0)))
                .andExpect(jsonPath("page.number", equalTo(0)))
                .andExpect(jsonPath("_links.self.href", equalTo("http://localhost/api/v1/catalog?page=0&size=50")))
                .andExpect(jsonPath("_links.first").doesNotExist())
                .andExpect(jsonPath("_links.prev").doesNotExist())
                .andExpect(jsonPath("_links.next").doesNotExist())
                .andExpect(jsonPath("_links.last").doesNotExist())
                .andExpect(jsonPath("_embedded").doesNotExist());

        verify(catalogService).getPage(pageableCaptor.capture());
        var actual = pageableCaptor.getValue();
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(actual.getPageNumber()).isZero();
            s.assertThat(actual.getPageSize()).isEqualTo(50);
        });
    }

    @ParameterizedTest
    @MethodSource("uniqueDefaultPageProvider")
    @DisplayName("provide a unique page when the number of editions in the catalog is lower than or equal to the requested page size")
    void uniqueDefaultPage(int numberOfElements, List<Edition> editions) throws Exception {
        var request = PageRequest.of(0, 50);
        when(catalogService.getPage(any())).thenReturn(new PageImpl<>(editions, request, numberOfElements));

        var result = mvc.perform(get("/api/v1/catalog").accept("application/hal+json"))
                .andExpect(status().isPartialContent())
                .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                .andExpect(jsonPath("page.size", equalTo(50)))
                .andExpect(jsonPath("page.total_elements", equalTo(numberOfElements)))
                .andExpect(jsonPath("page.total_pages", equalTo(1)))
                .andExpect(jsonPath("page.number", equalTo(0)))
                .andExpect(jsonPath("_links.self.href", equalTo("http://localhost/api/v1/catalog?page=0&size=50")))
                .andExpect(jsonPath("_links.first.href").doesNotExist())
                .andExpect(jsonPath("_links.prev").doesNotExist())
                .andExpect(jsonPath("_links.next").doesNotExist())
                .andExpect(jsonPath("_links.last.href").doesNotExist())
                .andExpect(jsonPath("_embedded").exists());

        assertResponseContainsAllEditions(result, editions);
        assertResponseContainsAllEditionsAuthors(result, editions);

        verify(catalogService).getPage(pageableCaptor.capture());
        var actual = pageableCaptor.getValue();
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(actual.getPageNumber()).isZero();
            s.assertThat(actual.getPageSize()).isEqualTo(50);
        });
    }

    private static Stream<Arguments> uniqueDefaultPageProvider() {
        return integers().between(1, 50)
                .flatMap(numberOfElements -> editions().list().ofSize(numberOfElements)
                        .map(editions -> Arguments.of(numberOfElements, editions)))
                .sampleStream().limit(5);
    }

    @ParameterizedTest
    @MethodSource("defaultPageSizeProvider")
    @DisplayName("provide a page with default page size when not specified explicitly")
    void defaultPageSize(Page<Edition> editions) throws Exception {
        when(catalogService.getPage(any())).thenReturn(editions);

        var result = mvc.perform(get("/api/v1/catalog").accept("application/hal+json")
                        .queryParam("page", String.valueOf(editions.getNumber())))
                .andExpect(status().isPartialContent())
                .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                .andExpect(jsonPath("page.size", equalTo(editions.getSize())))
                .andExpect(jsonPath("page.total_elements", equalTo(Long.valueOf(editions.getTotalElements()).intValue())))
                .andExpect(jsonPath("page.total_pages", equalTo(editions.getTotalPages())))
                .andExpect(jsonPath("page.number", equalTo(editions.getNumber())))
                .andExpect(jsonPath("_links.self.href", equalTo(STR."http://localhost/api/v1/catalog?page=\{editions.getNumber()}&size=50")))
                .andExpect(jsonPath("_embedded").exists());

        assertResponseContainsAllEditions(result, editions.toList());
        assertResponseContainsAllEditionsAuthors(result, editions.toList());

        verify(catalogService).getPage(pageableCaptor.capture());
        var actual = pageableCaptor.getValue();
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(actual.getPageNumber()).isEqualTo(editions.getNumber());
            s.assertThat(actual.getPageSize()).isEqualTo(50);
        });
    }

    private static Stream<Arguments> defaultPageSizeProvider() {
        return integers().between(0, 100)
                .map(pageIndex -> pageSample(pageIndex, 50))
                .map(Arguments::of)
                .sampleStream().limit(5);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-50", "-2", "-1"})
    @DisplayName("refuse providing a page with a negative page number")
    void getInvalidPageNumber(String pageNumber) throws Exception {
        mvc.perform(get("/api/v1/catalog").accept("application/hal+json")
                        .queryParam("page", pageNumber)
                ).andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("type", equalTo("/problems/invalid-request")))
                .andExpect(jsonPath("status", equalTo(400)))
                .andExpect(jsonPath("title", equalTo("Request validation error")))
                .andExpect(jsonPath("detail", equalTo("Request parameters or body are invalid compared to the OpenAPI specification. See errors for more information")))
                .andExpect(jsonPath("errors").isArray())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0].detail", equalTo(STR."Numeric instance is lower than the required minimum (minimum: 0, found: \{pageNumber})")))
                .andExpect(jsonPath("errors[0].parameter", equalTo("page")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-50", "-1", "0"})
    @DisplayName("refuse providing a page with a negative page size")
    void getInvalidPageSize(String pageSize) throws Exception {
        mvc.perform(get("/api/v1/catalog").accept("application/hal+json")
                        .queryParam("size", pageSize)
                ).andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("type", equalTo("/problems/invalid-request")))
                .andExpect(jsonPath("status", equalTo(400)))
                .andExpect(jsonPath("title", equalTo("Request validation error")))
                .andExpect(jsonPath("detail", equalTo("Request parameters or body are invalid compared to the OpenAPI specification. See errors for more information")))
                .andExpect(jsonPath("errors").isArray())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0].detail",
                        equalTo(STR."Numeric instance is lower than the required minimum (minimum: 1, found: \{pageSize})")))
                .andExpect(jsonPath("errors[0].parameter", equalTo("size")));
    }

    @ParameterizedTest
    @CsvSource({"-50,-1", "-2,-50", "-1,0"})
    @DisplayName("refuse providing a page with a negative page number")
    void getInvalidPageNumberAndSize(String pageNumber, String pageSize) throws Exception {
        mvc.perform(get("/api/v1/catalog").accept("application/hal+json")
                        .queryParam("page", pageNumber)
                        .queryParam("size", pageSize)
                ).andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("type", equalTo("/problems/invalid-request")))
                .andExpect(jsonPath("status", equalTo(400)))
                .andExpect(jsonPath("title", equalTo("Request validation error")))
                .andExpect(jsonPath("detail", equalTo("Request parameters or body are invalid compared to the OpenAPI specification. See errors for more information")))
                .andExpect(jsonPath("errors").isArray())
                .andExpect(jsonPath("errors", hasSize(2)))
                .andExpect(jsonPath("errors[0].detail",
                        equalTo(STR."Numeric instance is lower than the required minimum (minimum: 0, found: \{pageNumber})")))
                .andExpect(jsonPath("errors[0].parameter", equalTo("page")))
                .andExpect(jsonPath("errors[1].detail",
                        equalTo(STR."Numeric instance is lower than the required minimum (minimum: 1, found: \{pageSize})")))
                .andExpect(jsonPath("errors[1].parameter", equalTo("size")));
    }

    @ParameterizedTest
    @MethodSource("pageProvider")
    @DisplayName("provide a page corresponding to requested page and size")
    void getPage(int pageNumber, int pageSize, Page<Edition> editions) throws Exception {
        when(catalogService.getPage(any())).thenReturn(editions);

        var result = mvc.perform(get("/api/v1/catalog").accept("application/hal+json")
                        .queryParam("page", Integer.toString(pageNumber))
                        .queryParam("size", Integer.toString(pageSize))
                ).andExpect(status().isPartialContent())
                .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                .andExpect(jsonPath("page.size", equalTo(editions.getSize())))
                .andExpect(jsonPath("page.total_elements", equalTo(Long.valueOf(editions.getTotalElements()).intValue())))
                .andExpect(jsonPath("page.total_pages", equalTo(editions.getTotalPages())))
                .andExpect(jsonPath("page.number", equalTo(pageNumber)))
                .andExpect(jsonPath("_links.self.href", equalTo(STR."http://localhost/api/v1/catalog?page=\{pageNumber}&size=\{pageSize}")))
                .andExpect(jsonPath("_embedded").exists());

        assertResponseContainsAllEditions(result, editions.toList());
        assertResponseContainsAllEditionsAuthors(result, editions.toList());

        verify(catalogService).getPage(pageableCaptor.capture());
        var actual = pageableCaptor.getValue();
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(actual.getPageNumber()).isEqualTo(editions.getNumber());
            s.assertThat(actual.getPageSize()).isEqualTo(editions.getSize());
        });
    }

    private static Stream<Arguments> pageProvider() {
        return Combinators.combine(integers().between(0, 100), integers().between(2, 100))
                .as((pageNumber, pageSize) -> Arguments.of(pageNumber, pageSize, pageSample(pageNumber, pageSize)))
                .sampleStream().limit(5);
    }

    @ParameterizedTest(name = "[{index}] Page = {0}, size = {1}, first = {3}, prev = {4}, next = {5}, last = {6}")
    @MethodSource({"uniquePagesProvider", "firstPagesProvider", "intermediatePagesProvider", "lastPagesProvider"})
    @DisplayName("provide a page with navigation links")
    void getPageWithNavigation(int pageNumber, int pageSize, Page<Edition> editions, boolean hasFirst,
                               boolean hasPrev, boolean hasNext, boolean hasLast) throws Exception {
        when(catalogService.getPage(any())).thenReturn(editions);

        var result = mvc.perform(get("/api/v1/catalog").accept("application/hal+json")
                        .queryParam("page", Integer.toString(pageNumber))
                        .queryParam("size", Integer.toString(pageSize)))
                .andExpect(status().isPartialContent())
                .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                .andExpect(jsonPath("_links.self.href", equalTo(STR."http://localhost/api/v1/catalog?page=\{pageNumber}&size=\{pageSize}")));

        verifyNavigationLink(result, hasFirst, "first", STR."http://localhost/api/v1/catalog?page=0&size=\{pageSize}");
        verifyNavigationLink(result, hasPrev, "prev", STR."http://localhost/api/v1/catalog?page=\{pageNumber - 1}&size=\{pageSize}");
        verifyNavigationLink(result, hasNext, "next", STR."http://localhost/api/v1/catalog?page=\{pageNumber + 1}&size=\{pageSize}");
        verifyNavigationLink(result, hasLast, "last", STR."http://localhost/api/v1/catalog?page=\{editions.getTotalPages() - 1}&size=\{pageSize}");
    }

    static void verifyNavigationLink(ResultActions result, boolean hasLink, String linkName, String valueIfExists) throws Exception {
        if (hasLink) {
            result.andExpect(jsonPath(STR."_links.\{linkName}.href", equalTo(valueIfExists)));
        } else {
            result.andExpect(jsonPath(STR."_links.\{linkName}").doesNotExist());
        }
    }

    static Stream<Arguments> uniquePagesProvider() {
        return integers().between(2, 100)
                .map(requestedPageSize -> lastPage(0, requestedPageSize))
                .map(editions -> Arguments.of(0, editions.getPageable().getPageSize(), editions, false, false, false, false))
                .sampleStream().distinct().limit(3);
    }

    static Stream<Arguments> firstPagesProvider() {
        return integers().between(2, 100)
                .map(requestedPageSize -> fullPage(0, requestedPageSize))
                .map(editions -> Arguments.of(0, editions.getPageable().getPageSize(), editions, true, false, true, true))
                .sampleStream().distinct().limit(3);
    }

    static Stream<Arguments> intermediatePagesProvider() {
        return Combinators.combine(integers().between(1, 100), integers().between(2, 100))
                .as(CatalogControllerTests::fullPage)
                .map(editions -> Arguments.of(editions.getNumber(), editions.getPageable().getPageSize(), editions, true, true, true, true))
                .sampleStream().distinct().limit(3);
    }

    static Stream<Arguments> lastPagesProvider() {
        return Combinators.combine(integers().between(1, 100), integers().between(2, 100))
                .as(CatalogControllerTests::lastPage)
                .map(editions -> Arguments.of(editions.getNumber(), editions.getPageable().getPageSize(), editions, true, true, false, true))
                .sampleStream().distinct().limit(3);
    }

    private static Page<Edition> pageSample(int pageIndex, int requestedPageSize) {
        var isLastPage = Arbitraries.of(TRUE, FALSE).sample();
        if (isLastPage) {
            return lastPage(pageIndex, requestedPageSize);
        }
        return fullPage(pageIndex, requestedPageSize);
    }

    private static PageImpl<Edition> lastPage(int page, int requestedPageSize) {
        int pageSize = integers().between(1, requestedPageSize).sample();
        int totalRows = pageSize;
        if (page > 0) {
            totalRows += requestedPageSize * page;
        }
        var elements = editions().list().ofSize(pageSize).sample();
        return new PageImpl<>(elements, PageRequest.of(page, requestedPageSize), totalRows);
    }

    private static PageImpl<Edition> fullPage(int page, int requestedPageSize) {
        var totalPages = integers().between(page + 1, 150).sample();
        var lastPageSize = integers().between(1, requestedPageSize - 1).sample();
        var totalRows = totalPages * requestedPageSize + lastPageSize;
        var elements = editions().list().ofSize(requestedPageSize).sample();
        return new PageImpl<>(elements, PageRequest.of(page, requestedPageSize), totalRows);
    }

    private void assertResponseContainsAllEditions(ResultActions result, List<Edition> expected) throws Exception {
        result.andExpect(jsonPath("_embedded.editions").exists())
                .andExpect(jsonPath("_embedded.editions").isArray())
                .andExpect(jsonPath("_embedded.editions", hasSize(expected.size())))
                .andExpect(jsonPath("_embedded.editions").exists())
                .andExpect(jsonPath("_embedded.editions").isArray())
                .andExpect(jsonPath("_embedded.editions", hasSize(expected.size())));
        for (int i = 0; i < expected.size(); i++) {
            assertResponseContainsEdition(result, "_embedded.editions[" + i + "].", expected.get(i));
            assertResponseContainsEdition(result, "_embedded.editions[" + i + "].", expected.get(i));
        }
    }

    private void assertResponseContainsEdition(ResultActions result, String jsonPrefix, Edition expected) throws Exception {
        result.andExpect(jsonPath(jsonPrefix + "isbn", equalTo(expected.isbn())))
                .andExpect(jsonPath(jsonPrefix + "title", equalTo(expected.title())))
                .andExpect(jsonPath(jsonPrefix + "authors", containsInAnyOrder(
                        expected.book().authors().stream().map(Author::id).map(UUID::toString).toArray())))
                .andExpect(jsonPath(jsonPrefix + "language", equalTo(expected.language())))
                .andExpect(jsonPath(jsonPrefix + "summary", equalTo(expected.summary())))
                .andExpect(jsonPath(jsonPrefix + "_links.self.href", equalTo("http://localhost/api/v1/editions/" + expected.isbn())));
    }

    private void assertResponseContainsAllEditionsAuthors(ResultActions result, Collection<Edition> expectedEditions) throws Exception {
        var expectedAuthors = expectedEditions.stream().map(Edition::book).map(Book::authors).flatMap(Collection::stream).collect(toSet());
        assertResponseContainsAllEmbeddedAuthors(result, expectedAuthors);
    }

}
