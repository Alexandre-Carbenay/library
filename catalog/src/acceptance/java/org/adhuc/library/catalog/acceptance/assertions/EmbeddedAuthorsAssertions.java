package org.adhuc.library.catalog.acceptance.assertions;

import io.restassured.response.ValidatableResponse;

import java.util.List;
import java.util.function.Function;

import static io.restassured.RestAssured.withArgs;
import static org.hamcrest.Matchers.*;

public class EmbeddedAuthorsAssertions {

    public static void assertResponseEmbedsAuthors(ValidatableResponse response,
                                                   List<String> authorNames,
                                                   Function<String, String> assertionDescription) {
        response.body("_embedded.authors", hasSize(authorNames.size()));
        authorNames.forEach(name ->
                response.body("_embedded.authors.find { it.name == '%s' }", withArgs(name),
                        describedAs(assertionDescription.apply(name), notNullValue()))
        );
    }

}
