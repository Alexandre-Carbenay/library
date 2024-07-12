package org.adhuc.library.catalog.adapter.rest.books;

import net.jqwik.api.Arbitrary;
import org.adhuc.library.catalog.adapter.rest.support.validation.openapi.RequestValidationConfiguration;
import org.adhuc.library.catalog.adapter.rest.authors.AuthorModelAssembler;
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
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;
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
    @DisplayName("refuse providing a book with invalid ID")
    void invalidBookId(String invalidId) throws Exception {
        mvc.perform(get("/api/v1/books/{id}", invalidId).accept("application/hal+json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("status", equalTo(400)))
                .andExpect(jsonPath("error", equalTo("INVALID_REQUEST")))
                .andExpect(jsonPath("description", equalTo("Request validation error")))
                .andExpect(jsonPath("sources").isArray())
                .andExpect(jsonPath("sources", hasSize(1)))
                .andExpect(jsonPath("sources[0].reason",
                        equalTo(STR."Input string \"\{invalidId}\" is not a valid UUID")))
                .andExpect(jsonPath("sources[0].parameter", equalTo("id")));
    }

    @Test
    @DisplayName("not find a book corresponding to an unknown ID")
    void unknownBookId() throws Exception {
        var unknownId = UUID.randomUUID();
        mvc.perform(get("/api/v1/books/{id}", unknownId).accept("application/hal+json"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("status", equalTo(404)))
                .andExpect(jsonPath("error", equalTo("ENTITY_NOT_FOUND")))
                .andExpect(jsonPath("description", allOf(
                        startsWith("No book exists with id"),
                        containsString(unknownId.toString()))
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

        var result = mvc.perform(get("/api/v1/books/{id}", book.id()).accept("application/hal+json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", equalTo(book.id().toString())))
                .andExpect(jsonPath("isbn", equalTo(book.isbn())))
                .andExpect(jsonPath("title", equalTo(book.title())))
                .andExpect(jsonPath("publication_date", equalTo(book.publicationDate().toString())))
                .andExpect(jsonPath("language", equalTo(book.language())))
                .andExpect(jsonPath("summary", equalTo(book.summary())))
                .andExpect(jsonPath("_links.self.href", equalTo("http://localhost/api/v1/books/" + book.id())));

        assertResponseContainsAllEmbeddedAuthors(result, book.authors());

        verify(booksService).getBook(book.id());
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
