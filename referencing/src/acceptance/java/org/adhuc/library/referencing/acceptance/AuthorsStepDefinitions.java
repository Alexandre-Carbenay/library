package org.adhuc.library.referencing.acceptance;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.ValidatableResponse;
import org.adhuc.library.referencing.acceptance.authors.Author;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.adhuc.library.referencing.acceptance.authors.actions.AuthorReferencing.referenceAuthor;
import static org.adhuc.library.referencing.acceptance.authors.actions.AuthorReferencing.referenceAuthorWithNameOnly;
import static org.adhuc.library.referencing.acceptance.authors.actions.AuthorsListing.listAuthors;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.hamcrest.Matchers.*;

public class AuthorsStepDefinitions {

    @Nullable
    private ValidatableResponse response;

    @Given("{authorName} is not present in the list of authors")
    public void authorNotPresentInList(String authorName) {
        var authors = listAuthors();
        var authorIsPresent = isAuthorPresentInList(authors, authorName);
        assumeThat(authorIsPresent).as("Author should not be present").isFalse();
    }

    @Given("{authorName} born on {date} and dead on {date} is present in the list of authors")
    public void authorPresentInList(String authorName, LocalDate dateOfBirth, LocalDate dateOfDeath) {
        var authors = listAuthors();
        var authorIsPresent = isAuthorPresentInList(authors, authorName);
        if (!authorIsPresent) {
            response = referenceAuthor(authorName, dateOfBirth, dateOfDeath);
            response.statusCode(201);
        }
    }

    @When("she references new author {authorName}")
    public void referenceNewAuthorOnlyName(String authorName) {
        response = referenceAuthorWithNameOnly(authorName);
    }

    @When("she references new author {authorName} born on {date}")
    public void referenceNewAuthorBornExactDate(String authorName, LocalDate dateOfBirth) {
        response = referenceAuthor(authorName, dateOfBirth);
    }

    @When("she references new author {authorName} born on {year}")
    public void referenceNewAuthorBornYearDate(String authorName, int yearOfBirth) {
    }

    @When("she references new author {authorName} born on {date} and dead on {date}")
    public void referenceNewDeadAuthorBornExactDate(String authorName, LocalDate dateOfBirth, LocalDate dateOfDeath) {
        response = referenceAuthor(authorName, dateOfBirth, dateOfDeath);
    }

    @Then("{authorName} is referenced")
    public void assertReferencedAuthor(String authorName) {
        var location = Objects.requireNonNull(response, "Response must have been set before assertion")
                .assertThat()
                .log().ifError()
                .statusCode(201)
                .header("Location", notNullValue())
                .extract().header("Location");
        // TODO assert location is accessible and provides author
    }

    @Then("the referencing fails with date of birth required")
    public void assertReferenceFailedMissingDateOfBirth() {
        Objects.requireNonNull(response, "Response must have been set before assertion")
                .assertThat()
                .log().ifStatusCodeMatches(not(equalTo(400)))
                .statusCode(400)
                .header("Location", nullValue())
                .body("type", equalTo("/problems/invalid-request"))
                .body("title", equalTo("Request validation error"))
                .body("errors[0].detail", equalTo("Missing required property"))
                .body("errors[0].pointer", equalTo("/date_of_birth"));
    }

    @Then("the referencing fails with invalid date of death {date} being before date of birth {date}")
    public void assertReferenceFailedInvalidDateOfDeath(LocalDate dateOfDeath, LocalDate dateOfBirth) {
        Objects.requireNonNull(response, "Response must have been set before assertion")
                .assertThat()
                .log().ifStatusCodeMatches(not(equalTo(400)))
                .statusCode(400)
                .header("Location", nullValue())
                .body("type", equalTo("/problems/invalid-request"))
                .body("title", equalTo("Request validation error"))
                .body("errors[0].detail", equalTo("Date of death " + dateOfDeath + " must be after date of birth " + dateOfBirth))
                .body("errors[0].pointer", equalTo("/date_of_death"));
    }

    @Then("{authorName} is now present in the list of authors")
    public void assertAuthorPresentInList(String authorName) {
        var authors = listAuthors();
        var authorIsPresent = isAuthorPresentInList(authors, authorName);
        assertThat(authorIsPresent).as("Author should be present after referencing").isTrue();
    }

    @Then("{authorName} is still not present in the list of authors")
    public void assertAuthorNotPresentInList(String authorName) {
        var authors = listAuthors();
        var authorIsPresent = isAuthorPresentInList(authors, authorName);
        assertThat(authorIsPresent).as("Author should not be present after referencing").isFalse();
    }

    @Then("{authorName} is present twice in the list of authors")
    public void assertAuthorDuplicateInList(String authorName) {
        var authors = listAuthors();
        var numberOfOccurrences = authorOccurrencesInList(authors, authorName);
        assertThat(numberOfOccurrences).isEqualTo(2L);
    }

    private boolean isAuthorPresentInList(@Nullable List<Author> authors, String authorName) {
        return authors != null && !authors.isEmpty() && authors.stream().anyMatch(author -> author.hasName(authorName));
    }

    private long authorOccurrencesInList(@Nullable List<Author> authors, String authorName) {
        return authors == null ? 0 : authors.stream().filter(author -> author.hasName(authorName)).count();
    }

}
