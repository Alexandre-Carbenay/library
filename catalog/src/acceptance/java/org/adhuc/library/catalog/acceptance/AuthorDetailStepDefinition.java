package org.adhuc.library.catalog.acceptance;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.ValidatableResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class AuthorDetailStepDefinition {

    private UUID authorId;
    private ValidatableResponse response;

    @When("he retrieves the details of an author with ID {uuid}")
    public void retrieveAuthorDetails(UUID authorId) {
        this.authorId = authorId;
        response = when().get("/v1/authors/{id}", authorId)
                .then();
    }

    @When("he retrieves the details of an author with ID {uuid} in {language}")
    public void retrieveAuthorDetails(UUID authorId, Locale language) {
        this.authorId = authorId;
        response = given().header("Accept-Language", language.getLanguage())
                .when().get("/v1/authors/{id}", authorId)
                .then();
    }

    @When("he retrieves the details of an author with ID {uuid} in {string}")
    public void retrieveAuthorDetails(UUID authorId, String acceptLanguages) {
        this.authorId = authorId;
        response = given().header("Accept-Language", acceptLanguages)
                .when().get("/v1/authors/{id}", authorId)
                .then();
    }

    @Then("the author details cannot be retrieved because it does not exists")
    public void nonExistingAuthor() {
        response.statusCode(404)
                .contentType("application/problem+json")
                .body("type", equalTo("/problems/unknown-entity"))
                .body("status", equalTo(404))
                .body("title", equalTo("Unknown author"))
                .body("detail", equalTo("No author exists with id '" + authorId + "'"));
    }

    @Then("the author details have the expected {string}, {date}, {date} and authored notable {titles}")
    public void existingAuthorDetails(String name, LocalDate dateOfBirth, LocalDate dateOfDeath, List<String> bookTitles) {
        response.statusCode(200)
                .contentType("application/json")
                .body("id", equalTo(authorId.toString()))
                .body("name", equalTo(name))
                .body("date_of_birth", equalTo(dateOfBirth.toString()));
        if (dateOfDeath != null) {
            response.body("date_of_death", equalTo(dateOfDeath.toString()));
        } else {
            response.body("date_of_death", nullValue());
        }
        bookTitles.forEach(bookId ->
                response.body("_embedded.notable_books.find { it.title == \"%s\" }", withArgs(bookId),
                        describedAs("Author " + authorId + " details must contain book with title " + bookId, notNullValue()))
        );
    }

}
