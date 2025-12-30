package org.adhuc.library.referencing.acceptance.authors.actions;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.adhuc.library.referencing.acceptance.authors.Author;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static java.time.format.DateTimeFormatter.ISO_DATE;

public class AuthorReferencing {

    public static ValidatableResponse referenceAuthor(String name, LocalDate dateOfBirth) {
        return given()
                .contentType(ContentType.JSON)
                .body(new Author(name, dateOfBirth.format(ISO_DATE)))
                .log().ifValidationFails()
                .when()
                .post("/v1/authors")
                .then();
    }

    public static ValidatableResponse referenceAuthor(String name, LocalDate dateOfBirth, LocalDate dateOfDeath) {
        return given()
                .contentType(ContentType.JSON)
                .body(new Author(name, dateOfBirth.format(ISO_DATE), dateOfDeath.format(ISO_DATE)))
                .log().ifValidationFails()
                .when()
                .post("/v1/authors")
                .then();
    }

    public static ValidatableResponse referenceAuthorWithNameOnly(String name) {
        return given()
                .contentType(ContentType.JSON)
                .body("{\"name\": \"" + name + "\"}")
                .when()
                .post("/v1/authors")
                .then();
    }

}
