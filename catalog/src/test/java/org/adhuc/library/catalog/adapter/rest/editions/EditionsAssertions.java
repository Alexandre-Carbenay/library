package org.adhuc.library.catalog.adapter.rest.editions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.adhuc.library.catalog.editions.Edition;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Collection;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class EditionsAssertions {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void assertResponseContainsAllEmbeddedEditions(ResultActions result,
                                                                 String collectionName,
                                                                 Collection<Edition> expectedEditions) throws Exception {
        var embeddedResources = "_embedded." + collectionName;
        if (expectedEditions.isEmpty()) {
            result.andExpect(jsonPath(embeddedResources).doesNotExist());
        } else {
            result.andExpect(jsonPath(embeddedResources).exists())
                    .andExpect(jsonPath(embeddedResources).isArray())
                    .andExpect(jsonPath(embeddedResources, hasSize(expectedEditions.size())));
            for (var edition : expectedEditions) {
                result.andExpect(jsonPath(
                        embeddedResources + "[" +
                                "?(@.isbn == \"" + edition.isbn() + "\" " +
                                "&& @.title == \"" + edition.title() + "\" " +
                                "&& @.language == \"" + edition.language() + "\" " +
                                "&& @.summary == " + MAPPER.writeValueAsString(edition.summary()) + " " +
                                "&& @._links.self.href == \"http://localhost/api/v1/editions/" + edition.isbn() + "\")]").exists());
            }
        }
    }

}
