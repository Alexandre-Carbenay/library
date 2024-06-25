package org.adhuc.library.catalog.adapter.rest.authors;

import org.adhuc.library.catalog.authors.Author;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Collection;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class AuthorsAssertions {

    public static void assertResponseContainsAllEmbeddedAuthors(ResultActions result, Collection<Author> expectedAuthors) throws Exception {
        result.andExpect(jsonPath("_embedded.authors").exists())
                .andExpect(jsonPath("_embedded.authors").isArray())
                .andExpect(jsonPath("_embedded.authors", hasSize(expectedAuthors.size())));
        for (var author : expectedAuthors) {
            result.andExpect(jsonPath(
                    "_embedded.authors.[" +
                            "?(@.id == \"" + author.id() + "\" " +
                            "&& @.name == \"" + author.name() + "\" " +
                            "&& @._links.self.href == \"http://localhost/api/v1/authors/" + author.id() + "\")]").exists());
        }
    }

}
