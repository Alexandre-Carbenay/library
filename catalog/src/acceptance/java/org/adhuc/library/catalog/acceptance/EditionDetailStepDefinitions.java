package org.adhuc.library.catalog.acceptance;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.ValidatableResponse;

import java.util.Locale;
import java.util.UUID;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;

public class EditionDetailStepDefinitions {

    private String isbn;
    private ValidatableResponse response;

    @When("he retrieves the details of an edition with ISBN {word}")
    public void retrieveEditionDetails(String isbn) {
        this.isbn = isbn;
        response = when().get("/v1/editions/{isbn}", isbn)
                .then();
    }

    @Then("the edition details cannot be retrieved because it does not exists")
    public void nonExistingEdition() {
        response.statusCode(404)
                .contentType("application/problem+json")
                .body("type", equalTo("/problems/unknown-entity"))
                .body("status", equalTo(404))
                .body("title", equalTo("Unknown edition"))
                .body("detail", equalTo("No edition exists with ISBN '" + isbn + "'"));
    }

    @Then("the edition details have the expected {string}, {word}, {language}, {string} and related {uuid}")
    public void existingEditionDetails(String title, String publicationDate, Locale language, String summary, UUID bookId) {
        response.statusCode(200)
                .contentType("application/json")
                .body("isbn", equalTo(isbn))
                .body("title", equalTo(title))
                .body("publication_date", equalTo(publicationDate))
                .body("language", equalTo(language.getLanguage()))
                .body("summary", equalTo(summary))
                .body("_links.book.href", endsWith("/books/" + bookId.toString()));
    }

}
