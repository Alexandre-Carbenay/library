package org.adhuc.library.referencing.acceptance;

import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.ValidatableResponse;
import org.adhuc.library.referencing.acceptance.books.Book;
import org.adhuc.library.referencing.acceptance.books.BookRetriever;
import org.adhuc.library.referencing.acceptance.editions.EditionRetriever;
import org.adhuc.library.referencing.acceptance.publishers.Publisher;
import org.adhuc.library.referencing.acceptance.publishers.PublisherRetriever;
import org.assertj.core.api.SoftAssertions;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;

import static java.util.Objects.requireNonNull;
import static org.adhuc.library.referencing.acceptance.editions.EditionsMother.publicationDate;
import static org.adhuc.library.referencing.acceptance.editions.EditionsMother.summary;
import static org.adhuc.library.referencing.acceptance.editions.actions.EditionReferencing.referenceEdition;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.hamcrest.Matchers.*;

public class EditionsStepDefinitions {

    @Nullable
    private ValidatableResponse response;
    @Nullable
    private String referenceIsbn;
    @Nullable
    private String editionTitle;
    @Nullable
    private String editionSummary;

    @Given("{isbn} is not present in the list of editions")
    public void editionNotPresentInList(String isbn) {
        var editionIsPresent = isEditionPresentInList(isbn);
        assumeThat(editionIsPresent).as("Edition should not be present").isFalse();
    }

    @Given("edition with ISBN {isbn} edited in {language} by {publisherName} for {bookTitle} is present in the list of editions")
    public void bookPresentInList(String isbn, String language, String publisherName, String bookTitle) {
        var editionIsPresent = isEditionPresentInList(isbn);
        if (!editionIsPresent) {
            var book = getBook(bookTitle);
            var publisher = getPublisher(publisherName);
            response = referenceEdition(isbn, language, book, publisher, publicationDate());
            response.statusCode(201);
        }
    }

    @When("she references new edition with ISBN {isbn} edited in {language} by {publisherName} the {date} for {bookTitle}")
    public void referenceNewEdition(String isbn, String language, String publisherName, LocalDate date, String bookTitle) {
        this.referenceIsbn = isbn;
        var publisher = getPublisher(publisherName);
        var book = getBook(bookTitle);
        response = referenceEdition(isbn, language, book, publisher, date);
    }

    @When("she references new edition titled {bookTitle} with ISBN {isbn} edited in {language} by {publisherName} the {date} for {bookTitle}, with a summary")
    public void referenceNewEdition(String editionTitle, String isbn, String language, String publisherName, LocalDate date, String bookTitle) {
        this.referenceIsbn = isbn;
        this.editionTitle = editionTitle;
        this.editionSummary = summary();
        var publisher = getPublisher(publisherName);
        var book = getBook(bookTitle);
        response = referenceEdition(isbn, language, book, publisher, date, this.editionTitle, this.editionSummary);
    }

    @When("she references new edition edited in {language} by {publisherName} the {date} for {bookTitle}")
    public void referenceNewEditionNoIsbn(String language, String publisherName, LocalDate date, String bookTitle) {
        var publisher = getPublisher(publisherName);
        var book = getBook(bookTitle);
        response = referenceEdition(null, language, book, publisher, date);
    }

    @When("she references new edition with ISBN {isbn} edited by {publisherName} the {date} for {bookTitle}")
    public void referenceNewEditionNoLanguage(String isbn, String publisherName, LocalDate date, String bookTitle) {
        this.referenceIsbn = isbn;
        var publisher = getPublisher(publisherName);
        var book = getBook(bookTitle);
        response = referenceEdition(isbn, null, book, publisher, date);
    }

    @When("she references new edition with ISBN {isbn} edited in {language} by {publisherName} the {date}")
    public void referenceNewEditionNoBook(String isbn, String language, String publisherName, LocalDate date) {
        this.referenceIsbn = isbn;
        var publisher = getPublisher(publisherName);
        response = referenceEdition(isbn, language, null, publisher, date);
    }

    @When("she references new edition with ISBN {isbn} edited in {language} the {date} for {bookTitle}")
    public void referenceNewEditionNoPublisher(String isbn, String language, LocalDate date, String bookTitle) {
        this.referenceIsbn = isbn;
        var book = getBook(bookTitle);
        response = referenceEdition(isbn, language, book, null, date);
    }

    @When("she references new edition with ISBN {isbn} edited in {language} by {publisherName} for {bookTitle}")
    public void referenceNewEditionNoPublicationDate(String isbn, String language, String publisherName, String bookTitle) {
        this.referenceIsbn = isbn;
        var publisher = getPublisher(publisherName);
        var book = getBook(bookTitle);
        response = referenceEdition(isbn, language, book, publisher, null);
    }

    @Then("the edition referencing fails with {editionParameterPointer} required")
    public void assertReferenceFailedMissingEditionParameter(String pointer) {
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

    @Then("the edition referencing fails with duplication")
    public void assertReferenceFailedDuplication() {
        requireNonNull(response, "Response must have been set before assertion")
                .assertThat()
                .log().ifStatusCodeMatches(not(equalTo(409)))
                .statusCode(409)
                .header("Location", nullValue())
                .body("type", equalTo("/problems/duplicate-edition"))
                .body("title", equalTo("Duplicate edition"))
                .body("errors[0].detail", equalTo("Edition with ISBN '%s' already exists".formatted(referenceIsbn)))
                .body("errors[0].pointer", equalTo("/isbn"));
    }

    @Then("edition with ISBN {isbn} is referenced")
    public void assertReferencedEdition(String isbn) {
        var location = requireNonNull(response, "Response must have been set before assertion")
                .assertThat()
                .log().ifError()
                .statusCode(201)
                .header("Location", notNullValue())
                .extract().header("Location");
        // TODO assert location is accessible and provides book
    }

    @Then("{isbn} is now present in the list of editions")
    public void assertEditionPresentInList(String isbn) {
        var editionIsPresent = isEditionPresentInList(isbn);
        assertThat(editionIsPresent).as("Edition should be present after referencing").isTrue();
    }

    @Then("{isbn} is present only once in the list of editions")
    public void assertEditionPresentOnceInList(String isbn) {
        var editions = EditionRetriever.findEditionsByIsbn(isbn);
        assertThat(editions).as("Edition should be present only once after referencing").hasSize(1);
    }

    @Then("edition {isbn} has the title and summary of its book in {language}")
    public void assertEditionHasBookTitleAndSummary(String isbn, String language) {
        var edition = EditionRetriever.findEditionsByIsbn(isbn).stream().findFirst().orElseThrow();
        var book = BookRetriever.findBookById(edition.bookId()).orElseThrow();
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(edition.title())
                    .as("Edition %s must have title of its book %s in %s", edition.isbn(), book.id(), language)
                    .isEqualTo(book.titleIn(language));
            s.assertThat(edition.summary())
                    .as("Edition %s must have summary of its book %s in %s", edition.isbn(), book.id(), language)
                    .isEqualTo(book.descriptionIn(language));
        });
    }

    @Then("edition {isbn} has the specified title and summary")
    public void assertEditionHasSpecifiedTitleAndSummary(String isbn) {
        var edition = EditionRetriever.findEditionsByIsbn(isbn).stream().findFirst().orElseThrow();
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(editionTitle).as("Edition %s must have title", edition.isbn()).isEqualTo(edition.title());
            s.assertThat(editionSummary).as("Edition %s must have summary", edition.isbn()).isEqualTo(edition.summary());
        });
    }

    private Book getBook(String bookTitle) {
        return BookRetriever.findBooksByTitle(bookTitle)
                .stream().findFirst()
                .orElseThrow(() -> new AssertionError("Book " + bookTitle + " must exist"));
    }

    private Publisher getPublisher(String publisherName) {
        return PublisherRetriever.findPublisherByName(publisherName)
                .stream().findFirst()
                .orElseThrow(() -> new AssertionError("Publisher " + publisherName + " must exist"));
    }

    private boolean isEditionPresentInList(String isbn) {
        var editions = EditionRetriever.findEditionsByIsbn(isbn);
        return !editions.isEmpty();
    }

    @ParameterType("[A-z][A-z ]*")
    public String editionParameterPointer(String source) {
        return switch (source) {
            case "ISBN" -> "/isbn";
            case "language" -> "/language";
            case "book" -> "/book";
            case "publisher" -> "/publisher";
            case "publication date" -> "/publication_date";
            default -> throw new IllegalArgumentException("Unknown edition parameter " + source);
        };
    }

}
