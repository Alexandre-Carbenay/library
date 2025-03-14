package org.adhuc.library.catalog.acceptance;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.ValidatableResponse;

import java.util.List;

import static io.restassured.RestAssured.*;
import static org.adhuc.library.catalog.acceptance.assertions.EmbeddedAuthorsAssertions.assertResponseEmbedsAuthors;
import static org.hamcrest.Matchers.*;

@SuppressWarnings("preview")
public class CatalogStepDefinitions {

    private static final List<String> NAVIGATION_LINKS = List.of("first", "prev", "next", "last");

    private ValidatableResponse response;

    @When("he browses the catalog to page {int} showing {int} editions")
    public void browseCatalogToPage(int page, int pageSize) {
        response = given().params(
                        "page", page,
                        "size", pageSize
                )
                .when().get("/v1/catalog")
                .then();
    }

    @When("he browses the catalog for the first time")
    public void browseCatalogFirstTime() {
        response = when().get("/v1/catalog").then();
    }

    @When("he navigates through the catalog with {word} link")
    public void navigatesWithLink(String linkName) {
        var link = response.extract().jsonPath().getString(STR."_links.\{linkName}.href");
        response = when().get(link).then();
    }

    @Then("the catalog returns page {int} containing {int} editions over {int} requested")
    public void catalogReturnedPage(int page, int pageElements, int pageSize) {
        response.statusCode(206)
                .contentType("application/json")
                .body("page.number", describedAs(STR."Page number = \{page}", equalTo(page)))
                .body("page.size", describedAs(STR."Page size = \{pageSize}", equalTo(pageSize)))
                .body("_embedded.editions", describedAs(STR."Embedded editions size = \{pageElements}", hasSize(pageElements)));
    }

    @Then("the catalog contains {int} editions in a total of {int} pages")
    public void catalogReturnedElements(int totalElements, int totalPages) {
        response.statusCode(206)
                .contentType("application/json")
                .body("page.total_elements", describedAs(STR."Total elements = \{totalElements}", equalTo(totalElements)))
                .body("page.total_pages", describedAs(STR."Total pages = \{totalPages}", equalTo(totalPages)));
    }

    @Then("the catalog returns page {int} with available {navigation} links")
    public void catalogPageWithNavigationLinks(int page, List<String> linkNames) {
        response.statusCode(206)
                .contentType("application/json")
                .body("page.number", describedAs(STR."Page number = \{page}", equalTo(page)));
        NAVIGATION_LINKS.stream()
                .filter(link -> !linkNames.contains(link))
                .forEach(link -> response.body("_links", describedAs(STR."Link \{link} must not be present in catalog links", not(hasKey(link)))));
        linkNames.forEach(link -> response.body("_links", describedAs(STR."Link \{link} must be present in catalog links", hasKey(link))));
    }

    @Then("the page {int} contains editions corresponding to the expected {isbns}")
    public void catalogPageWithIsbns(int page, List<String> isbns) {
        for (int index = 0; index < isbns.size(); index++) {
            var isbn = isbns.get(index);
            response.body("_embedded.editions[%d].isbn", withArgs(index),
                    describedAs(STR."Edition with index \{index} within page \{page} must have ISBN = \{isbn}", equalTo(isbn)));
        }
    }

    @Then("the page {int} contains {authorNames} corresponding to the editions")
    public void catalogPageWithAuthors(int page, List<String> authorNames) {
        assertResponseEmbedsAuthors(response, authorNames, name -> STR."Catalog page \{page} must contain author named \{name}");
    }

}
