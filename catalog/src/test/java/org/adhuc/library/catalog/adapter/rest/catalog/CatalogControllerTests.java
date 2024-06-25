package org.adhuc.library.catalog.adapter.rest.catalog;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Combinators;
import org.adhuc.library.catalog.adapter.rest.PaginationSerializationConfiguration;
import org.adhuc.library.catalog.adapter.rest.RequestValidationConfiguration;
import org.adhuc.library.catalog.adapter.rest.authors.AuthorModelAssembler;
import org.adhuc.library.catalog.adapter.rest.books.BookModelAssembler;
import org.adhuc.library.catalog.authors.Author;
import org.adhuc.library.catalog.books.Book;
import org.adhuc.library.catalog.books.CatalogService;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import static org.adhuc.library.catalog.books.BooksMother.books;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {CatalogController.class, BookModelAssembler.class, AuthorModelAssembler.class})
@Import({RequestValidationConfiguration.class, PaginationSerializationConfiguration.class})
@DisplayName("Catalog controller should")
class CatalogControllerTests {

    @Autowired
    private MockMvc mvc;
    @MockBean
    private CatalogService catalogService;
    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    @Test
    @DisplayName("provide an empty page when no book can be found in the catalog")
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
    @DisplayName("provide a unique page when the number of books in the catalog is lower than or equal to the requested page size")
    void uniqueDefaultPage(int numberOfElements, List<Book> books) throws Exception {
        var request = PageRequest.of(0, 50);
        when(catalogService.getPage(any())).thenReturn(new PageImpl<>(books, request, numberOfElements));

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

        assertResponseContainsAllBooks(result, books);
        assertResponseContainsAllBooksAuthors(result, books);

        verify(catalogService).getPage(pageableCaptor.capture());
        var actual = pageableCaptor.getValue();
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(actual.getPageNumber()).isZero();
            s.assertThat(actual.getPageSize()).isEqualTo(50);
        });
    }

    private static Stream<Arguments> uniqueDefaultPageProvider() {
        return integers().between(1, 50)
                .flatMap(numberOfElements -> books().list().ofSize(numberOfElements)
                        .map(books -> Arguments.of(numberOfElements, books)))
                .sampleStream().limit(5);
    }

    @ParameterizedTest
    @MethodSource("defaultPageSizeProvider")
    @DisplayName("provide a page with default page size when not specified explicitly")
    void defaultPageSize(Page<Book> books) throws Exception {
        when(catalogService.getPage(any())).thenReturn(books);

        var result = mvc.perform(get("/api/v1/catalog").accept("application/hal+json")
                        .param("page", String.valueOf(books.getNumber())))
                .andExpect(status().isPartialContent())
                .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                .andExpect(jsonPath("page.size", equalTo(books.getSize())))
                .andExpect(jsonPath("page.total_elements", equalTo(Long.valueOf(books.getTotalElements()).intValue())))
                .andExpect(jsonPath("page.total_pages", equalTo(books.getTotalPages())))
                .andExpect(jsonPath("page.number", equalTo(books.getNumber())))
                .andExpect(jsonPath("_links.self.href", equalTo(STR."http://localhost/api/v1/catalog?page=\{books.getNumber()}&size=50")))
                .andExpect(jsonPath("_embedded").exists());

        assertResponseContainsAllBooks(result, books.toList());
        assertResponseContainsAllBooksAuthors(result, books.toList());

        verify(catalogService).getPage(pageableCaptor.capture());
        var actual = pageableCaptor.getValue();
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(actual.getPageNumber()).isEqualTo(books.getNumber());
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
    @MethodSource("pageProvider")
    @DisplayName("provide a page corresponding to requested page and size")
    void getPage(int pageNumber, int pageSize, Page<Book> books) throws Exception {
        when(catalogService.getPage(any())).thenReturn(books);

        var result = mvc.perform(get("/api/v1/catalog").accept("application/hal+json")
                                .param("page", Integer.toString(pageNumber))
                                .param("size", Integer.toString(pageSize))
                ).andExpect(status().isPartialContent())
                .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                .andExpect(jsonPath("page.size", equalTo(books.getSize())))
                .andExpect(jsonPath("page.total_elements", equalTo(Long.valueOf(books.getTotalElements()).intValue())))
                .andExpect(jsonPath("page.total_pages", equalTo(books.getTotalPages())))
                .andExpect(jsonPath("page.number", equalTo(pageNumber)))
                .andExpect(jsonPath("_links.self.href", equalTo(STR."http://localhost/api/v1/catalog?page=\{pageNumber}&size=\{pageSize}")))
                .andExpect(jsonPath("_embedded").exists());

        assertResponseContainsAllBooks(result, books.toList());
        assertResponseContainsAllBooksAuthors(result, books.toList());

        verify(catalogService).getPage(pageableCaptor.capture());
        var actual = pageableCaptor.getValue();
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(actual.getPageNumber()).isEqualTo(books.getNumber());
            s.assertThat(actual.getPageSize()).isEqualTo(books.getSize());
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
    void getPageWithNavigation(int pageNumber, int pageSize, Page<Book> books, boolean hasFirst,
                               boolean hasPrev, boolean hasNext, boolean hasLast) throws Exception {
        when(catalogService.getPage(any())).thenReturn(books);

        var result = mvc.perform(get("/api/v1/catalog").accept("application/hal+json")
                        .param("page", Integer.toString(pageNumber))
                        .param("size", Integer.toString(pageSize)))
                .andExpect(status().isPartialContent())
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
        return integers().between(2, 100)
                .map(requestedPageSize -> lastPage(0, requestedPageSize))
                .map(books -> Arguments.of(0, books.getPageable().getPageSize(), books, false, false, false, false))
                .sampleStream().distinct().limit(3);
    }

    static Stream<Arguments> firstPagesProvider() {
        return integers().between(2, 100)
                .map(requestedPageSize -> fullPage(0, requestedPageSize))
                .map(books -> Arguments.of(0, books.getPageable().getPageSize(), books, true, false, true, true))
                .sampleStream().distinct().limit(3);
    }

    static Stream<Arguments> intermediatePagesProvider() {
        return Combinators.combine(integers().between(1, 100), integers().between(2, 100))
                .as(CatalogControllerTests::fullPage)
                .map(books -> Arguments.of(books.getNumber(), books.getPageable().getPageSize(), books, true, true, true, true))
                .sampleStream().distinct().limit(3);
    }

    static Stream<Arguments> lastPagesProvider() {
        return Combinators.combine(integers().between(1, 100), integers().between(2, 100))
                .as(CatalogControllerTests::lastPage)
                .map(books -> Arguments.of(books.getNumber(), books.getPageable().getPageSize(), books, true, true, false, true))
                .sampleStream().distinct().limit(3);
    }

    private static Page<Book> pageSample(int pageIndex, int requestedPageSize) {
        var isLastPage = Arbitraries.of(TRUE, FALSE).sample();
        if (isLastPage) {
            return lastPage(pageIndex, requestedPageSize);
        }
        return fullPage(pageIndex, requestedPageSize);
    }

    private static PageImpl<Book> lastPage(int page, int requestedPageSize) {
        int pageSize = integers().between(1, requestedPageSize).sample();
        int totalRows = pageSize;
        if (page > 0) {
            totalRows += requestedPageSize * page;
        }
        var elements = books().list().ofSize(pageSize).sample();
        return new PageImpl<>(elements, PageRequest.of(page, requestedPageSize), totalRows);
    }

    private static PageImpl<Book> fullPage(int page, int requestedPageSize) {
        var totalPages = integers().between(page + 1, 150).sample();
        var lastPageSize = integers().between(1, requestedPageSize - 1).sample();
        var totalRows = totalPages * requestedPageSize + lastPageSize;
        var elements = books().list().ofSize(requestedPageSize).sample();
        return new PageImpl<>(elements, PageRequest.of(page, requestedPageSize), totalRows);
    }

    private void assertResponseContainsAllBooks(ResultActions result, List<Book> expected) throws Exception {
        result.andExpect(jsonPath("_embedded.books").exists())
                .andExpect(jsonPath("_embedded.books").isArray())
                .andExpect(jsonPath("_embedded.books", hasSize(expected.size())));
        for (int i = 0; i < expected.size(); i++) {
            assertResponseContainsBook(result, "_embedded.books[" + i + "].", expected.get(i));
        }
    }

    private void assertResponseContainsBook(ResultActions result, String jsonPrefix, Book expected) throws Exception {
        result.andExpect(jsonPath(jsonPrefix + "id", equalTo(expected.id().toString())))
                .andExpect(jsonPath(jsonPrefix + "isbn", equalTo(expected.isbn())))
                .andExpect(jsonPath(jsonPrefix + "title", equalTo(expected.title())))
                .andExpect(jsonPath(jsonPrefix + "authors", containsInAnyOrder(
                        expected.authors().stream().map(Author::id).map(UUID::toString).toArray())))
                .andExpect(jsonPath(jsonPrefix + "language", equalTo(expected.language())))
                .andExpect(jsonPath(jsonPrefix + "summary", equalTo(expected.summary())))
                .andExpect(jsonPath(jsonPrefix + "_links.self.href", equalTo("http://localhost/api/v1/books/" + expected.id())));
    }

    private void assertResponseContainsAllBooksAuthors(ResultActions result, Collection<Book> expectedBooks) throws Exception {
        var expectedAuthors = expectedBooks.stream().map(Book::authors).flatMap(Collection::stream).collect(toSet());
        assertResponseContainsAllEmbeddedAuthors(result, expectedAuthors);
    }

}
