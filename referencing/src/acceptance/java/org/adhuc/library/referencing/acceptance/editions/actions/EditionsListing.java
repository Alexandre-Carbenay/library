package org.adhuc.library.referencing.acceptance.editions.actions;

import org.adhuc.library.referencing.acceptance.editions.Edition;

import java.util.List;

import static io.restassured.RestAssured.given;

public class EditionsListing {

    public static List<Edition> listEditions() {
        return given()
                .log().ifValidationFails()
                .when()
                .get("/v1/editions")
                .then()
                .log().ifError()
                .statusCode(206)
                .extract()
                .body()
                .jsonPath().getList("_embedded.editions", Edition.class);
    }

}
