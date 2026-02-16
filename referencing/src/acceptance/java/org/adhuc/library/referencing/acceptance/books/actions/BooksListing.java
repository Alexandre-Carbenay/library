package org.adhuc.library.referencing.acceptance.books.actions;

import org.adhuc.library.referencing.acceptance.authors.actions.AuthorsListing;
import org.adhuc.library.referencing.acceptance.books.Book;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static io.restassured.RestAssured.given;

public class BooksListing {

    public static List<Book> listBooks() {
        var books = given()
                .log().ifValidationFails()
                .when()
                .get("/v1/books")
                .then()
                .log().ifError()
                .statusCode(206)
                .extract()
                .body()
                .jsonPath().getList("_embedded.books", BookResponse.class);
        var authors = Objects.requireNonNull(AuthorsListing.listAuthors());
        return books.stream().map(book -> book.toBook(authors)).toList();
    }

}
