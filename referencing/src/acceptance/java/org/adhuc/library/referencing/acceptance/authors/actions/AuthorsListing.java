package org.adhuc.library.referencing.acceptance.authors.actions;

import org.adhuc.library.referencing.acceptance.authors.Author;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static io.restassured.RestAssured.given;

public class AuthorsListing {

    @Nullable
    public static List<Author> listAuthors() {
        return given()
                .log().ifValidationFails()
                .when()
                .get("/v1/authors")
                .then()
                .log().ifError()
                .statusCode(206)
                .extract()
                .body()
                .jsonPath().getList("_embedded.authors", Author.class);
    }

}
