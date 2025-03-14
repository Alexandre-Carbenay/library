package org.adhuc.library.catalog.adapter.rest.editions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.adhuc.library.catalog.books.Book;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Collection;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class EditionsAssertions {

    private static final ObjectMapper MAPPER = new ObjectMapper();

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
                                "?(@.isbn == \"" + book.isbn() + "\" " +
                                "&& @.title == \"" + book.title() + "\" " +
                                "&& @.language == \"" + book.language() + "\" " +
                                "&& @.summary == " + MAPPER.writeValueAsString(book.summary()) + " " +
                                "&& @._links.self.href == \"http://localhost/api/v1/editions/" + book.isbn() + "\")]").exists());
            }
        }
    }

}
