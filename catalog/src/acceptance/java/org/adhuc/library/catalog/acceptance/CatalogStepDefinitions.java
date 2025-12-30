package org.adhuc.library.catalog.acceptance;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.ValidatableResponse;

import java.util.List;
import java.util.Locale;

import static io.restassured.RestAssured.*;
import static org.adhuc.library.catalog.acceptance.assertions.EmbeddedAuthorsAssertions.assertResponseEmbedsAuthors;
import static org.hamcrest.Matchers.*;

public class CatalogStepDefinitions {

    private static final List<String> NAVIGATION_LINKS = List.of("first", "prev", "next", "last");

    private Locale language;
    private ValidatableResponse response;

    @When("he browses the catalog to page {int} showing {int} books in {language}")
    public void browseCatalogToPage(int page, int pageSize, Locale language) {
        this.language = language;
        response = given().params(
                        "page", page,
                        "size", pageSize
                )
                .header("Accept-Language", language.getLanguage())
                .when()
                .get("/v1/catalog")
                .then();
    }

    @When("he browses the catalog for the first time")
    public void browseCatalogFirstTime() {
        response = when().get("/v1/catalog").then();
    }

    @When("he navigates through the catalog with {word} link")
    public void navigatesWithLink(String linkName) {
        var link = response.extract().jsonPath().getString("_links." + linkName + ".href");
        response = given()
                .header("Accept-Language", language.getLanguage())
                .when()
                .get(link)
                .then();
    }

    @Then("the catalog returns page {int} containing {int} books over {int} requested")
    public void catalogReturnedPage(int page, int pageElements, int pageSize) {
        response.statusCode(206)
                .contentType("application/json")
                .body("page.number", describedAs("Page number = " + page, equalTo(page)))
                .body("page.size", describedAs("Page size = " + pageSize, equalTo(pageSize)))
                .body("_embedded.books", describedAs("Embedded books size = " + pageElements, hasSize(pageElements)));
    }

    @Then("the catalog contains {int} books in a total of {int} pages")
    public void catalogReturnedElements(int totalElements, int totalPages) {
        response.statusCode(206)
                .contentType("application/json")
                .body("page.total_elements", describedAs("Total elements = " + totalElements, equalTo(totalElements)))
                .body("page.total_pages", describedAs("Total pages = " + totalPages, equalTo(totalPages)));
    }

    @Then("the catalog returns page {int} with available {navigation} links")
    public void catalogPageWithNavigationLinks(int page, List<String> linkNames) {
        response.statusCode(206)
                .contentType("application/json")
                .body("page.number", describedAs("Page number = " + page, equalTo(page)));
        NAVIGATION_LINKS.stream()
                .filter(link -> !linkNames.contains(link))
                .forEach(link -> response.body("_links", describedAs("Link " + link + " must not be present in catalog links", not(hasKey(link)))));
        linkNames.forEach(link -> response.body("_links", describedAs("Link " + link + " must be present in catalog links", hasKey(link))));
    }

    @Then("the page {int} contains books corresponding to the expected {ids}")
    public void catalogPageWithIds(int page, List<String> ids) {
        for (int index = 0; index < ids.size(); index++) {
            var id = ids.get(index);
            response.body("_embedded.books[%d].id", withArgs(index),
                    describedAs("Book with index " + index + " within page " + page + " must have id starting with " + id, startsWith(id)));
        }
    }

    @Then("the page {int} contains {authorNames} corresponding to the books")
    public void catalogPageWithAuthors(int page, List<String> authorNames) {
        assertResponseEmbedsAuthors(response, authorNames, name -> "Catalog page " + page + " must contain author named " + name);
    }

    @Then("the catalog returns page in {language}")
    public void catalogPageInLanguage(Locale language) {
        response.header("Content-Language", language.getLanguage());
    }

}
