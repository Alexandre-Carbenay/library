package org.adhuc.library.catalog.adapter.rest.books;

import org.adhuc.library.catalog.adapter.rest.authors.AuthorModelAssembler;
import org.adhuc.library.catalog.adapter.rest.support.validation.openapi.RequestValidationConfiguration;
import org.adhuc.library.catalog.books.Book;
import org.adhuc.library.catalog.books.BooksService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.adhuc.library.catalog.adapter.rest.authors.AuthorsAssertions.assertResponseContainsAllEmbeddedAuthors;
import static org.adhuc.library.catalog.books.BooksMother.builder;
import static org.adhuc.library.catalog.books.BooksMother.detailsBuilder;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE;
import static org.springframework.http.HttpHeaders.CONTENT_LANGUAGE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("preview")
@WebMvcTest(controllers = {
        BooksController.class,
        BookDetailsModelAssembler.class,
        AuthorModelAssembler.class
})
@Import(RequestValidationConfiguration.class)
@DisplayName("Books controller should")
class BooksControllerTests {

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private BooksService booksService;

    @ParameterizedTest
    @ValueSource(strings = {"123", "invalid"})
    @DisplayName("refuse providing a book with invalid ID")
    void invalidBookId(String invalidId) throws Exception {
        mvc.perform(get("/api/v1/books/{id}", invalidId).accept("application/hal+json"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("type", equalTo("/problems/invalid-request")))
                .andExpect(jsonPath("status", equalTo(400)))
                .andExpect(jsonPath("title", equalTo("Request validation error")))
                .andExpect(jsonPath("detail", equalTo("Request parameters or body are invalid compared to the OpenAPI specification. See errors for more information")))
                .andExpect(jsonPath("errors").isArray())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0].detail",
                        equalTo(STR."Input string \"\{invalidId}\" is not a valid UUID")))
                .andExpect(jsonPath("errors[0].parameter", equalTo("id")));
    }

    @Test
    @DisplayName("not find a book corresponding to an unknown ID")
    void unknownBookId() throws Exception {
        var unknownId = UUID.randomUUID();
        mvc.perform(get("/api/v1/books/{id}", unknownId).accept("application/hal+json"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("type", equalTo("/problems/unknown-entity")))
                .andExpect(jsonPath("status", equalTo(404)))
                .andExpect(jsonPath("title", equalTo("Unknown book")))
                .andExpect(jsonPath("detail", allOf(
                        startsWith("No book exists with id"),
                        containsString(unknownId.toString()))
                ));
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource({
            "knownBookSingleLanguageProvider",
            "knownBookMultipleLanguagesProvider"
    })
    @DisplayName("provide the book details corresponding to the ID, without accept language header")
    void knownBookIdWithoutAcceptLanguage(String displayName, Book book, String expectedLanguage) throws Exception {
        when(booksService.getBook(any())).thenReturn(Optional.of(book));

        var result = mvc.perform(get("/api/v1/books/{id}", book.id())
                        .accept("application/hal+json")
                )
                .andExpect(status().isOk())
                .andExpect(header().string(CONTENT_LANGUAGE, expectedLanguage))
                .andExpect(jsonPath("id", equalTo(book.id().toString())))
                .andExpect(jsonPath("title", equalTo(book.titleIn(expectedLanguage))))
                .andExpect(jsonPath("description", equalTo(book.descriptionIn(expectedLanguage))))
                .andExpect(jsonPath("_links.self.href", equalTo(STR."http://localhost/api/v1/books/\{book.id()}")));

        assertResponseContainsAllEmbeddedAuthors(result, book.authors());

        verify(booksService).getBook(book.id());
    }

    private static Stream<Arguments> knownBookSingleLanguageProvider() {
        return Stream.of(
                Arguments.of(
                        "Known book with only one language",
                        builder()
                                .originalLanguage("fr")
                                .details(Set.of(
                                        detailsBuilder().language("fr").withWikipediaLink().build()
                                ))
                                .build(),
                        "fr"
                )
        );
    }

    private static Stream<Arguments> knownBookMultipleLanguagesProvider() {
        return Stream.of(
                Arguments.of(
                        "Known book with multiple languages",
                        builder()
                                .originalLanguage("it")
                                .details(Set.of(
                                        detailsBuilder().language("fr").withWikipediaLink().build(),
                                        detailsBuilder().language("it").withWikipediaLink().build()
                                ))
                                .build(),
                        "it"
                )
        );
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource({
            "knownBookSingleOriginalLanguageProvider",
            "knownBookMultipleOriginalLanguageProvider",
            "knownBookMultipleOtherLanguageProvider",
            "knownBookUnknownLanguageProvider",
            "knownBookMultipleLanguagesWithWeightProvider"
    })
    @DisplayName("provide the book details corresponding to the ID")
    void knownBookId(String displayName, Book book, String acceptLanguage, String expectedLanguage) throws Exception {
        when(booksService.getBook(any())).thenReturn(Optional.of(book));

        var result = mvc.perform(get("/api/v1/books/{id}", book.id())
                        .accept("application/hal+json")
                        .header(ACCEPT_LANGUAGE, acceptLanguage)
                )
                .andExpect(status().isOk())
                .andExpect(header().string(CONTENT_LANGUAGE, expectedLanguage))
                .andExpect(jsonPath("id", equalTo(book.id().toString())))
                .andExpect(jsonPath("title", equalTo(book.titleIn(expectedLanguage))))
                .andExpect(jsonPath("description", equalTo(book.descriptionIn(expectedLanguage))))
                .andExpect(jsonPath("_links.self.href", equalTo(STR."http://localhost/api/v1/books/\{book.id()}")));

        assertResponseContainsAllEmbeddedAuthors(result, book.authors());

        verify(booksService).getBook(book.id());
    }

    private static Stream<Arguments> knownBookSingleOriginalLanguageProvider() {
        return Stream.of(
                Arguments.of(
                        "Known book with only one language, accepting the original language",
                        builder()
                                .originalLanguage("fr")
                                .details(Set.of(
                                        detailsBuilder().language("fr").withWikipediaLink().build()
                                ))
                                .build(),
                        "fr",
                        "fr"
                )
        );
    }

    private static Stream<Arguments> knownBookMultipleOriginalLanguageProvider() {
        return Stream.of(
                Arguments.of(
                        "Known book with multiple languages, accepting the original language",
                        builder()
                                .originalLanguage("fr")
                                .details(Set.of(
                                        detailsBuilder().language("fr").withWikipediaLink().build(),
                                        detailsBuilder().language("it").withWikipediaLink().build()
                                ))
                                .build(),
                        "fr",
                        "fr"
                )
        );
    }

    private static Stream<Arguments> knownBookMultipleOtherLanguageProvider() {
        return Stream.of(
                Arguments.of(
                        "Known book with multiple languages, accepting a language different from the original one",
                        builder()
                                .originalLanguage("it")
                                .details(Set.of(
                                        detailsBuilder().language("fr").withWikipediaLink().build(),
                                        detailsBuilder().language("it").withWikipediaLink().build()
                                ))
                                .build(),
                        "fr",
                        "fr"
                )
        );
    }

    private static Stream<Arguments> knownBookUnknownLanguageProvider() {
        return Stream.of(
                Arguments.of(
                        "Known book with multiple languages, accepting an unknown language",
                        builder()
                                .originalLanguage("it")
                                .details(Set.of(
                                        detailsBuilder().language("fr").withWikipediaLink().build(),
                                        detailsBuilder().language("it").withWikipediaLink().build()
                                ))
                                .build(),
                        "de",
                        "it"
                )
        );
    }

    private static Stream<Arguments> knownBookMultipleLanguagesWithWeightProvider() {
        return Stream.of(
                Arguments.of(
                        "Known book with multiple languages, accepting multiple languages with different weights",
                        builder()
                                .originalLanguage("it")
                                .details(Set.of(
                                        detailsBuilder().language("fr").withWikipediaLink().build(),
                                        detailsBuilder().language("it").withWikipediaLink().build()
                                ))
                                .build(),
                        "fr, it;q=0.5",
                        "fr"
                ),
                Arguments.of(
                        "Known book with multiple languages, accepting multiple languages with different weights",
                        builder()
                                .originalLanguage("fr")
                                .details(Set.of(
                                        detailsBuilder().language("fr").withWikipediaLink().build(),
                                        detailsBuilder().language("it").withWikipediaLink().build()
                                ))
                                .build(),
                        "de, fr;q=0.4, it;q=0.6",
                        "it"
                )
        );
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("knownBookWithWikipediaLinkProvider")
    @DisplayName("provide the book details corresponding to the ID with wikipedia link when available")
    void knownBookWithWikipediaLink(String displayName, Book book, String acceptLanguage, String expectedLanguage) throws Exception {
        when(booksService.getBook(any())).thenReturn(Optional.of(book));

        mvc.perform(get("/api/v1/books/{id}", book.id())
                        .accept("application/hal+json")
                        .header(ACCEPT_LANGUAGE, acceptLanguage)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("_links.wikipedia.href", equalTo(book.wikipediaLinkIn(expectedLanguage).get().value())));

        verify(booksService).getBook(book.id());
    }

    private static Stream<Arguments> knownBookWithWikipediaLinkProvider() {
        return Stream.of(
                Arguments.of(
                        "Known book with multiple languages, each having a wikipedia link",
                        builder()
                                .originalLanguage("it")
                                .details(Set.of(
                                        detailsBuilder().language("fr").withWikipediaLink().build(),
                                        detailsBuilder().language("it").withWikipediaLink().build()
                                ))
                                .build(),
                        "fr, it;q=0.5",
                        "fr"
                ),
                Arguments.of(
                        "Known book with multiple languages, one with higher weight having a wikipedia link",
                        builder()
                                .originalLanguage("fr")
                                .details(Set.of(
                                        detailsBuilder().language("fr").withoutWikipediaLink().build(),
                                        detailsBuilder().language("it").withWikipediaLink().build()
                                ))
                                .build(),
                        "de, fr;q=0.4, it;q=0.6",
                        "it"
                ),
                Arguments.of(
                        "Known book with multiple languages, original one having a wikipedia link",
                        builder()
                                .originalLanguage("fr")
                                .details(Set.of(
                                        detailsBuilder().language("fr").withWikipediaLink().build(),
                                        detailsBuilder().language("it").withoutWikipediaLink().build()
                                ))
                                .build(),
                        "*",
                        "fr"
                )
        );
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("knownBookWithoutWikipediaLinkProvider")
    @DisplayName("provide the book details corresponding to the ID without wikipedia link when not available")
    void knownBookWithoutWikipediaLink(String displayName, Book book, String acceptLanguage, String expectedLanguage) throws Exception {
        when(booksService.getBook(any())).thenReturn(Optional.of(book));

        mvc.perform(get("/api/v1/books/{id}", book.id())
                        .accept("application/hal+json")
                        .header(ACCEPT_LANGUAGE, acceptLanguage)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("_links.wikipedia").doesNotExist());

        verify(booksService).getBook(book.id());
    }

    private static Stream<Arguments> knownBookWithoutWikipediaLinkProvider() {
        return Stream.of(
                Arguments.of(
                        "Known book with multiple languages, none having a wikipedia link",
                        builder()
                                .originalLanguage("it")
                                .details(Set.of(
                                        detailsBuilder().language("fr").withoutWikipediaLink().build(),
                                        detailsBuilder().language("it").withoutWikipediaLink().build()
                                ))
                                .build(),
                        "fr, it;q=0.5",
                        "fr"
                ),
                Arguments.of(
                        "Known book with multiple languages, one with higher weight not having a wikipedia link",
                        builder()
                                .originalLanguage("fr")
                                .details(Set.of(
                                        detailsBuilder().language("fr").withWikipediaLink().build(),
                                        detailsBuilder().language("it").withoutWikipediaLink().build()
                                ))
                                .build(),
                        "de, fr;q=0.4, it;q=0.6",
                        "it"
                ),
                Arguments.of(
                        "Known book with multiple languages, original one having a wikipedia link",
                        builder()
                                .originalLanguage("fr")
                                .details(Set.of(
                                        detailsBuilder().language("fr").withoutWikipediaLink().build(),
                                        detailsBuilder().language("it").withWikipediaLink().build()
                                ))
                                .build(),
                        "*",
                        "fr"
                )
        );
    }

}
