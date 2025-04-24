package org.adhuc.library.catalog.adapter.rest.books;

import org.adhuc.library.catalog.books.Book;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Collection;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class BooksAssertions {

    public static void assertResponseContainsAllEmbeddedBooks(ResultActions result,
                                                              Collection<Book> expectedBooks,
                                                              String expectedLanguage) throws Exception {
        assertResponseContainsAllEmbeddedBooks(result, "books", expectedBooks, expectedLanguage);
    }

    public static void assertResponseContainsAllEmbeddedBooks(ResultActions result,
                                                              String collectionName,
                                                              Collection<Book> expectedBooks,
                                                              String expectedLanguage) throws Exception {
        var embeddedResources = "_embedded." + collectionName;
        if (expectedBooks.isEmpty()) {
            result.andExpect(jsonPath(embeddedResources).doesNotExist());
        } else {
            result.andExpect(jsonPath(embeddedResources).exists())
                    .andExpect(jsonPath(embeddedResources).isArray())
                    .andExpect(jsonPath(embeddedResources, hasSize(expectedBooks.size())));
            for (var book : expectedBooks) {
                result.andExpect(jsonPath(
                        embeddedResources + "[" +
                                "?(@.id == \"" + book.id() + "\" " +
                                "&& @.title == \"" + book.titleIn(expectedLanguage) + "\" " +
                                "&& @.description == \"" + book.descriptionIn(expectedLanguage) + "\" " +
                                "&& @._links.self.href == \"http://localhost/api/v1/books/" + book.id() + "\")]").exists());
            }
        }
    }

    public static void assertResponseContainsAllEmbeddedBooks(ResultActions result,
                                                              String collectionName,
                                                              Collection<Book> expectedBooks) throws Exception {
        var embeddedResources = "_embedded." + collectionName;
        if (expectedBooks.isEmpty()) {
            result.andExpect(jsonPath(embeddedResources).doesNotExist());
        } else {
            result.andExpect(jsonPath(embeddedResources).exists())
                    .andExpect(jsonPath(embeddedResources).isArray())
                    .andExpect(jsonPath(embeddedResources, hasSize(expectedBooks.size())));
            for (var book : expectedBooks) {
                result.andExpect(jsonPath(
                        embeddedResources + "[" +
                                "?(@.id == \"" + book.id() + "\" " +
                                "&& @.title == \"" + book.titleIn(book.originalLanguage()) + "\" " +
                                "&& @.description == \"" + book.descriptionIn(book.originalLanguage()) + "\" " +
                                "&& @._links.self.href == \"http://localhost/api/v1/books/" + book.id() + "\")]").exists());
            }
        }
    }

}
