package org.adhuc.library.referencing.acceptance.publishers.actions;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.adhuc.library.referencing.acceptance.publishers.Publisher;

import static io.restassured.RestAssured.given;

public class PublisherReferencing {

    public static ValidatableResponse referencePublisher(String name) {
        return referencePublisherInternal(new Publisher(name));
    }

    public static ValidatableResponse referencePublisherWithoutName() {
        return referencePublisherInternal("{}");
    }

    private static ValidatableResponse referencePublisherInternal(Object body) {
        return given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/v1/publishers")
                .then();
    }

}
