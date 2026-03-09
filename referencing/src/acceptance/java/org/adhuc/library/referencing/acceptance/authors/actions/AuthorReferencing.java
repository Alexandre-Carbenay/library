package org.adhuc.library.referencing.acceptance.authors.actions;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.adhuc.library.referencing.acceptance.authors.Author;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static java.time.format.DateTimeFormatter.ISO_DATE;

public class AuthorReferencing {

    public static ValidatableResponse referenceAuthor(String name, LocalDate dateOfBirth) {
        return referenceAuthor(new Author(name, dateOfBirth.format(ISO_DATE)));
    }

    public static ValidatableResponse referenceAuthor(String name, LocalDate dateOfBirth, LocalDate dateOfDeath) {
        return referenceAuthor(new Author(name, dateOfBirth.format(ISO_DATE), dateOfDeath.format(ISO_DATE)));
    }

    public static ValidatableResponse referenceAuthorWithNameOnly(String name) {
        return referenceAuthor("{\"name\": \"" + name + "\"}");
    }

    public static ValidatableResponse referenceAuthorWithDateOfBirthOnly(LocalDate dateOfBirth) {
        return referenceAuthor("{\"date_of_birth\": \"" + dateOfBirth + "\"}");
    }

    private static ValidatableResponse referenceAuthor(Object body) {
        return given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/v1/authors")
                .then();
    }

}
