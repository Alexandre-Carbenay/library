package org.adhuc.library.catalog.adapter.rest.books;

import org.adhuc.library.catalog.adapter.rest.RestAdapterTestConfiguration;
import org.adhuc.library.catalog.adapter.rest.authors.AuthorModelAssembler;
import org.adhuc.library.catalog.adapter.rest.editions.EditionModelAssembler;
import org.adhuc.library.catalog.books.Book;
import org.adhuc.library.catalog.books.BooksService;
import org.adhuc.library.catalog.editions.Edition;
import org.adhuc.library.catalog.editions.EditionsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.adhuc.library.catalog.adapter.rest.authors.AuthorsAssertions.assertResponseContainsAllEmbeddedAuthors;
import static org.adhuc.library.catalog.adapter.rest.editions.EditionsAssertions.assertResponseContainsAllEmbeddedEditions;
import static org.adhuc.library.catalog.books.BooksMother.builder;
import static org.adhuc.library.catalog.books.BooksMother.detailsBuilder;
import static org.adhuc.library.catalog.editions.EditionsMother.editionsOf;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE;
import static org.springframework.http.HttpHeaders.CONTENT_LANGUAGE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings({"preview", "NotNullFieldNotInitialized"})
@Tag("integration")
@Tag("restApi")
@WebMvcTest(controllers = {
        BooksController.class,
        BookDetailsModelAssembler.class,
        AuthorModelAssembler.class,
        EditionModelAssembler.class
})
@Import(RestAdapterTestConfiguration.class)
@DisplayName("Books controller should")
class BooksControllerTests {

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private BooksService booksService;
    @MockitoBean
    private EditionsService editionsService;

    @ParameterizedTest
    @MethodSource("invalidBookIdProvider")
    @DisplayName("refuse providing a book with invalid ID")
    void invalidBookId(String invalidId, String errorDetail) throws Exception {
        mvc.perform(get("/api/v1/books/{id}", invalidId).accept("application/hal+json"))
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
        verifyNoInteractions(booksService, editionsService);
    }

    static Stream<Arguments> invalidBookIdProvider() {
        return Stream.of(
                Arguments.of("123", "Input string \"123\" is not a valid UUID"),
                Arguments.of("invalid", "Input string \"invalid\" is not a valid UUID")
        );
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
        verifyNoInteractions(editionsService);
    }

    @Test
    @DisplayName("provide the book details corresponding to the ID, without any edition")
    void knownBookIdWithoutAcceptLanguage() throws Exception {
        var book = builder()
                .originalLanguage("fr")
                .details(detailsBuilder().language("fr").withWikipediaLink().build())
                .build();

        when(booksService.getBook(any())).thenReturn(Optional.of(book));
        when(editionsService.getBookEditions(any())).thenReturn(List.of());

        var result = mvc.perform(get("/api/v1/books/{id}", book.id())
                        .accept("application/hal+json")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", equalTo(book.id().toString())))
                .andExpect(jsonPath("title", equalTo(book.titleIn(book.originalLanguage()))))
                .andExpect(jsonPath("description", equalTo(book.descriptionIn(book.originalLanguage()))))
                .andExpect(jsonPath("_links.self.href", equalTo(STR."http://localhost/api/v1/books/\{book.id()}")))
                .andExpect(jsonPath("_embedded.editions").doesNotExist());

        assertResponseContainsAllEmbeddedAuthors(result, book.authors());

        verify(booksService).getBook(book.id());
        verify(editionsService).getBookEditions(book.id());
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource({
            "knownBookSingleLanguageProvider",
            "knownBookMultipleLanguagesProvider"
    })
    @DisplayName("provide the book details corresponding to the ID, without accept language header")
    void knownBookIdWithoutAcceptLanguage(String displayName, Book book, List<Edition> editions, String expectedLanguage) throws Exception {
        when(booksService.getBook(any())).thenReturn(Optional.of(book));
        when(editionsService.getBookEditions(any())).thenReturn(editions);

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
        assertResponseContainsAllEmbeddedEditions(result, editions);

        verify(booksService).getBook(book.id());
        verify(editionsService).getBookEditions(book.id());
    }

    private static Stream<Arguments> knownBookSingleLanguageProvider() {
        var book = builder()
                .originalLanguage("fr")
                .details(detailsBuilder().language("fr").withWikipediaLink().build())
                .build();
        var editions = editionsOf(book);
        return Stream.of(Arguments.of("Known book with only one language", book, editions, "fr"));
    }

    private static Stream<Arguments> knownBookMultipleLanguagesProvider() {
        var book = builder()
                .originalLanguage("it")
                .details(Set.of(
                        detailsBuilder().language("fr").withWikipediaLink().build(),
                        detailsBuilder().language("it").withWikipediaLink().build()
                ))
                .build();
        var editions = editionsOf(book);
        return Stream.of(Arguments.of("Known book with multiple languages", book, editions, "it"));
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
    void knownBookId(String displayName, Book book, List<Edition> editions, String acceptLanguage, String expectedLanguage) throws Exception {
        when(booksService.getBook(any())).thenReturn(Optional.of(book));
        when(editionsService.getBookEditions(any())).thenReturn(editions);

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
        assertResponseContainsAllEmbeddedEditions(result, editions);

        verify(booksService).getBook(book.id());
        verify(editionsService).getBookEditions(book.id());
    }

    private static Stream<Arguments> knownBookSingleOriginalLanguageProvider() {
        var book = builder()
                .originalLanguage("fr")
                .details(detailsBuilder().language("fr").withWikipediaLink().build())
                .build();
        var editions = editionsOf(book);
        return Stream.of(Arguments.of("Known book with only one language, accepting the original language", book, editions, "fr", "fr"));
    }

    private static Stream<Arguments> knownBookMultipleOriginalLanguageProvider() {
        var book = builder()
                .originalLanguage("fr")
                .details(Set.of(
                        detailsBuilder().language("fr").withWikipediaLink().build(),
                        detailsBuilder().language("it").withWikipediaLink().build()
                ))
                .build();
        var editions = editionsOf(book);
        return Stream.of(Arguments.of("Known book with multiple languages, accepting the original language", book, editions, "fr", "fr"));
    }

    private static Stream<Arguments> knownBookMultipleOtherLanguageProvider() {
        var book = builder()
                .originalLanguage("it")
                .details(Set.of(
                        detailsBuilder().language("fr").withWikipediaLink().build(),
                        detailsBuilder().language("it").withWikipediaLink().build()
                ))
                .build();
        var editions = editionsOf(book);
        return Stream.of(Arguments.of("Known book with multiple languages, accepting a language different from the original one", book, editions, "fr", "fr"));
    }

    private static Stream<Arguments> knownBookUnknownLanguageProvider() {
        var book = builder()
                .originalLanguage("it")
                .details(Set.of(
                        detailsBuilder().language("fr").withWikipediaLink().build(),
                        detailsBuilder().language("it").withWikipediaLink().build()
                ))
                .build();
        var editions = editionsOf(book);
        return Stream.of(Arguments.of("Known book with multiple languages, accepting an unknown language", book, editions, "de", "it"));
    }

    private static Stream<Arguments> knownBookMultipleLanguagesWithWeightProvider() {
        var book1 = builder()
                .originalLanguage("it")
                .details(Set.of(
                        detailsBuilder().language("fr").withWikipediaLink().build(),
                        detailsBuilder().language("it").withWikipediaLink().build()
                ))
                .build();
        var editions1 = editionsOf(book1);
        var book2 = builder()
                .originalLanguage("fr")
                .details(Set.of(
                        detailsBuilder().language("fr").withWikipediaLink().build(),
                        detailsBuilder().language("it").withWikipediaLink().build()
                ))
                .build();
        var editions2 = editionsOf(book2);
        return Stream.of(
                Arguments.of("Known book with multiple languages, accepting multiple languages with different weights", book1, editions1, "fr, it;q=0.5", "fr"),
                Arguments.of("Known book with multiple languages, accepting multiple languages with different weights", book2, editions2, "de, fr;q=0.4, it;q=0.6", "it")
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
                .andExpect(jsonPath("_links.wikipedia.href", equalTo(book.wikipediaLinkIn(expectedLanguage).orElseThrow().value())));

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
    void knownBookWithoutWikipediaLink(String displayName, Book book, String acceptLanguage) throws Exception {
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
