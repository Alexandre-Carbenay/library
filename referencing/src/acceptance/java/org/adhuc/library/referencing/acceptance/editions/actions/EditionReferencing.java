package org.adhuc.library.referencing.acceptance.editions.actions;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.adhuc.library.referencing.acceptance.books.Book;
import org.adhuc.library.referencing.acceptance.publishers.Publisher;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static java.util.Optional.ofNullable;

public class EditionReferencing {

    public static ValidatableResponse referenceEdition(@Nullable String isbn, @Nullable String language, @Nullable Book book,
                                                       @Nullable Publisher publisher, @Nullable LocalDate publicationDate) {
        return referenceEdition(isbn, language, book, publisher, publicationDate, null, null);
    }

    public static ValidatableResponse referenceEdition(@Nullable String isbn, @Nullable String language, @Nullable Book book,
                                                       @Nullable Publisher publisher, @Nullable LocalDate publicationDate,
                                                       @Nullable String title, @Nullable String summary) {
        var request = new ReferenceRequest(
                isbn,
                language,
                ofNullable(book).map(Book::id).orElse(null),
                ofNullable(publisher).map(Publisher::id).orElse(null),
                publicationDate,
                title,
                summary
        );
        return given()
                .contentType(ContentType.JSON)
                .body(request)
                .log().ifValidationFails()
                .when()
                .post("/v1/editions")
                .then();
    }

}
