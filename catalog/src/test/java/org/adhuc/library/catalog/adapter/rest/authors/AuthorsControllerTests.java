package org.adhuc.library.catalog.adapter.rest.authors;

import org.adhuc.library.catalog.adapter.rest.RestAdapterTestConfiguration;
import org.adhuc.library.catalog.adapter.rest.books.BookModelAssembler;
import org.adhuc.library.catalog.authors.Author;
import org.adhuc.library.catalog.authors.AuthorsService;
import org.adhuc.library.catalog.books.Book;
import org.adhuc.library.catalog.books.BooksMother.Books;
import org.adhuc.library.catalog.books.BooksService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.adhuc.library.catalog.adapter.rest.books.BooksAssertions.assertResponseContainsAllEmbeddedBooks;
import static org.adhuc.library.catalog.authors.AuthorsMother.Authors.*;
import static org.adhuc.library.catalog.authors.AuthorsMother.Real.ALBERT_CAMUS;
import static org.adhuc.library.catalog.authors.AuthorsMother.Real.GUSTAVE_FLAUBERT;
import static org.adhuc.library.catalog.authors.AuthorsMother.author;
import static org.adhuc.library.catalog.authors.AuthorsMother.builder;
import static org.adhuc.library.catalog.books.BooksMother.Real.*;
import static org.adhuc.library.catalog.books.BooksMother.notableBooksOf;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings({"NotNullFieldNotInitialized"})
@Tag("integration")
@Tag("restApi")
@WebMvcTest(controllers = {
        AuthorsController.class,
        AuthorDetailsModelAssembler.class,
        BookModelAssembler.class
})
@Import(RestAdapterTestConfiguration.class)
@DisplayName("Authors controller should")
class AuthorsControllerTests {

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private AuthorsService authorsService;
    @MockitoBean
    private BooksService booksService;

    @ParameterizedTest
    @MethodSource("invalidAuthorIdProvider")
    @DisplayName("refuse providing an author with invalid ID")
    void invalidAuthorId(String invalidId, String errorDetail) throws Exception {
        mvc.perform(get("/api/v1/authors/{id}", invalidId).accept("application/hal+json"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("type", equalTo("/problems/invalid-request")))
                .andExpect(jsonPath("status", equalTo(400)))
                .andExpect(jsonPath("title", equalTo("Request validation error")))
                .andExpect(jsonPath("detail", equalTo("Request parameters or body are invalid compared to the OpenAPI specification. See errors for more information")))
                .andExpect(jsonPath("errors").isArray())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0].detail", equalTo(errorDetail)))
                .andExpect(jsonPath("errors[0].parameter", equalTo("id")));
    }

    static Stream<Arguments> invalidAuthorIdProvider() {
        return Stream.of(
                Arguments.of("123", "Input string \"123\" is not a valid UUID"),
                Arguments.of("invalid", "Input string \"invalid\" is not a valid UUID")
        );
    }

    @Test
    @DisplayName("not find an author corresponding to an unknown ID")
    void unknownAuthorId() throws Exception {
        var unknownId = UUID.randomUUID();
        mvc.perform(get("/api/v1/authors/{id}", unknownId).accept("application/hal+json"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("type", equalTo("/problems/unknown-entity")))
                .andExpect(jsonPath("status", equalTo(404)))
                .andExpect(jsonPath("title", equalTo("Unknown author")))
                .andExpect(jsonPath("detail", allOf(
                        startsWith("No author exists with id"),
                        containsString(unknownId.toString()))
                ));
    }

    @Test
    @DisplayName("provide the alive author details corresponding to the ID")
    void knownAliveAuthorId() throws Exception {
        var author = builder().dateOfBirth(aliveDateOfBirth()).dateOfDeath(null).build();

        when(authorsService.getAuthor(Mockito.any())).thenReturn(Optional.of(author));

        mvc.perform(get("/api/v1/authors/{id}", author.id()).accept("application/hal+json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", equalTo(author.id().toString())))
                .andExpect(jsonPath("name", equalTo(author.name())))
                .andExpect(jsonPath("date_of_birth", equalTo(author.dateOfBirth().toString())))
                .andExpect(jsonPath("_links.self.href", equalTo("http://localhost/api/v1/authors/" + author.id())))
                .andExpect(jsonPath("date_of_death").doesNotExist());

        verify(authorsService).getAuthor(author.id());
    }

    @Test
    @DisplayName("provide the dead author details corresponding to the ID")
    void knownDeadAuthorId() throws Exception {
        var dateOfBirth = dateOfBirth();
        var author = builder().dateOfBirth(dateOfBirth()).dateOfDeath(deadDateOfDeath(dateOfBirth)).build();

        when(authorsService.getAuthor(Mockito.any())).thenReturn(Optional.of(author));

        mvc.perform(get("/api/v1/authors/{id}", author.id()).accept("application/hal+json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", equalTo(author.id().toString())))
                .andExpect(jsonPath("name", equalTo(author.name())))
                .andExpect(jsonPath("date_of_birth", equalTo(author.dateOfBirth().toString())))
                .andExpect(jsonPath("_links.self.href", equalTo("http://localhost/api/v1/authors/" + author.id())))
                .andExpect(jsonPath("date_of_death", equalTo(Objects.requireNonNull(author.dateOfDeath()).toString())));

        verify(authorsService).getAuthor(author.id());
    }

    @Test
    @DisplayName("provide the author details with expected notable books")
    void knownAuthorNotableBooksDefaultLanguage() throws Exception {
        var author = author();
        var notableBooks = notableBooksOf(author.id());

        when(authorsService.getAuthor(Mockito.any())).thenReturn(Optional.of(author));
        when(booksService.getNotableBooks(author.id())).thenReturn(notableBooks);

        var result = mvc.perform(get("/api/v1/authors/{id}", author.id()).accept("application/hal+json"))
                .andExpect(status().isOk());

        assertResponseContainsAllEmbeddedBooks(result, "notable_books", notableBooks);

        verify(authorsService).getAuthor(author.id());
        verify(booksService).getNotableBooks(author.id());
    }

    @Test
    @DisplayName("provide the author details with expected notable books")
    void knownAuthorNotableBooksInLanguage() throws Exception {
        var author = author();
        var originalLanguage = Books.language();
        var otherLanguages = Books.otherLanguages(originalLanguage);
        var notableBooks = notableBooksOf(author.id(), originalLanguage, otherLanguages);

        when(authorsService.getAuthor(Mockito.any())).thenReturn(Optional.of(author));
        when(booksService.getNotableBooks(author.id())).thenReturn(notableBooks);

        var result = mvc.perform(get("/api/v1/authors/{id}", author.id())
                        .accept("application/hal+json")
                        .header("Accept-Language", originalLanguage)
                )
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Language", originalLanguage));

        assertResponseContainsAllEmbeddedBooks(result, "notable_books", notableBooks, originalLanguage);

        verify(authorsService).getAuthor(author.id());
        verify(booksService).getNotableBooks(author.id());
    }

    @ParameterizedTest
    @MethodSource("authorWithNotableBooksInDifferentLanguagesProvider")
    @DisplayName("provide the author details with notables books in the expected language, selected from the accept ones")
    void knownAuthorsNotableBooksMultipleLanguages(Author author, List<Book> notableBooks, List<Book> expectedBooks,
                                                   String acceptLanguages, String expectedLanguage) throws Exception {
        when(authorsService.getAuthor(Mockito.any())).thenReturn(Optional.of(author));
        when(booksService.getNotableBooks(author.id())).thenReturn(notableBooks);

        var result = mvc.perform(get("/api/v1/authors/{id}", author.id())
                        .accept("application/hal+json")
                        .header("Accept-Language", acceptLanguages)
                )
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Language", expectedLanguage));

        assertResponseContainsAllEmbeddedBooks(result, "notable_books", expectedBooks, expectedLanguage);

        verify(authorsService).getAuthor(author.id());
        verify(booksService).getNotableBooks(author.id());
    }

    static Stream<Arguments> authorWithNotableBooksInDifferentLanguagesProvider() {
        return Stream.of(
                Arguments.of(
                        ALBERT_CAMUS,
                        List.of(L_ETRANGER, LA_PESTE, LA_CHUTE),
                        List.of(L_ETRANGER, LA_PESTE, LA_CHUTE),
                        "fr, en;q=0.8, de;q=0.7",
                        "fr"
                ),
                Arguments.of(
                        ALBERT_CAMUS,
                        List.of(L_ETRANGER, LA_PESTE, LA_CHUTE),
                        List.of(L_ETRANGER, LA_PESTE),
                        "en",
                        "en"
                ),
                Arguments.of(
                        ALBERT_CAMUS,
                        List.of(L_ETRANGER, LA_PESTE, LA_CHUTE),
                        List.of(L_ETRANGER, LA_PESTE),
                        "en;q=0.8, de;q=0.7",
                        "en"
                ),
                Arguments.of(
                        ALBERT_CAMUS,
                        List.of(L_ETRANGER, LA_PESTE, LA_CHUTE),
                        List.of(L_ETRANGER, LA_PESTE),
                        "en;q=0.9, fr;q=0.8, de;q=0.7",
                        "en"
                ),
                Arguments.of(
                        ALBERT_CAMUS,
                        List.of(L_ETRANGER, LA_PESTE, LA_CHUTE),
                        List.of(L_ETRANGER, LA_CHUTE),
                        "de, en;q=0.7",
                        "de"
                ),
                Arguments.of(
                        ALBERT_CAMUS,
                        List.of(L_ETRANGER, LA_PESTE, LA_CHUTE),
                        List.of(L_ETRANGER, LA_PESTE),
                        "it, de;q=0.7, en;q=0.8",
                        "en"
                ),
                Arguments.of(
                        ALBERT_CAMUS,
                        List.of(L_ETRANGER, LA_PESTE, LA_CHUTE),
                        List.of(L_ETRANGER, LA_CHUTE),
                        "it, de;q=0.7",
                        "de"
                )
        );
    }

    @ParameterizedTest
    @MethodSource("authorWithNotableBooksUnavailableForLanguagesProvider")
    @DisplayName("provide the author details with all notables books if none of the accept languages correspond to books")
    void knownAuthorsNotableBooksNoneWithAcceptLanguages(Author author, List<Book> notableBooks, String acceptLanguages) throws Exception {
        when(authorsService.getAuthor(Mockito.any())).thenReturn(Optional.of(author));
        when(booksService.getNotableBooks(author.id())).thenReturn(notableBooks);

        var result = mvc.perform(get("/api/v1/authors/{id}", author.id())
                        .accept("application/hal+json")
                        .header("Accept-Language", acceptLanguages)
                )
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Content-Language"));

        assertResponseContainsAllEmbeddedBooks(result, "notable_books", notableBooks);

        verify(authorsService).getAuthor(author.id());
        verify(booksService).getNotableBooks(author.id());
    }

    static Stream<Arguments> authorWithNotableBooksUnavailableForLanguagesProvider() {
        return Stream.of(
                Arguments.of(
                        ALBERT_CAMUS,
                        List.of(L_ETRANGER, LA_PESTE, LA_CHUTE),
                        "it"
                ),
                Arguments.of(
                        ALBERT_CAMUS,
                        List.of(L_ETRANGER, LA_PESTE, LA_CHUTE),
                        "it, es;q=0.8"
                ),
                Arguments.of(
                        GUSTAVE_FLAUBERT,
                        List.of(MADAME_BOVARY, SALAMMBO),
                        "en, de;q=0.5"
                )
        );
    }

}
