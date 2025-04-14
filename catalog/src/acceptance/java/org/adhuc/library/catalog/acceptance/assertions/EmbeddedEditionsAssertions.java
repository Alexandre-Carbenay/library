package org.adhuc.library.catalog.acceptance.assertions;

import io.restassured.response.ValidatableResponse;

import java.util.List;
import java.util.function.Function;

import static io.restassured.RestAssured.withArgs;
import static org.hamcrest.Matchers.*;

public class EmbeddedEditionsAssertions {

    public static void assertResponseEmbedsEditions(ValidatableResponse response,
                                                    List<String> editionIsbns,
                                                    Function<String, String> assertionDescription) {
        response.body("_embedded.editions", hasSize(editionIsbns.size()));
        editionIsbns.forEach(isbn ->
                response.body("_embedded.editions.find { it.isbn == '%s' }", withArgs(isbn),
                        describedAs(assertionDescription.apply(isbn), notNullValue()))
        );
    }

}
