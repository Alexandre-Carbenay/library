package org.adhuc.library.referencing.acceptance.books.actions;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.adhuc.library.referencing.acceptance.authors.Author;

import java.util.List;

import static io.restassured.RestAssured.given;

public class BookReferencing {

    public static ValidatableResponse referenceBook(List<Author> authors, String language, String title, String description) {
        return given()
                .contentType(ContentType.JSON)
                .body(new ReferenceRequest(authors, language, title, description))
                .log().ifValidationFails()
                .when()
                .post("/v1/books")
                .then();
    }

}
