package org.adhuc.library.referencing.acceptance;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.ValidatableResponse;
import org.adhuc.library.referencing.acceptance.publishers.PublisherRetriever;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static org.adhuc.library.referencing.acceptance.publishers.actions.PublisherReferencing.referencePublisher;
import static org.adhuc.library.referencing.acceptance.publishers.actions.PublisherReferencing.referencePublisherWithoutName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.hamcrest.Matchers.*;

public class PublishersStepDefinitions {

    @Nullable
    private ValidatableResponse response;
    @Nullable
    private String referenceName;

    @Given("{publisherName} is not present in the list of publishers")
    public void authorNotPresentInList(String publisherName) {
        var publisherIsPresent = isPublisherPresentInList(publisherName);
        assumeThat(publisherIsPresent).as("Publisher should not be present").isFalse();
    }

    @Given("{publisherName} is present in the list of publishers")
    public void publisherPresentInList(String publisherName) {
        var publisherIsPresent = isPublisherPresentInList(publisherName);
        if (!publisherIsPresent) {
            response = referencePublisher(publisherName);
            response.statusCode(201);
        }
    }

    @When("she references new publisher {publisherName}")
    public void referenceNewPublisher(String publisherName) {
        referenceName = publisherName;
        response = referencePublisher(publisherName);
    }

    @When("she references new publisher without name")
    public void referenceNewPublisherWithoutName() {
        response = referencePublisherWithoutName();
    }

    @Then("publisher {publisherName} is referenced")
    public void assertReferencedPublisher(String publisherName) {
        var location = Objects.requireNonNull(response, "Response must have been set before assertion")
                .assertThat()
                .log().ifError()
                .statusCode(201)
                .header("Location", notNullValue())
                .extract().header("Location");
        // TODO assert location is accessible and provides publisher
    }

    @Then("the publisher referencing fails with name required")
    public void assertReferenceFailedMissingName() {
        Objects.requireNonNull(response, "Response must have been set before assertion")
                .assertThat()
                .log().ifStatusCodeMatches(not(equalTo(400)))
                .statusCode(400)
                .header("Location", nullValue())
                .body("type", equalTo("/problems/invalid-request"))
                .body("title", equalTo("Request validation error"))
                .body("errors[0].detail", equalTo("Missing required property"))
                .body("errors[0].pointer", equalTo("/name"));
    }

    @Then("the publisher referencing fails with duplication")
    public void assertReferenceFailedDuplication() {
        requireNonNull(response, "Response must have been set before assertion")
                .assertThat()
                .log().ifStatusCodeMatches(not(equalTo(409)))
                .statusCode(409)
                .header("Location", nullValue())
                .body("type", equalTo("/problems/duplicate-publisher"))
                .body("title", equalTo("Duplicate publisher"))
                .body("errors[0].detail", equalTo("Publisher '%s' already exists".formatted(referenceName)))
                .body("errors[0].pointer", equalTo("/name"));
    }

    @Then("{publisherName} is now present in the list of publishers")
    public void assertPublisherPresentInList(String publisherName) {
        var publisherIsPresent = isPublisherPresentInList(publisherName);
        assertThat(publisherIsPresent).as("Publisher should be present after referencing").isTrue();
    }

    @Then("{publisherName} is present only once in the list of publishers")
    public void assertBookPresentOnceInList(String publisherName) {
        var publishers = PublisherRetriever.findPublisherByName(publisherName);
        assertThat(publishers).as("Publisher should be present only once after referencing").hasSize(1);
    }

    private boolean isPublisherPresentInList(String publisherName) {
        return !PublisherRetriever.findPublisherByName(publisherName).isEmpty();
    }

}
