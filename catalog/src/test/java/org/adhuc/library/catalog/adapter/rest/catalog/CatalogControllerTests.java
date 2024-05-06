package org.adhuc.library.catalog.adapter.rest.catalog;

import org.adhuc.library.catalog.adapter.rest.PaginationSerializationConfiguration;
import org.adhuc.library.catalog.adapter.rest.RequestValidationConfiguration;
import org.adhuc.library.catalog.adapter.rest.authors.AuthorModelAssembler;
import org.adhuc.library.catalog.adapter.rest.books.BookModelAssembler;
import org.adhuc.library.catalog.books.Author;
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

import static java.util.stream.Collectors.toSet;
import static net.jqwik.api.Arbitraries.integers;
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
        assertResponseContainsAllAuthors(result, expectedAuthors);
    }

    private void assertResponseContainsAllAuthors(ResultActions result, Collection<Author> expectedAuthors) throws Exception {
        result.andExpect(jsonPath("_embedded.authors").exists())
                .andExpect(jsonPath("_embedded.authors").isArray())
                .andExpect(jsonPath("_embedded.authors", hasSize(expectedAuthors.size())));
        for (var author : expectedAuthors) {
            result.andExpect(jsonPath(
                    "_embedded.authors.[" +
                            "?(@.id == \"" + author.id() + "\" " +
                            "&& @.name == \"" + author.name() + "\" " +
                            "&& @._links.self.href == \"http://localhost/api/v1/authors/" + author.id() + "\")]").exists());
        }
    }

}
