package org.adhuc.library.catalog.adapter.rest.catalog;

import net.datafaker.Faker;
import org.adhuc.library.catalog.adapter.rest.RestAdapterTestConfiguration;
import org.adhuc.library.catalog.adapter.rest.authors.AuthorModelAssembler;
import org.adhuc.library.catalog.adapter.rest.books.BookModelAssembler;
import org.adhuc.library.catalog.authors.Author;
import org.adhuc.library.catalog.books.Book;
import org.adhuc.library.catalog.books.BooksMother.Books;
import org.adhuc.library.catalog.books.CatalogService;
import org.assertj.core.api.SoftAssertions;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
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
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Locale.FRENCH;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;
import static org.adhuc.library.catalog.adapter.rest.authors.AuthorsAssertions.assertResponseContainsAllEmbeddedAuthors;
import static org.adhuc.library.catalog.books.BooksMother.books;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings({"preview", "NotNullFieldNotInitialized"})
@Tag("integration")
@Tag("restApi")
@WebMvcTest(controllers = {CatalogController.class, BookModelAssembler.class, AuthorModelAssembler.class})
@Import(RestAdapterTestConfiguration.class)
@DisplayName("Catalog controller should")
class CatalogControllerTests {

    private static final Faker FAKER = new Faker();

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private CatalogService catalogService;
    private final ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.captor();

    @Test
    @DisplayName("provide an empty page when no edition can be found in the catalog for the default language")
    void emptyDefaultPageDefaultLanguage() throws Exception {
        var request = PageRequest.of(0, 50);
        when(catalogService.getPage(any(), any())).thenReturn(Page.empty(request));

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
                .andExpect(jsonPath("_embedded").doesNotExist())
                .andExpect(header().string("Content-Language", "fr"));

        verify(catalogService).getPage(pageableCaptor.capture(), eq(FRENCH));
        var actual = pageableCaptor.getValue();
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(actual.getPageNumber()).isZero();
            s.assertThat(actual.getPageSize()).isEqualTo(50);
        });
    }

    @ParameterizedTest
    @CsvSource({"fr", "en"})
    @DisplayName("provide an empty page when no edition can be found in the catalog for the accept language")
    void emptyDefaultPageAcceptLanguage(String language) throws Exception {
        var request = PageRequest.of(0, 50);
        when(catalogService.getPage(any(), any())).thenReturn(Page.empty(request));

        mvc.perform(get("/api/v1/catalog")
                        .accept("application/hal+json")
                        .header(ACCEPT_LANGUAGE, language)
                )
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
                .andExpect(jsonPath("_embedded").doesNotExist())
                .andExpect(header().string("Content-Language", language));

        verify(catalogService).getPage(pageableCaptor.capture(), eq(Locale.of(language)));
        var actual = pageableCaptor.getValue();
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(actual.getPageNumber()).isZero();
            s.assertThat(actual.getPageSize()).isEqualTo(50);
        });
    }

    @ParameterizedTest
    @MethodSource({"uniqueDefaultPageProvider", "uniqueDefaultPageWithOtherLanguageProvider"})
    @DisplayName("provide a unique page when the number of books in the catalog is lower than or equal to the requested page size")
    void uniqueDefaultPage(int numberOfElements, String language, List<Book> books) throws Exception {
        var request = PageRequest.of(0, 50);
        when(catalogService.getPage(any(), any())).thenReturn(new PageImpl<>(books, request, numberOfElements));

        var result = mvc.perform(get("/api/v1/catalog")
                        .accept("application/hal+json")
                        .header(ACCEPT_LANGUAGE, language)
                )
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
                .andExpect(jsonPath("_embedded").exists())
                .andExpect(header().string("Content-Language", language));

        assertResponseContainsAllBooks(result, language, books);
        assertResponseContainsAllBooksAuthors(result, books);

        verify(catalogService).getPage(pageableCaptor.capture(), eq(Locale.of(language)));
        var actual = pageableCaptor.getValue();
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(actual.getPageNumber()).isZero();
            s.assertThat(actual.getPageSize()).isEqualTo(50);
        });
    }

    private static Stream<Arguments> uniqueDefaultPageProvider() {
        var numberOfElements = FAKER.random().nextInt(1, 50);
        var originalLanguage = Books.language();
        var otherLanguages = Books.otherLanguages(originalLanguage);
        var books = books(numberOfElements, originalLanguage, otherLanguages);
        return Stream.of(Arguments.of(numberOfElements, originalLanguage, books));
    }

    private static Stream<Arguments> uniqueDefaultPageWithOtherLanguageProvider() {
        var numberOfElements = FAKER.random().nextInt(1, 50);
        var originalLanguage = Books.language();
        var otherLanguages = Books.otherLanguages(originalLanguage);
        var books = books(numberOfElements, originalLanguage, otherLanguages);
        return Stream.of(Arguments.of(numberOfElements, otherLanguages.getFirst(), books));
    }

    @Test
    @DisplayName("provide a page with default page size when not specified explicitly")
    void defaultPageSize() throws Exception {
        var pageIndex = FAKER.random().nextInt(1, 100);
        var originalLanguage = Books.language();
        var books = pageSample(pageIndex, 50, originalLanguage, Books.otherLanguages(originalLanguage));

        when(catalogService.getPage(any(), any())).thenReturn(books);

        var result = mvc.perform(get("/api/v1/catalog")
                        .accept("application/hal+json")
                        .header(ACCEPT_LANGUAGE, originalLanguage)
                        .queryParam("page", String.valueOf(books.getNumber())))
                .andExpect(status().isPartialContent())
                .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                .andExpect(jsonPath("page.size", equalTo(books.getSize())))
                .andExpect(jsonPath("page.total_elements", equalTo(Long.valueOf(books.getTotalElements()).intValue())))
                .andExpect(jsonPath("page.total_pages", equalTo(books.getTotalPages())))
                .andExpect(jsonPath("page.number", equalTo(books.getNumber())))
                .andExpect(jsonPath("_links.self.href", equalTo(STR."http://localhost/api/v1/catalog?page=\{books.getNumber()}&size=50")))
                .andExpect(jsonPath("_embedded").exists())
                .andExpect(header().string("Content-Language", originalLanguage));

        assertResponseContainsAllBooks(result, originalLanguage, books.toList());
        assertResponseContainsAllBooksAuthors(result, books.toList());

        verify(catalogService).getPage(pageableCaptor.capture(), eq(Locale.of(originalLanguage)));
        var actual = pageableCaptor.getValue();
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(actual.getPageNumber()).isEqualTo(books.getNumber());
            s.assertThat(actual.getPageSize()).isEqualTo(50);
        });
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
    @DisplayName("refuse providing a page with invalid page number & page size")
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

    @Test
    @DisplayName("provide a page corresponding to requested page and size")
    void getPage() throws Exception {
        var pageIndex = FAKER.random().nextInt(0, 100);
        var pageSize = FAKER.random().nextInt(2, 100);
        var originalLanguage = Books.language();
        var books = pageSample(pageIndex, pageSize, originalLanguage, Books.otherLanguages(originalLanguage));

        when(catalogService.getPage(any(), any())).thenReturn(books);

        var result = mvc.perform(get("/api/v1/catalog")
                        .accept("application/hal+json")
                        .header(ACCEPT_LANGUAGE, originalLanguage)
                        .queryParam("page", Integer.toString(pageIndex))
                        .queryParam("size", Integer.toString(pageSize))
                ).andExpect(status().isPartialContent())
                .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                .andExpect(jsonPath("page.size", equalTo(books.getSize())))
                .andExpect(jsonPath("page.total_elements", equalTo(Long.valueOf(books.getTotalElements()).intValue())))
                .andExpect(jsonPath("page.total_pages", equalTo(books.getTotalPages())))
                .andExpect(jsonPath("page.number", equalTo(pageIndex)))
                .andExpect(jsonPath("_links.self.href", equalTo(STR."http://localhost/api/v1/catalog?page=\{pageIndex}&size=\{pageSize}")))
                .andExpect(jsonPath("_embedded").exists())
                .andExpect(header().string("Content-Language", originalLanguage));

        assertResponseContainsAllBooks(result, originalLanguage, books.toList());
        assertResponseContainsAllBooksAuthors(result, books.toList());

        verify(catalogService).getPage(pageableCaptor.capture(), eq(Locale.of(originalLanguage)));
        var actual = pageableCaptor.getValue();
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(actual.getPageNumber()).isEqualTo(books.getNumber());
            s.assertThat(actual.getPageSize()).isEqualTo(books.getSize());
        });
    }

    @Test
    @DisplayName("provide a page corresponding to requested page and size in default language")
    void getPageDefaultLanguage() throws Exception {
        var pageIndex = FAKER.random().nextInt(0, 100);
        var pageSize = FAKER.random().nextInt(2, 100);
        var books = pageSample(pageIndex, pageSize, "fr", Books.otherLanguages("fr"));

        when(catalogService.getPage(any(), any())).thenReturn(books);

        var result = mvc.perform(get("/api/v1/catalog")
                        .accept("application/hal+json")
                        .queryParam("page", Integer.toString(pageIndex))
                        .queryParam("size", Integer.toString(pageSize))
                ).andExpect(status().isPartialContent())
                .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                .andExpect(jsonPath("page.size", equalTo(books.getSize())))
                .andExpect(jsonPath("page.total_elements", equalTo(Long.valueOf(books.getTotalElements()).intValue())))
                .andExpect(jsonPath("page.total_pages", equalTo(books.getTotalPages())))
                .andExpect(jsonPath("page.number", equalTo(pageIndex)))
                .andExpect(jsonPath("_links.self.href", equalTo(STR."http://localhost/api/v1/catalog?page=\{pageIndex}&size=\{pageSize}")))
                .andExpect(jsonPath("_embedded").exists())
                .andExpect(header().string("Content-Language", "fr"));

        assertResponseContainsAllBooks(result, "fr", books.toList());
        assertResponseContainsAllBooksAuthors(result, books.toList());

        verify(catalogService).getPage(pageableCaptor.capture(), eq(FRENCH));
        var actual = pageableCaptor.getValue();
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(actual.getPageNumber()).isEqualTo(books.getNumber());
            s.assertThat(actual.getPageSize()).isEqualTo(books.getSize());
        });
    }

    @ParameterizedTest(name = "[{index}] Page = {0}, size = {1}, first = {3}, prev = {4}, next = {5}, last = {6}")
    @MethodSource({"uniquePagesProvider", "firstPagesProvider", "intermediatePagesProvider", "lastPagesProvider"})
    @DisplayName("provide a page with navigation links")
    void getPageWithNavigation(int pageIndex, int pageSize, String language, Page<Book> books, boolean hasFirst,
                               boolean hasPrev, boolean hasNext, boolean hasLast) throws Exception {
        when(catalogService.getPage(any(), any())).thenReturn(books);

        var result = mvc.perform(get("/api/v1/catalog")
                        .accept("application/hal+json")
                        .header(ACCEPT_LANGUAGE, language)
                        .queryParam("page", Integer.toString(pageIndex))
                        .queryParam("size", Integer.toString(pageSize))
                ).andExpect(status().isPartialContent())
                .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                .andExpect(jsonPath("_links.self.href", equalTo(STR."http://localhost/api/v1/catalog?page=\{pageIndex}&size=\{pageSize}")));

        verifyNavigationLink(result, hasFirst, "first", STR."http://localhost/api/v1/catalog?page=0&size=\{pageSize}");
        verifyNavigationLink(result, hasPrev, "prev", STR."http://localhost/api/v1/catalog?page=\{pageIndex - 1}&size=\{pageSize}");
        verifyNavigationLink(result, hasNext, "next", STR."http://localhost/api/v1/catalog?page=\{pageIndex + 1}&size=\{pageSize}");
        verifyNavigationLink(result, hasLast, "last", STR."http://localhost/api/v1/catalog?page=\{books.getTotalPages() - 1}&size=\{pageSize}");
    }

    static void verifyNavigationLink(ResultActions result, boolean hasLink, String linkName, @Nullable String valueIfExists) throws Exception {
        if (hasLink) {
            result.andExpect(jsonPath(STR."_links.\{linkName}.href", equalTo(requireNonNull(valueIfExists))));
        } else {
            result.andExpect(jsonPath(STR."_links.\{linkName}").doesNotExist());
        }
    }

    static Stream<Arguments> uniquePagesProvider() {
        var requestedPageSize = FAKER.random().nextInt(2, 100);
        var originalLanguage = Books.language();
        var otherLanguages = Books.otherLanguages(originalLanguage);
        var page = lastPage(0, requestedPageSize, originalLanguage, otherLanguages);
        return Stream.of(Arguments.of(0, page.getPageable().getPageSize(), originalLanguage, page, false, false, false, false));
    }

    static Stream<Arguments> firstPagesProvider() {
        var requestedPageSize = FAKER.random().nextInt(2, 100);
        var originalLanguage = Books.language();
        var otherLanguages = Books.otherLanguages(originalLanguage);
        var page = fullPage(0, requestedPageSize, originalLanguage, otherLanguages);
        return Stream.of(Arguments.of(0, page.getPageable().getPageSize(), originalLanguage, page, true, false, true, true));
    }

    static Stream<Arguments> intermediatePagesProvider() {
        var pageIndex = FAKER.random().nextInt(1, 100);
        var requestedPageSize = FAKER.random().nextInt(2, 100);
        var originalLanguage = Books.language();
        var otherLanguages = Books.otherLanguages(originalLanguage);
        var page = fullPage(pageIndex, requestedPageSize, originalLanguage, otherLanguages);
        return Stream.of(Arguments.of(page.getNumber(), page.getPageable().getPageSize(), originalLanguage, page, true, true, true, true));
    }

    static Stream<Arguments> lastPagesProvider() {
        var pageIndex = FAKER.random().nextInt(1, 100);
        var requestedPageSize = FAKER.random().nextInt(2, 100);
        var originalLanguage = Books.language();
        var otherLanguages = Books.otherLanguages(originalLanguage);
        var page = lastPage(pageIndex, requestedPageSize, originalLanguage, otherLanguages);
        return Stream.of(Arguments.of(page.getNumber(), page.getPageable().getPageSize(), originalLanguage, page, true, true, false, true));
    }

    private static Page<Book> pageSample(int pageIndex, int requestedPageSize, String language, Collection<String> otherLanguages) {
        var isLastPage = FAKER.bool().bool();
        if (isLastPage) {
            return lastPage(pageIndex, requestedPageSize, language, otherLanguages);
        }
        return fullPage(pageIndex, requestedPageSize, language, otherLanguages);
    }

    private static PageImpl<Book> lastPage(int page, int requestedPageSize, String language, Collection<String> otherLanguages) {
        int pageSize = FAKER.random().nextInt(1, requestedPageSize);
        int totalRows = pageSize;
        if (page > 0) {
            totalRows += requestedPageSize * page;
        }
        var elements = books(pageSize, language, otherLanguages);
        return new PageImpl<>(elements, PageRequest.of(page, requestedPageSize), totalRows);
    }

    private static PageImpl<Book> fullPage(int page, int requestedPageSize, String language, Collection<String> otherLanguages) {
        var totalPages = FAKER.random().nextInt(page + 1, 150);
        var lastPageSize = FAKER.random().nextInt(1, requestedPageSize - 1);
        var totalRows = totalPages * requestedPageSize + lastPageSize;
        var elements = books(requestedPageSize, language, otherLanguages);
        return new PageImpl<>(elements, PageRequest.of(page, requestedPageSize), totalRows);
    }

    private void assertResponseContainsAllBooks(ResultActions result, String language, List<Book> expected) throws Exception {
        result.andExpect(jsonPath("_embedded.books").exists())
                .andExpect(jsonPath("_embedded.books").isArray())
                .andExpect(jsonPath("_embedded.books", hasSize(expected.size())))
                .andExpect(jsonPath("_embedded.books").exists())
                .andExpect(jsonPath("_embedded.books").isArray())
                .andExpect(jsonPath("_embedded.books", hasSize(expected.size())));
        for (int i = 0; i < expected.size(); i++) {
            assertResponseContainsBook(result, "_embedded.books[" + i + "].", language, expected.get(i));
        }
    }

    private void assertResponseContainsBook(ResultActions result, String jsonPrefix, String language, Book expected) throws Exception {
        result.andExpect(jsonPath(jsonPrefix + "id", equalTo(expected.id().toString())))
                .andExpect(jsonPath(jsonPrefix + "title", equalTo(expected.titleIn(language))))
                .andExpect(jsonPath(jsonPrefix + "authors", containsInAnyOrder(
                        expected.authors().stream().map(Author::id).map(UUID::toString).toArray())))
                .andExpect(jsonPath(jsonPrefix + "description", equalTo(expected.descriptionIn(language))))
                .andExpect(jsonPath(jsonPrefix + "_links.self.href", equalTo("http://localhost/api/v1/books/" + expected.id())));
    }

    private void assertResponseContainsAllBooksAuthors(ResultActions result, Collection<Book> expectedBooks) throws Exception {
        var expectedAuthors = expectedBooks.stream().map(Book::authors).flatMap(Collection::stream).collect(toSet());
        assertResponseContainsAllEmbeddedAuthors(result, expectedAuthors);
    }

}
