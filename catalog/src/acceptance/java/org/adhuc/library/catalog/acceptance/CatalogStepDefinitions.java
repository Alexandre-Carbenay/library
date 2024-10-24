package org.adhuc.library.catalog.acceptance;

import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.ValidatableResponse;

import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.*;

@SuppressWarnings("preview")
public class CatalogStepDefinitions {

    private static final List<String> NAVIGATION_LINKS = List.of("first", "prev", "next", "last");

    private ValidatableResponse response;

    @Given("{word} is a library member")
    public void libraryMember(String name) {
        // nothing to do here now
    }

    @When("he browses the catalog to page {int} showing {int} books")
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

    @Then("the catalog returns page {int} containing {int} books over {int} requested")
    public void catalogReturnedPage(int page, int pageElements, int pageSize) {
        response.statusCode(206)
                .contentType("application/json")
                .body("page.number", describedAs(STR."Page number = \{page}", equalTo(page)))
                .body("page.size", describedAs(STR."Page size = \{pageSize}", equalTo(pageSize)))
                .body("_embedded.books", describedAs(STR."Embedded books size = \{pageElements}", hasSize(pageElements)));
    }

    @Then("the catalog contains {int} books in a total of {int} pages")
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

    @Then("the page {int} contains books corresponding to the expected {isbns}")
    public void catalogPageWithIsbn(int page, List<String> isbns) {
        for (int index = 0; index < isbns.size(); index++) {
            var isbn = isbns.get(index);
            response.body(STR."_embedded.books[\{index}].isbn",
                    describedAs(STR."Book with index \{index} within page \{page} must have ISBN = \{isbn}", equalTo(isbn)));
        }
    }

    @ParameterType(".*")
    public List<String> navigation(String source) {
        return List.of(source.split(", "));
    }

    @ParameterType(".*")
    public List<String> isbns(String source) {
        return List.of(source.split(", "));
    }

}
