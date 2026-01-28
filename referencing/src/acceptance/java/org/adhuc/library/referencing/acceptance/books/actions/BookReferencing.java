package org.adhuc.library.referencing.acceptance.books.actions;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.adhuc.library.referencing.acceptance.authors.Author;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static io.restassured.RestAssured.given;

public class BookReferencing {

    public static ValidatableResponse referenceBook(@Nullable List<Author> authors,
                                                    @Nullable String language,
                                                    @Nullable String title,
                                                    @Nullable String description) {
        return referenceBook(authors, language, language, title, description);
    }

    public static ValidatableResponse referenceBook(@Nullable List<Author> authors,
                                                    @Nullable String originalLanguage,
                                                    @Nullable String detailLanguage,
                                                    @Nullable String title,
                                                    @Nullable String description) {
        return referenceBook(new ReferenceRequest(authors, originalLanguage, detailLanguage, title, description));
    }

    public static ValidatableResponse referenceBook(ReferenceRequest referenceRequest) {
        return given()
                .contentType(ContentType.JSON)
                .body(referenceRequest)
                .log().ifValidationFails()
                .when()
                .post("/v1/books")
                .then();
    }

}
