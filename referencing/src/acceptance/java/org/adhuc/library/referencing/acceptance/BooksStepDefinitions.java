package org.adhuc.library.referencing.acceptance;

import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.ValidatableResponse;
import org.adhuc.library.referencing.acceptance.authors.Author;
import org.adhuc.library.referencing.acceptance.authors.AuthorRetriever;
import org.adhuc.library.referencing.acceptance.books.Book;
import org.adhuc.library.referencing.acceptance.books.BookRetriever;
import org.adhuc.library.referencing.acceptance.books.actions.ReferenceRequest;
import org.adhuc.library.referencing.acceptance.books.actions.ReferenceRequest.ReferenceDetail;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.adhuc.library.referencing.acceptance.books.Book.hasWikipediaLink;
import static org.adhuc.library.referencing.acceptance.books.BooksMother.description;
import static org.adhuc.library.referencing.acceptance.books.actions.BookReferencing.referenceBook;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.hamcrest.Matchers.*;

public class BooksStepDefinitions {

    @Nullable
    private String referenceTitle;

    @Nullable
    private ValidatableResponse response;
    @Nullable
    private Book foundBook;

    @Given("{bookTitle} is not present in the list of books")
    public void bookNotPresentInList(String bookTitle) {
        var bookIsPresent = isBookPresentInList(bookTitle);
        assumeThat(bookIsPresent).as("Book should not be present").isFalse();
    }

    @Given("{bookTitle} written in {language} by {authorName} is present in the list of books")
    public void bookPresentInList(String bookTitle, String language, String authorName) {
        var bookIsPresent = isBookPresentInList(bookTitle);
        if (!bookIsPresent) {
            var author = getAuthor(authorName);
            response = referenceBook(List.of(author), language, bookTitle, description());
            response.statusCode(201);
        }
    }

    @Given("{bookTitle} written in {language} by authors {authorName} and {authorName} is present in the list of books")
    public void bookPresentInList(String bookTitle, String language, String authorName1, String authorName2) {
        var bookIsPresent = isBookPresentInList(bookTitle);
        if (!bookIsPresent) {
            var authors = getAuthors(List.of(authorName1, authorName2));
            response = referenceBook(authors, language, bookTitle, description());
            response.statusCode(201);
        }
    }

    @Given("{bookTitle} written in {language} by {authorName} is present in the list of books, with title {bookTitle} in {language}")
    public void bookWithMultipleLocalizationsPresentInList(String bookTitle, String originalLanguage, String authorName,
                                                           String bookTitleOtherLanguage, String otherLanguage) {
        var bookIsPresent = isBookPresentInList(bookTitle);
        if (!bookIsPresent) {
            var author = getAuthor(authorName);
            var authorId = Optional.ofNullable(author.id()).orElseThrow();
            var request = new ReferenceRequest(List.of(authorId), originalLanguage, List.of(
                    new ReferenceDetail(originalLanguage, bookTitle, description()),
                    new ReferenceDetail(otherLanguage, bookTitleOtherLanguage, description())
            ));
            response = referenceBook(request);
            response.statusCode(201);
        }
    }

    @When("she references new book {bookTitle} written in {language} by {authorName} with a description")
    public void referenceNewBook(String bookTitle, String language, String authorName) {
        referenceTitle = bookTitle;

        var author = getAuthor(authorName);
        response = referenceBook(List.of(author), language, bookTitle, description());
    }

    @When("she references new book {bookTitle} written in {language} by authors {authorName} and {authorName} with a description")
    public void referenceNewBook(String bookTitle, String language, String authorName1, String authorName2) {
        referenceTitle = bookTitle;

        var authors = getAuthors(List.of(authorName1, authorName2));
        response = referenceBook(authors, language, bookTitle, description());
    }

    @When("she references new book {bookTitle} written in {language} by {authorName}, with title and description in {language}")
    public void referenceNewBookWithDetailInDifferentLanguage(String bookTitle, String originalLanguage, String authorName, String detailLanguage) {
        referenceTitle = bookTitle;

        var author = getAuthor(authorName);
        response = referenceBook(List.of(author), originalLanguage, detailLanguage, bookTitle, description());
    }

    @When("she references new book {bookTitle} written in {language} by {authorName}, with title and description in {language}, and title {bookTitle} in {language}")
    public void referenceNewBookWithDetailInDifferentLanguage(String bookTitle, String originalLanguage, String authorName,
                                                              String detailLanguage, String otherBookTitle, String otherLanguage) {
        referenceTitle = bookTitle;

        var author = getAuthor(authorName);
        var referenceRequest = new ReferenceRequest(List.of(requireNonNull(author.id())), originalLanguage, List.of(
                new ReferenceDetail(detailLanguage, bookTitle, description()),
                new ReferenceDetail(otherLanguage, otherBookTitle, description())
        ));
        response = referenceBook(referenceRequest);
    }

    @When("she references new book written in {language} by {authorName} with a description but no title")
    public void referenceNewBookNoTitle(String language, String authorName) {
        var author = getAuthor(authorName);
        response = referenceBook(List.of(author), language, null, description());
    }

    @When("she references new book {bookTitle} written in {language} with a description")
    public void referenceNewBookNoAuthor(String bookTitle, String language) {
        referenceTitle = bookTitle;

        response = referenceBook(null, language, bookTitle, description());
    }

    @When("she references new book {bookTitle} written by {authorName} with a description")
    public void referenceNewBookNoOriginalLanguage(String bookTitle, String authorName) {
        referenceTitle = bookTitle;

        var author = getAuthor(authorName);
        response = referenceBook(List.of(author), null, bookTitle, description());
    }

    @When("she references new book {bookTitle} written in {language} by {authorName} without description")
    public void referenceNewBookNoOriginalLanguage(String bookTitle, String language, String authorName) {
        referenceTitle = bookTitle;

        var author = getAuthor(authorName);
        response = referenceBook(List.of(author), language, bookTitle, null);
    }

    @Then("the book referencing fails with {bookParameterPointer} in {language} required")
    public void assertReferenceFailedMissingBookParameter(String pointer, String language) {
        requireNonNull(response, "Response must have been set before assertion")
                .assertThat()
                .log().ifStatusCodeMatches(not(equalTo(400)))
                .statusCode(400)
                .header("Location", nullValue())
                .body("type", equalTo("/problems/invalid-request"))
                .body("title", equalTo("Request validation error"))
                .body("errors[0].detail", equalTo("Missing required property"))
                .body("errors[0].pointer", equalTo(pointer));
    }

    @Then("the book referencing fails with {bookParameterPointer} required")
    public void assertReferenceFailedMissingBookParameter(String pointer) {
        requireNonNull(response, "Response must have been set before assertion")
                .assertThat()
                .log().ifStatusCodeMatches(not(equalTo(400)))
                .statusCode(400)
                .header("Location", nullValue())
                .body("type", equalTo("/problems/invalid-request"))
                .body("title", equalTo("Request validation error"))
                .body("errors[0].detail", equalTo("Missing required property"))
                .body("errors[0].pointer", equalTo(pointer));
    }

    @Then("the book referencing fails with duplication")
    public void assertReferenceFailedDuplication() {
        requireNonNull(response, "Response must have been set before assertion")
                .assertThat()
                .log().ifStatusCodeMatches(not(equalTo(409)))
                .statusCode(409)
                .header("Location", nullValue())
                .body("type", equalTo("/problems/duplicate-book"))
                .body("title", equalTo("Duplicate book"))
                .body("errors[0].detail", equalTo("Title '%s' already exists".formatted(referenceTitle)))
                .body("errors[0].pointer", equalTo("/details/0/title"));
    }

    @Then("book {bookTitle} is referenced")
    public void assertReferencedTitle(String bookTitle) {
        var location = requireNonNull(response, "Response must have been set before assertion")
                .assertThat()
                .log().ifError()
                .statusCode(201)
                .header("Location", notNullValue())
                .extract().header("Location");
        // TODO assert location is accessible and provides book
    }

    @Then("book {bookTitle} - written by {authorName} - is referenced")
    public void assertReferencedTitle(String bookTitle, String authorName) {
        assertReferencedTitle(bookTitle);
    }

    @Then("{bookTitle} is still not present in the list of books")
    public void assertBookStillNotPresentInList(String bookTitle) {
        var bookIsPresent = isBookPresentInList(bookTitle);
        assertThat(bookIsPresent).as("Book should still not be present after referencing failure").isFalse();
    }

    @Then("{bookTitle} is now present in the list of books")
    public void assertBookPresentInList(String bookTitle) {
        var bookIsPresent = isBookPresentInList(bookTitle);
        assertThat(bookIsPresent).as("Book should be present after referencing").isTrue();
    }

    @Then("{bookTitle} - written by {authorName} - is now present in the list of books")
    public void assertBookPresentInList(String bookTitle, String authorName) {
        var author = getAuthor(authorName);
        var bookIsPresent = isBookPresentInList(bookTitle, author);
        assertThat(bookIsPresent).as("Book should be present after referencing").isTrue();
    }

    @Then("{bookTitle} is present only once in the list of books")
    public void assertBookPresentOnceInList(String bookTitle) {
        var books = BookRetriever.findBooksByTitle(bookTitle);
        assertThat(books).as("Book should be present only once after referencing").hasSize(1);
    }

    @Then("{bookTitle} - written by {authorName} - is present only once in the list of books")
    public void assertBookPresentOnceInList(String bookTitle, String authorName) {
        var author = getAuthor(authorName);
        var books = BookRetriever.findBooksByTitleAndAuthor(bookTitle, author);
        assertThat(books).as("Book should be present only once after referencing").hasSize(1);
    }

    @Then("this book also has a localisation in {language} with title {bookTitle}")
    public void assertBookLocalisation(String language, String bookTitle) {
        assertThat(foundBook).isNotNull()
                .as("Book should have a localisation in language %s with title %s", language, bookTitle)
                .matches(book -> book.hasDetailIn(language) && book.detailIn(language).hasTitle(bookTitle));
    }

    @Then("{bookTitle} does not have a wikipedia link")
    public void assertBookWithNoWikipediaLink(String bookTitle) {
        assertThat(foundBook).isNotNull()
                .as("Book should not have wikipedia link for any detail").doesNotMatch(hasWikipediaLink());
    }

    private Author getAuthor(String authorName) {
        return AuthorRetriever.findAuthorByName(authorName)
                .orElseThrow(() -> new AssertionError("Author " + authorName + " must exist"));
    }

    private List<Author> getAuthors(List<String> authorsNames) {
        return authorsNames.stream().map(this::getAuthor).toList();
    }

    private boolean isBookPresentInList(String bookTitle) {
        var books = BookRetriever.findBooksByTitle(bookTitle);
        foundBook = books.stream().findFirst().orElse(null);
        return !books.isEmpty();
    }

    private boolean isBookPresentInList(String bookTitle, Author author) {
        var books = BookRetriever.findBooksByTitleAndAuthor(bookTitle, author);
        foundBook = books.stream().findFirst().orElse(null);
        return !books.isEmpty();
    }

    @ParameterType("[a-z][a-z ]*")
    public String bookParameterPointer(String source) {
        return switch (source) {
            case "author" -> "/authors";
            case "original language" -> "/original_language";
            case "title" -> "/details/0/title";
            case "description" -> "/details/0/description";
            default -> throw new IllegalArgumentException("Unknown book parameter " + source);
        };
    }

}
