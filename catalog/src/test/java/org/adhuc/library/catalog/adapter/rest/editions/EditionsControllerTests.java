package org.adhuc.library.catalog.adapter.rest.editions;

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
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
        EditionsController.class,
        EditionModelAssembler.class,
        EditionDetailsModelAssembler.class,
        AuthorModelAssembler.class
})
@Import(RequestValidationConfiguration.class)
@DisplayName("Editions controller should")
class EditionsControllerTests {

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private BooksService booksService;

    @ParameterizedTest
    @ValueSource(strings = {"123", "invalid"})
    @DisplayName("refuse providing an edition with invalid ISBN")
    void invalidEditionId(String invalidIsbn) throws Exception {
        mvc.perform(get("/api/v1/editions/{isbn}", invalidIsbn).accept("application/hal+json"))
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
    @DisplayName("not find an edition corresponding to an unknown ISBN")
    void unknownEditionId() throws Exception {
        var unknownIsbn = "9782081275232";
        mvc.perform(get("/api/v1/editions/{id}", unknownIsbn).accept("application/hal+json"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("type", equalTo("/problems/unknown-entity")))
                .andExpect(jsonPath("status", equalTo(404)))
                .andExpect(jsonPath("title", equalTo("Unknown edition")))
                .andExpect(jsonPath("detail", allOf(
                        startsWith("No edition exists with ISBN"),
                        containsString(unknownIsbn))
                ));
    }

    @ParameterizedTest
    @MethodSource({
            "editionsWithExactPublicationDateProvider",
            "editionsWithYearADPublicationDateProvider",
            "editionsWithYearBCPublicationDateProvider"
    })
    @DisplayName("provide the edition details corresponding to the ID")
    void knownEditionId(Book book) throws Exception {
        when(booksService.getBook(Mockito.any())).thenReturn(Optional.of(book));

        var result = mvc.perform(get("/api/v1/editions/{isbn}", book.isbn()).accept("application/hal+json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("isbn", equalTo(book.isbn())))
                .andExpect(jsonPath("title", equalTo(book.title())))
                .andExpect(jsonPath("publication_date", equalTo(book.publicationDate().toString())))
                .andExpect(jsonPath("language", equalTo(book.language())))
                .andExpect(jsonPath("summary", equalTo(book.summary())))
                .andExpect(jsonPath("_links.self.href", equalTo(STR."http://localhost/api/v1/editions/\{book.isbn()}")));

        assertResponseContainsAllEmbeddedAuthors(result, book.authors());

        verify(booksService).getBook(book.isbn());
    }

    static Stream<Arguments> editionsWithExactPublicationDateProvider() {
        return editionsWithPublicationDateProvider(Books.exactPublicationDates());
    }

    static Stream<Arguments> editionsWithYearADPublicationDateProvider() {
        return editionsWithPublicationDateProvider(Books.yearADPublicationDates());
    }

    static Stream<Arguments> editionsWithYearBCPublicationDateProvider() {
        return editionsWithPublicationDateProvider(Books.yearBCPublicationDates());
    }

    static Stream<Arguments> editionsWithPublicationDateProvider(Arbitrary<PublicationDate> publicationDateArbitrary) {
        return publicationDateArbitrary
                .map(publicationDate -> BooksMother.builder().publicationDate(publicationDate).build())
                .map(Arguments::of)
                .sampleStream().limit(3);
    }

}
