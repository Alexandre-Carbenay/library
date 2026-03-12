package org.adhuc.library.referencing.acceptance.publishers.actions;

import org.adhuc.library.referencing.acceptance.publishers.Publisher;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static io.restassured.RestAssured.given;

public class PublishersListing {

    public static List<Publisher> listPublishers() {
        return given()
                .log().ifValidationFails()
                .when()
                .get("/v1/publishers")
                .then()
                .log().ifError()
                .statusCode(206)
                .extract()
                .body()
                .jsonPath().getList("_embedded.publishers", Publisher.class);
    }

}
