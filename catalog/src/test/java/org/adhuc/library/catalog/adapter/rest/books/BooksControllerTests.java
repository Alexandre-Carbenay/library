package org.adhuc.library.catalog.adapter.rest.books;

import net.jqwik.api.Arbitrary;
import org.adhuc.library.catalog.adapter.rest.authors.AuthorModelAssembler;
import org.adhuc.library.catalog.adapter.rest.support.validation.openapi.RequestValidationConfiguration;
import org.adhuc.library.catalog.books.Book;
import org.adhuc.library.catalog.books.BooksMother;
import org.adhuc.library.catalog.books.BooksMother.Books;
import org.adhuc.library.catalog.books.BooksService;
import org.adhuc.library.catalog.books.PublicationDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.stream.Stream;

import static org.adhuc.library.catalog.adapter.rest.authors.AuthorsAssertions.assertResponseContainsAllEmbeddedAuthors;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {
        BooksController.class,
        BookModelAssembler.class,
        BookDetailsModelAssembler.class,
        AuthorModelAssembler.class
})
@Import(RequestValidationConfiguration.class)
@DisplayName("Books controller should")
class BooksControllerTests {

    @Autowired
    private MockMvc mvc;
    @MockBean
    private BooksService booksService;

    @ParameterizedTest
    @ValueSource(strings = {"123", "invalid"})
    @DisplayName("refuse providing a book with invalid ISBN")
    void invalidBookId(String invalidIsbn) throws Exception {
        mvc.perform(get("/api/v1/books/{isbn}", invalidIsbn).accept("application/hal+json"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("type", equalTo("/problems/invalid-request")))
                .andExpect(jsonPath("status", equalTo(400)))
                .andExpect(jsonPath("title", equalTo("Request validation error")))
                .andExpect(jsonPath("detail", equalTo("Request parameters or body are invalid compared to the OpenAPI specification. See errors for more information")))
                .andExpect(jsonPath("errors").isArray())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0].detail",
                        equalTo(STR."Input string \"\{invalidIsbn}\" is not a valid ISBN")))
                .andExpect(jsonPath("errors[0].parameter", equalTo("isbn")));
    }

    @Test
    @DisplayName("not find a book corresponding to an unknown ISBN")
    void unknownBookId() throws Exception {
        var unknownIsbn = "9782081275232";
        mvc.perform(get("/api/v1/books/{id}", unknownIsbn).accept("application/hal+json"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("type", equalTo("/problems/unknown-entity")))
                .andExpect(jsonPath("status", equalTo(404)))
                .andExpect(jsonPath("title", equalTo("Unknown book")))
                .andExpect(jsonPath("detail", allOf(
                        startsWith("No book exists with ISBN"),
                        containsString(unknownIsbn))
                ));
    }

    @ParameterizedTest
    @MethodSource({
            "booksWithExactPublicationDateProvider",
            "booksWithYearADPublicationDateProvider",
            "booksWithYearBCPublicationDateProvider"
    })
    @DisplayName("provide the book details corresponding to the ID")
    void knownBookId(Book book) throws Exception {
        when(booksService.getBook(Mockito.any())).thenReturn(Optional.of(book));

        var result = mvc.perform(get("/api/v1/books/{isbn}", book.isbn()).accept("application/hal+json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("isbn", equalTo(book.isbn())))
                .andExpect(jsonPath("title", equalTo(book.title())))
                .andExpect(jsonPath("publication_date", equalTo(book.publicationDate().toString())))
                .andExpect(jsonPath("language", equalTo(book.language())))
                .andExpect(jsonPath("summary", equalTo(book.summary())))
                .andExpect(jsonPath("_links.self.href", equalTo("http://localhost/api/v1/books/" + book.isbn())));

        assertResponseContainsAllEmbeddedAuthors(result, book.authors());

        verify(booksService).getBook(book.isbn());
    }

    static Stream<Arguments> booksWithExactPublicationDateProvider() {
        return booksWithPublicationDateProvider(Books.exactPublicationDates());
    }

    static Stream<Arguments> booksWithYearADPublicationDateProvider() {
        return booksWithPublicationDateProvider(Books.yearADPublicationDates());
    }

    static Stream<Arguments> booksWithYearBCPublicationDateProvider() {
        return booksWithPublicationDateProvider(Books.yearBCPublicationDates());
    }

    static Stream<Arguments> booksWithPublicationDateProvider(Arbitrary<PublicationDate> publicationDateArbitrary) {
        return publicationDateArbitrary
                .map(publicationDate -> BooksMother.builder().publicationDate(publicationDate).build())
                .map(Arguments::of)
                .sampleStream().limit(3);
    }

}
