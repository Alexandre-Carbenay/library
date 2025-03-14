package org.adhuc.library.catalog.adapter.rest.authors;

import org.adhuc.library.catalog.adapter.rest.editions.EditionModelAssembler;
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
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.adhuc.library.catalog.adapter.rest.editions.EditionsAssertions.assertResponseContainsAllEmbeddedBooks;
import static org.adhuc.library.catalog.authors.AuthorsMother.Authors.*;
import static org.adhuc.library.catalog.authors.AuthorsMother.authors;
import static org.adhuc.library.catalog.authors.AuthorsMother.builder;
import static org.adhuc.library.catalog.books.BooksMother.notableBooksOf;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {
        AuthorsController.class,
        AuthorDetailsModelAssembler.class,
        EditionModelAssembler.class
})
@Import(RequestValidationConfiguration.class)
@DisplayName("Authors controller should")
class AuthorsControllerTests {

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private AuthorsService authorsService;
    @MockitoBean
    private BooksService booksService;

    @ParameterizedTest
    @ValueSource(strings = {"123", "invalid"})
    @DisplayName("refuse providing an author with invalid ID")
    void invalidAuthorId(String invalidId) throws Exception {
        mvc.perform(get("/api/v1/authors/{id}", invalidId).accept("application/hal+json"))
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
