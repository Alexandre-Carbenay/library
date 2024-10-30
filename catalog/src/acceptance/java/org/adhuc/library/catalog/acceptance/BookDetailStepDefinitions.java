package org.adhuc.library.catalog.acceptance;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.ValidatableResponse;

import java.util.List;
import java.util.Locale;

import static io.restassured.RestAssured.when;
import static org.adhuc.library.catalog.acceptance.assertions.EmbeddedAuthorsAssertions.assertResponseEmbedsAuthors;
import static org.hamcrest.Matchers.equalTo;

@SuppressWarnings("preview")
public class BookDetailStepDefinitions {

    private String isbn;
    private ValidatableResponse response;

    @When("he retrieves the details of a book with ISBN {word}")
    public void retrieveBookDetails(String isbn) {
        this.isbn = isbn;
        response = when().get("/v1/books/{isbn}", isbn)
                .then();
    }

    @Then("the book details cannot be retrieved because it does not exists")
    public void nonExistingBook() {
        response.statusCode(404)
                .contentType("application/problem+json")
                .body("type", equalTo("/problems/unknown-entity"))
                .body("status", equalTo(404))
                .body("title", equalTo("Unknown book"))
                .body("detail", equalTo(STR."No book exists with ISBN '\{isbn}'"));
    }

    @Then("the book details have the expected {string}, {word}, {language}, {authorNames} and {string}")
    public void existingBookDetails(String title, String publicationDate, Locale language, List<String> authorNames, String summary) {
        response.statusCode(200)
                .contentType("application/json")
                .body("isbn", equalTo(isbn))
                .body("title", equalTo(title))
                .body("publication_date", equalTo(publicationDate))
                .body("language", equalTo(language.getLanguage()))
                .body("summary", equalTo(summary));
        assertResponseEmbedsAuthors(response, authorNames, name -> STR."Book \{isbn} details must contain author named \{name}");
    }

}
