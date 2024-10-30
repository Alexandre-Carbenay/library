package org.adhuc.library.catalog.acceptance;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.ValidatableResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.when;
import static io.restassured.RestAssured.withArgs;
import static org.hamcrest.Matchers.*;

@SuppressWarnings("preview")
public class AuthorDetailStepDefinition {

    private UUID authorId;
    private ValidatableResponse response;

    @When("he retrieves the details of an author with ID {uuid}")
    public void retrieveBookDetails(UUID authorId) {
        this.authorId = authorId;
        response = when().get("/v1/authors/{id}", authorId)
                .then();
    }

    @Then("the author details cannot be retrieved because it does not exists")
    public void nonExistingAuthor() {
        response.statusCode(404)
                .contentType("application/problem+json")
                .body("type", equalTo("/problems/unknown-entity"))
                .body("status", equalTo(404))
                .body("title", equalTo("Unknown author"))
                .body("detail", equalTo(STR."No author exists with id '\{authorId}'"));
    }

    @Then("the author details have the expected {string}, {date}, {date} and authored notable {isbns}")
    public void existingAuthorDetails(String name, LocalDate dateOfBirth, LocalDate dateOfDeath, List<String> bookIsbns) {
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
        bookIsbns.forEach(isbn ->
                response.body("_embedded.notable_books.find { it.isbn == '%s' }", withArgs(isbn),
                        describedAs(STR."Author \{authorId} details must contain book with ISBN \{isbn}", notNullValue()))
        );
    }

}
