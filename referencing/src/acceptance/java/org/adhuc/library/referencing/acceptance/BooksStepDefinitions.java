package org.adhuc.library.referencing.acceptance;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.ValidatableResponse;
import org.adhuc.library.referencing.acceptance.authors.AuthorRetriever;
import org.adhuc.library.referencing.acceptance.books.Book;
import org.adhuc.library.referencing.acceptance.books.BookRetriever;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static org.adhuc.library.referencing.acceptance.books.Book.hasWikipediaLink;
import static org.adhuc.library.referencing.acceptance.books.BooksMother.description;
import static org.adhuc.library.referencing.acceptance.books.actions.BookReferencing.referenceBook;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.hamcrest.Matchers.notNullValue;

public class BooksStepDefinitions {

    @Nullable
    private ValidatableResponse response;
    @Nullable
    private Book foundBook;

    @Given("{bookTitle} is not present in the list of books")
    public void bookNotPresentInList(String bookTitle) {
        var bookIsPresent = isBookPresentInList(bookTitle);
        assumeThat(bookIsPresent).as("Book should not be present").isFalse();
    }

    @When("she references new book {bookTitle} written in {language} by {authorName} with a description")
    public void referenceNewBook(String bookTitle, String language, String authorName) {
        var author = AuthorRetriever.findAuthorByName(authorName)
                .orElseThrow(() -> new AssertionError("Author " + authorName + " must exist"));
        response = referenceBook(List.of(author), language, bookTitle, description());
    }

    @Then("book {bookTitle} is referenced")
    public void assertReferencedTitle(String bookTitle) {
        var location = Objects.requireNonNull(response, "Response must have been set before assertion")
                .assertThat()
                .log().ifError()
                .statusCode(201)
                .header("Location", notNullValue())
                .extract().header("Location");
        // TODO assert location is accessible and provides book
    }

    @Then("{bookTitle} is now present in the list of books")
    public void assertBookPresentInList(String bookTitle) {
        var bookIsPresent = isBookPresentInList(bookTitle);
        assertThat(bookIsPresent).as("Book should be present after referencing").isTrue();
    }

    @Then("{bookTitle} does not have a wikipedia link")
    public void assertBookWithNoWikipediaLink(String bookTitle) {
        assertThat(foundBook).isNotNull()
                .as("Book should not have wikipedia link for any detail").doesNotMatch(hasWikipediaLink());
    }

    private boolean isBookPresentInList(String bookTitle) {
        var book = BookRetriever.findBookByTitle(bookTitle);
        foundBook = book.orElse(null);
        return BookRetriever.findBookByTitle(bookTitle).isPresent();
    }

}
