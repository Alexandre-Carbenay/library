package org.adhuc.library.catalog.adapter.rest.authors;

import org.adhuc.library.catalog.adapter.rest.books.BookModelAssembler;
import org.adhuc.library.catalog.adapter.rest.support.validation.openapi.RequestValidationConfiguration;
import org.adhuc.library.catalog.authors.Author;
import org.adhuc.library.catalog.authors.AuthorsService;
import org.adhuc.library.catalog.books.Book;
import org.adhuc.library.catalog.books.BooksService;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.adhuc.library.catalog.adapter.rest.books.BooksAssertions.assertResponseContainsAllEmbeddedBooks;
import static org.adhuc.library.catalog.authors.AuthorsMother.Authors.*;
import static org.adhuc.library.catalog.authors.AuthorsMother.authors;
import static org.adhuc.library.catalog.authors.AuthorsMother.builder;
import static org.adhuc.library.catalog.books.BooksMother.notableBooksOf;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        AuthorsController.class,
        AuthorDetailsModelAssembler.class,
        BookModelAssembler.class
})
@Import(RequestValidationConfiguration.class)
@DisplayName("Authors controller should")
class AuthorsControllerTests {

    @Autowired
    private MockMvc mvc;
    @MockBean
    private AuthorsService authorsService;
    @MockBean
    private BooksService booksService;

    @ParameterizedTest
    @ValueSource(strings = {"123", "invalid"})
    @DisplayName("refuse providing an author with invalid ID")
    void invalidAuthorId(String invalidId) throws Exception {
        mvc.perform(get("/api/v1/authors/{id}", invalidId).accept("application/hal+json"))
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
    @DisplayName("not find an author corresponding to an unknown ID")
    void unknownAuthorId() throws Exception {
        var unknownId = UUID.randomUUID();
        mvc.perform(get("/api/v1/authors/{id}", unknownId).accept("application/hal+json"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("timestamp").exists())
                .andExpect(jsonPath("status", equalTo(404)))
                .andExpect(jsonPath("error", equalTo("ENTITY_NOT_FOUND")))
                .andExpect(jsonPath("description", allOf(
                        startsWith("No author exists with id"),
                        containsString(unknownId.toString()))
                ));
    }

    @ParameterizedTest
    @MethodSource({
            "aliveAuthorProvider",
            "deadAuthorProvider",
            "authorWithNotableBookProvider"
    })
    @DisplayName("provide the author details corresponding to the ID")
    void knownAuthorId(Author author, List<Book> notableBooks) throws Exception {
        when(authorsService.getAuthor(Mockito.any())).thenReturn(Optional.of(author));
        when(booksService.getNotableBooks(author.id())).thenReturn(notableBooks);

        var result = mvc.perform(get("/api/v1/authors/{id}", author.id()).accept("application/hal+json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", equalTo(author.id().toString())))
                .andExpect(jsonPath("name", equalTo(author.name())))
                .andExpect(jsonPath("date_of_birth", equalTo(author.dateOfBirth().toString())))
                .andExpect(jsonPath("_links.self.href", equalTo("http://localhost/api/v1/authors/" + author.id())));
        if (author.dateOfDeath() != null) {
            result.andExpect(jsonPath("date_of_death", equalTo(author.dateOfDeath().toString())));
        } else {
            result.andExpect(jsonPath("date_of_death").doesNotExist());
        }

        assertResponseContainsAllEmbeddedBooks(result, "notable_books", notableBooks);

        verify(authorsService).getAuthor(author.id());
    }

    static Stream<Arguments> aliveAuthorProvider() {
        return aliveDatesOfBirth()
                .map(dateOfBirth -> builder().dateOfBirth(dateOfBirth).dateOfDeath(null).build())
                .map(author -> Arguments.of(author, List.of()))
                .sampleStream().limit(3);
    }

    static Stream<Arguments> deadAuthorProvider() {
        return deadDatesOfBirth()
                .flatMap(dateOfBirth -> deadDatesOfDeath(dateOfBirth)
                        .map(dateOfDeath -> builder().dateOfBirth(dateOfBirth).dateOfDeath(dateOfDeath).build()))
                .map(author -> Arguments.of(author, List.of()))
                .sampleStream().limit(3);
    }

    static Stream<Arguments> authorWithNotableBookProvider() {
        return authors().flatMap(author -> notableBooksOf(author.id()).list().ofMinSize(1).ofMaxSize(10)
                        .map(notableBooks -> Arguments.of(author, notableBooks))
                )
                .sampleStream().limit(3);
    }

}
