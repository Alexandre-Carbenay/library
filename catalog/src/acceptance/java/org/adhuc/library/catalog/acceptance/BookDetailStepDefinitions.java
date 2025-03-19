package org.adhuc.library.catalog.acceptance;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.ValidatableResponse;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.adhuc.library.catalog.acceptance.assertions.EmbeddedAuthorsAssertions.assertResponseEmbedsAuthors;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

@SuppressWarnings("preview")
public class BookDetailStepDefinitions {

    private UUID bookId;
    private ValidatableResponse response;

    @When("he retrieves the details of a book with ID {uuid} in {language}")
    public void retrieveEditionDetails(UUID bookId, Locale language) {
        this.bookId = bookId;
        response = given()
                .header("Accept-Language", language.getLanguage())
                .when()
                .get("/v1/books/{id}", this.bookId)
                .then();
    }

    @Then("the book details cannot be retrieved because it does not exists")
    public void nonExistingBook() {
        response.statusCode(404)
                .contentType("application/problem+json")
                .body("type", equalTo("/problems/unknown-entity"))
                .body("status", equalTo(404))
                .body("title", equalTo("Unknown book"))
                .body("detail", equalTo(STR."No book exists with id '\{bookId}'"));
    }

    @Then("the book details have the expected {string}, {authorNames}, {string} and wikipedia {word} in the requested {language}")
    public void existingBookDetails(String title, List<String> authorNames, String description, String wikipediaLink, Locale language) {
        response.statusCode(200)
                .contentType("application/json")
                .header("Content-Language", language.getLanguage())
                .body("id", equalTo(bookId.toString()))
                .body("title", equalTo(title))
                .body("description", equalTo(description))
                .body("_links.wikipedia.href", equalTo(wikipediaLink));
        assertResponseEmbedsAuthors(response, authorNames, name -> STR."Book \{bookId} details must contain author named \{name}");
    }

    @Then("the book details have no wikipedia link")
    public void existingBookWithoutWikipediaLink() {
        response.statusCode(200)
                .contentType("application/json")
                .body("_links.wikipedia", nullValue());
    }

    @Then("the book details have default {string} and {string} in {language}")
    public void existingBookDefaultLanguage(String title, String description, Locale language) {
        response.statusCode(200)
                .contentType("application/json")
                .header("Content-Language", language.getLanguage())
                .body("title", equalTo(title))
                .body("description", equalTo(description));
    }

}
