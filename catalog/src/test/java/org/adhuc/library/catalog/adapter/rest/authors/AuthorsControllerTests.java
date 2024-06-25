package org.adhuc.library.catalog.adapter.rest.authors;

import org.adhuc.library.catalog.adapter.rest.RequestValidationConfiguration;
import org.adhuc.library.catalog.adapter.rest.books.BookModelAssembler;
import org.adhuc.library.catalog.authors.Author;
import org.adhuc.library.catalog.authors.AuthorsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.adhuc.library.catalog.adapter.rest.books.BooksAssertions.assertResponseContainsAllEmbeddedBooks;
import static org.adhuc.library.catalog.authors.AuthorsMother.Authors.*;
import static org.adhuc.library.catalog.authors.AuthorsMother.builder;
import static org.adhuc.library.catalog.books.BooksMother.books;
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
            "authorWithNoNotableBookProvider"
    })
    @DisplayName("provide the author details corresponding to the ID")
    void knownAuthorId(Author author) throws Exception {
        when(authorsService.getAuthor(Mockito.any())).thenReturn(Optional.of(author));

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

        assertResponseContainsAllEmbeddedBooks(result, "notable_books", author.notableBooks());

        verify(authorsService).getAuthor(author.id());
    }

    static Stream<Arguments> aliveAuthorProvider() {
        return aliveDatesOfBirth()
                .map(dateOfBirth -> builder().dateOfBirth(dateOfBirth).dateOfDeath(null).build())
                .map(Arguments::of)
                .sampleStream().limit(3);
    }

    static Stream<Arguments> deadAuthorProvider() {
        return deadDatesOfBirth()
                .flatMap(dateOfBirth -> deadDatesOfDeath(dateOfBirth)
                        .map(dateOfDeath -> builder().dateOfBirth(dateOfBirth).dateOfDeath(dateOfDeath).build()))
                .map(Arguments::of)
                .sampleStream().limit(3);
    }

    static Stream<Arguments> authorWithNoNotableBookProvider() {
        return books().list().ofMinSize(1).ofMaxSize(10)
                .map(notableBooks -> builder().notableBooks(notableBooks).build())
                .map(Arguments::of)
                .sampleStream().limit(3);
    }

}
