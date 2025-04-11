package org.adhuc.library.catalog.adapter.rest.catalog;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Combinators;
import org.adhuc.library.catalog.adapter.rest.PaginationSerializationConfiguration;
import org.adhuc.library.catalog.adapter.rest.authors.AuthorModelAssembler;
import org.adhuc.library.catalog.adapter.rest.books.BookModelAssembler;
import org.adhuc.library.catalog.adapter.rest.editions.EditionModelAssembler;
import org.adhuc.library.catalog.adapter.rest.support.validation.openapi.RequestValidationConfiguration;
import org.adhuc.library.catalog.authors.Author;
import org.adhuc.library.catalog.books.Book;
import org.adhuc.library.catalog.books.CatalogService;
import org.adhuc.library.catalog.editions.Edition;
import org.adhuc.library.catalog.editions.EditionsService;
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

import java.util.*;
import java.util.stream.Stream;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Locale.FRENCH;
import static java.util.stream.Collectors.toSet;
import static net.jqwik.api.Arbitraries.integers;
import static org.adhuc.library.catalog.adapter.rest.authors.AuthorsAssertions.assertResponseContainsAllEmbeddedAuthors;
import static org.adhuc.library.catalog.books.BooksMother.Books.languages;
import static org.adhuc.library.catalog.books.BooksMother.Books.otherLanguages;
import static org.adhuc.library.catalog.books.BooksMother.books;
import static org.adhuc.library.catalog.editions.EditionsMother.editions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("unused")
@WebMvcTest(controllers = {CatalogController.class, BookModelAssembler.class, EditionModelAssembler.class, AuthorModelAssembler.class})
@Import({RequestValidationConfiguration.class, PaginationSerializationConfiguration.class})
@DisplayName("Catalog controller should")
class CatalogControllerTests {

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private CatalogService catalogService;
    @MockitoBean
    private EditionsService editionsService;
    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;
    @Captor
    private ArgumentCaptor<Collection<UUID>> booksCaptor;

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
        verifyNoInteractions(editionsService);
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
        verifyNoInteractions(editionsService);
    }

    @ParameterizedTest
    @MethodSource({"uniqueDefaultPageProvider", "uniqueDefaultPageWithOtherLanguageProvider"})
    @DisplayName("provide a unique page when the number of books in the catalog is lower than or equal to the requested page size")
    void uniqueDefaultPage(int numberOfElements, String language, List<Book> books, List<Edition> editions) throws Exception {
        var request = PageRequest.of(0, 50);
        when(catalogService.getPage(any(), any())).thenReturn(new PageImpl<>(books, request, numberOfElements));
        when(editionsService.getBooksEditions(any())).thenReturn(editions);

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
        assertResponseContainsAllEditions(result, editions);
        assertResponseContainsAllBooksAuthors(result, books);

        verify(catalogService).getPage(pageableCaptor.capture(), eq(Locale.of(language)));
        var actual = pageableCaptor.getValue();
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(actual.getPageNumber()).isZero();
            s.assertThat(actual.getPageSize()).isEqualTo(50);
        });
        verify(editionsService).getBooksEditions(booksCaptor.capture());
        var actualBookIds = booksCaptor.getValue();
        assertThat(actualBookIds).containsExactlyInAnyOrderElementsOf(books.stream().map(Book::id).toList());
    }

    private static Stream<Arguments> uniqueDefaultPageProvider() {
        return Combinators.combine(
                        integers().between(1, 50),
                        languages()
                )
                .flatAs((numberOfElements, language) -> otherLanguages(language).set()
                        .flatMap(otherLanguages -> Combinators.combine(
                                        books(language, otherLanguages).list().ofSize(numberOfElements),
                                        editions().list().ofMinSize(numberOfElements).ofMaxSize(numberOfElements * 2)
                                ).as((books, editions) -> Arguments.of(numberOfElements, language, books, editions))
                        )
                )
                .sampleStream().limit(5);
    }

    private static Stream<Arguments> uniqueDefaultPageWithOtherLanguageProvider() {
        return Combinators.combine(
                        integers().between(1, 50),
                        languages()
                )
                .flatAs((numberOfElements, language) -> otherLanguages(language).list().ofMinSize(1)
                        .flatMap(otherLanguages -> Combinators.combine(
                                        books(language, Set.copyOf(otherLanguages)).list().ofSize(numberOfElements),
                                        editions().list().ofMinSize(numberOfElements).ofMaxSize(numberOfElements * 2)
                                ).as((books, editions) -> Arguments.of(numberOfElements, otherLanguages.getFirst(), books, editions))
                        )
                )
                .sampleStream().limit(5);
    }

    @ParameterizedTest
    @MethodSource("defaultPageSizeProvider")
    @DisplayName("provide a page with default page size when not specified explicitly")
    void defaultPageSize(String language, Page<Book> books, List<Edition> editions) throws Exception {
        when(catalogService.getPage(any(), any())).thenReturn(books);
        when(editionsService.getBooksEditions(any())).thenReturn(editions);

        var result = mvc.perform(get("/api/v1/catalog")
                        .accept("application/hal+json")
                        .header(ACCEPT_LANGUAGE, language)
                        .queryParam("page", String.valueOf(books.getNumber())))
                .andExpect(status().isPartialContent())
                .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                .andExpect(jsonPath("page.size", equalTo(books.getSize())))
                .andExpect(jsonPath("page.total_elements", equalTo(Long.valueOf(books.getTotalElements()).intValue())))
                .andExpect(jsonPath("page.total_pages", equalTo(books.getTotalPages())))
                .andExpect(jsonPath("page.number", equalTo(books.getNumber())))
                .andExpect(jsonPath("_links.self.href", equalTo(STR."http://localhost/api/v1/catalog?page=\{books.getNumber()}&size=50")))
                .andExpect(jsonPath("_embedded").exists())
                .andExpect(header().string("Content-Language", language));

        assertResponseContainsAllBooks(result, language, books.toList());
        assertResponseContainsAllEditions(result, editions);
        assertResponseContainsAllBooksAuthors(result, books.toList());

        verify(catalogService).getPage(pageableCaptor.capture(), eq(Locale.of(language)));
        var actual = pageableCaptor.getValue();
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(actual.getPageNumber()).isEqualTo(books.getNumber());
            s.assertThat(actual.getPageSize()).isEqualTo(50);
        });
        verify(editionsService).getBooksEditions(booksCaptor.capture());
        var actualBookIds = booksCaptor.getValue();
        assertThat(actualBookIds).containsExactlyInAnyOrderElementsOf(books.stream().map(Book::id).toList());
    }

    private static Stream<Arguments> defaultPageSizeProvider() {
        return Combinators.combine(
                        integers().between(0, 100),
                        languages()
                )
                .flatAs((pageIndex, language) -> otherLanguages(language).set()
                        .flatMap(otherLanguages -> editions().list().ofMinSize(50).ofMaxSize(100)
                                .map(editions -> Arguments.of(language, pageSample(pageIndex, 50, language, otherLanguages), editions))
                        )
                )
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
    void getPage(int pageNumber, int pageSize, String language, Page<Book> books, List<Edition> editions) throws Exception {
        when(catalogService.getPage(any(), any())).thenReturn(books);
        when(editionsService.getBooksEditions(any())).thenReturn(editions);

        var result = mvc.perform(get("/api/v1/catalog")
                        .accept("application/hal+json")
                        .header(ACCEPT_LANGUAGE, language)
                        .queryParam("page", Integer.toString(pageNumber))
                        .queryParam("size", Integer.toString(pageSize))
                ).andExpect(status().isPartialContent())
                .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                .andExpect(jsonPath("page.size", equalTo(books.getSize())))
                .andExpect(jsonPath("page.total_elements", equalTo(Long.valueOf(books.getTotalElements()).intValue())))
                .andExpect(jsonPath("page.total_pages", equalTo(books.getTotalPages())))
                .andExpect(jsonPath("page.number", equalTo(pageNumber)))
                .andExpect(jsonPath("_links.self.href", equalTo(STR."http://localhost/api/v1/catalog?page=\{pageNumber}&size=\{pageSize}")))
                .andExpect(jsonPath("_embedded").exists())
                .andExpect(header().string("Content-Language", language));

        assertResponseContainsAllBooks(result, language, books.toList());
        assertResponseContainsAllBooksAuthors(result, books.toList());

        verify(catalogService).getPage(pageableCaptor.capture(), eq(Locale.of(language)));
        var actual = pageableCaptor.getValue();
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(actual.getPageNumber()).isEqualTo(books.getNumber());
            s.assertThat(actual.getPageSize()).isEqualTo(books.getSize());
        });
        verify(editionsService).getBooksEditions(booksCaptor.capture());
        var actualBookIds = booksCaptor.getValue();
        assertThat(actualBookIds).containsExactlyInAnyOrderElementsOf(books.stream().map(Book::id).toList());
    }

    private static Stream<Arguments> pageProvider() {
        return Combinators.combine(
                        integers().between(0, 100),
                        integers().between(2, 100),
                        languages())
                .flatAs((pageNumber, pageSize, language) -> otherLanguages(language).set()
                        .map(otherLanguages -> Arguments.of(
                                pageNumber,
                                pageSize,
                                language,
                                pageSample(pageNumber, pageSize, language, otherLanguages),
                                editions().list().ofMinSize(pageSize).ofMaxSize(pageSize * 2).sample()
                        ))
                )
                .sampleStream().limit(5);
    }

    @ParameterizedTest
    @MethodSource("pageDefaultLanguageProvider")
    @DisplayName("provide a page corresponding to requested page and size in default language")
    void getPageDefaultLanguage(int pageNumber, int pageSize, Page<Book> books, List<Edition> editions) throws Exception {
        when(catalogService.getPage(any(), any())).thenReturn(books);
        when(editionsService.getBooksEditions(any())).thenReturn(editions);

        var result = mvc.perform(get("/api/v1/catalog")
                        .accept("application/hal+json")
                        .queryParam("page", Integer.toString(pageNumber))
                        .queryParam("size", Integer.toString(pageSize))
                ).andExpect(status().isPartialContent())
                .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                .andExpect(jsonPath("page.size", equalTo(books.getSize())))
                .andExpect(jsonPath("page.total_elements", equalTo(Long.valueOf(books.getTotalElements()).intValue())))
                .andExpect(jsonPath("page.total_pages", equalTo(books.getTotalPages())))
                .andExpect(jsonPath("page.number", equalTo(pageNumber)))
                .andExpect(jsonPath("_links.self.href", equalTo(STR."http://localhost/api/v1/catalog?page=\{pageNumber}&size=\{pageSize}")))
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
        verify(editionsService).getBooksEditions(booksCaptor.capture());
        var actualBookIds = booksCaptor.getValue();
        assertThat(actualBookIds).containsExactlyInAnyOrderElementsOf(books.stream().map(Book::id).toList());
    }

    private static Stream<Arguments> pageDefaultLanguageProvider() {
        return Combinators.combine(
                        integers().between(0, 100),
                        integers().between(2, 100))
                .flatAs((pageNumber, pageSize) -> otherLanguages("fr").set()
                        .map(otherLanguages -> Arguments.of(
                                pageNumber,
                                pageSize,
                                pageSample(pageNumber, pageSize, "fr", otherLanguages),
                                editions().list().ofMinSize(pageSize).ofMaxSize(pageSize * 2).sample()
                        ))
                )
                .sampleStream().limit(5);
    }

    @ParameterizedTest(name = "[{index}] Page = {0}, size = {1}, first = {3}, prev = {4}, next = {5}, last = {6}")
    @MethodSource({"uniquePagesProvider", "firstPagesProvider", "intermediatePagesProvider", "lastPagesProvider"})
    @DisplayName("provide a page with navigation links")
    void getPageWithNavigation(int pageNumber, int pageSize, String language, Page<Book> books, List<Edition> editions, boolean hasFirst,
                               boolean hasPrev, boolean hasNext, boolean hasLast) throws Exception {
        when(catalogService.getPage(any(), any())).thenReturn(books);
        when(editionsService.getBooksEditions(any())).thenReturn(editions);

        var result = mvc.perform(get("/api/v1/catalog")
                        .accept("application/hal+json")
                        .header(ACCEPT_LANGUAGE, language)
                        .queryParam("page", Integer.toString(pageNumber))
                        .queryParam("size", Integer.toString(pageSize))
                ).andExpect(status().isPartialContent())
                .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                .andExpect(jsonPath("_links.self.href", equalTo(STR."http://localhost/api/v1/catalog?page=\{pageNumber}&size=\{pageSize}")));

        verifyNavigationLink(result, hasFirst, "first", STR."http://localhost/api/v1/catalog?page=0&size=\{pageSize}");
        verifyNavigationLink(result, hasPrev, "prev", STR."http://localhost/api/v1/catalog?page=\{pageNumber - 1}&size=\{pageSize}");
        verifyNavigationLink(result, hasNext, "next", STR."http://localhost/api/v1/catalog?page=\{pageNumber + 1}&size=\{pageSize}");
        verifyNavigationLink(result, hasLast, "last", STR."http://localhost/api/v1/catalog?page=\{books.getTotalPages() - 1}&size=\{pageSize}");
    }

    static void verifyNavigationLink(ResultActions result, boolean hasLink, String linkName, String valueIfExists) throws Exception {
        if (hasLink) {
            result.andExpect(jsonPath(STR."_links.\{linkName}.href", equalTo(valueIfExists)));
        } else {
            result.andExpect(jsonPath(STR."_links.\{linkName}").doesNotExist());
        }
    }

    static Stream<Arguments> uniquePagesProvider() {
        return Combinators.combine(
                        integers().between(2, 100),
                        integers().between(2, 100),
                        languages()
                ).flatAs((requestedPageSize, numberOfEditions, language) -> otherLanguages(language).set()
                        .flatMap(otherLanguages -> Combinators.combine(
                                        Arbitraries.just(lastPage(0, requestedPageSize, language, otherLanguages)),
                                        editions().list().ofSize(numberOfEditions)
                                ).as((books, editions) -> Arguments.of(0, books.getPageable().getPageSize(), language, books, editions, false, false, false, false))
                        )
                )
                .sampleStream().distinct().limit(3);
    }

    static Stream<Arguments> firstPagesProvider() {
        return Combinators.combine(
                        integers().between(2, 100),
                        integers().between(2, 100),
                        languages()
                ).flatAs((requestedPageSize, numberOfEditions, language) -> otherLanguages(language).set()
                        .flatMap(otherLanguages ->
                                Combinators.combine(
                                        Arbitraries.just(fullPage(0, requestedPageSize, language, otherLanguages)),
                                        editions().list().ofSize(numberOfEditions)
                                ).as((books, editions) -> Arguments.of(0, books.getPageable().getPageSize(), language, books, editions, true, false, true, true))
                        )
                )
                .sampleStream().distinct().limit(3);
    }

    static Stream<Arguments> intermediatePagesProvider() {
        return Combinators.combine(
                        integers().between(1, 100),
                        integers().between(2, 100),
                        integers().between(2, 100),
                        languages()
                ).flatAs((page, requestedPageSize, numberOfEditions, language) -> otherLanguages(language).set()
                        .flatMap(otherLanguages -> Combinators.combine(
                                        Arbitraries.just(fullPage(page, requestedPageSize, language, otherLanguages)),
                                        editions().list().ofSize(numberOfEditions)
                                ).as((books, editions) -> Arguments.of(books.getNumber(), books.getPageable().getPageSize(), language, books, editions, true, true, true, true))
                        )
                )
                .sampleStream().distinct().limit(3);
    }

    static Stream<Arguments> lastPagesProvider() {
        return Combinators.combine(
                        integers().between(1, 100),
                        integers().between(2, 100),
                        integers().between(2, 100),
                        languages()
                ).flatAs((page, requestedPageSize, numberOfEditions, language) -> otherLanguages(language).set()
                        .flatMap(otherLanguages -> Combinators.combine(
                                                Arbitraries.just(lastPage(page, requestedPageSize, language, otherLanguages)),
                                                editions().list().ofSize(numberOfEditions)
                                        )
                                        .as((books, editions) -> Arguments.of(books.getNumber(), books.getPageable().getPageSize(), language, books, editions, true, true, false, true))
                        )
                )
                .sampleStream().distinct().limit(3);
    }

    private static Page<Book> pageSample(int pageIndex, int requestedPageSize, String language, Set<String> otherLanguages) {
        var isLastPage = Arbitraries.of(TRUE, FALSE).sample();
        if (isLastPage) {
            return lastPage(pageIndex, requestedPageSize, language, otherLanguages);
        }
        return fullPage(pageIndex, requestedPageSize, language, otherLanguages);
    }

    private static PageImpl<Book> lastPage(int page, int requestedPageSize, String language, Set<String> otherLanguages) {
        int pageSize = integers().between(1, requestedPageSize).sample();
        int totalRows = pageSize;
        if (page > 0) {
            totalRows += requestedPageSize * page;
        }
        var elements = books(language, otherLanguages).list().ofSize(pageSize).sample();
        return new PageImpl<>(elements, PageRequest.of(page, requestedPageSize), totalRows);
    }

    private static PageImpl<Book> fullPage(int page, int requestedPageSize, String language, Set<String> otherLanguages) {
        var totalPages = integers().between(page + 1, 150).sample();
        var lastPageSize = integers().between(1, requestedPageSize - 1).sample();
        var totalRows = totalPages * requestedPageSize + lastPageSize;
        var elements = books(language, otherLanguages).list().ofSize(requestedPageSize).sample();
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

    private void assertResponseContainsAllEditions(ResultActions result, List<Edition> expected) throws Exception {
        result.andExpect(jsonPath("_embedded.editions").exists())
                .andExpect(jsonPath("_embedded.editions").isArray())
                .andExpect(jsonPath("_embedded.editions", hasSize(expected.size())))
                .andExpect(jsonPath("_embedded.editions").exists())
                .andExpect(jsonPath("_embedded.editions").isArray())
                .andExpect(jsonPath("_embedded.editions", hasSize(expected.size())));
        for (int i = 0; i < expected.size(); i++) {
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

}
