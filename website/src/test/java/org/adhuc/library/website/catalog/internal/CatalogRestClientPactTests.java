package org.adhuc.library.website.catalog.internal;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.adhuc.library.website.catalog.Author;
import org.adhuc.library.website.catalog.Book;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static au.com.dius.pact.consumer.dsl.PactDslJsonRootValue.stringMatcher;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@Tag("apiClient")
@ExtendWith({PactConsumerTestExt.class, MockitoExtension.class})
public class CatalogRestClientPactTests {

    private static final String UUID_REGEX = "^[0-9a-f]{8}\\b-[0-9a-f]{4}\\b-[0-9a-f]{4}\\b-[0-9a-f]{4}\\b-[0-9a-f]{12}$";
    private static final String ISBN_REGEX = "^97[89][0-9]{10}$";

    @Pact(consumer = "library-website", provider = "library-catalog")
    public RequestResponsePact defaultPageNoProvidedLanguage(PactDslWithProvider builder) {
        return builder
                .given("First page of 10 elements contains books")
                .uponReceiving("Default website page request without accept language")
                .method("GET")
                .path("/api/v1/catalog")
                .query("page=0&size=10")
                .willRespondWith()
                .status(206)
                .headers(Map.of("Content-Type", "application/json"))
                .body(newJsonBody(root -> {
                    root.object("page", page -> {
                        page.integerType("size", 10);
                        page.integerType("total_elements", 67);
                        page.integerType("total_pages", 7);
                        page.integerType("number", 0);
                    });
                    root.object("_links", links -> {
                        links.object("first", first -> first.stringType("href"));
                        links.object("next", next -> next.stringType("href"));
                        links.object("last", last -> last.stringType("href"));
                    });
                    root.object("_embedded", embedded -> {
                        embedded.maxArrayLike("books", 10, book -> {
                            book.stringMatcher("id", UUID_REGEX, "b6608a30-1e9b-4ae0-a89d-624c3ca85da4");
                            book.stringValue("title", "Du contrat social");
                            book.minArrayLike("authors", 1, stringMatcher(UUID_REGEX, "99287cef-2c8c-4a4d-a82e-f1a8452dcfe2"), 1);
                            book.stringValue("description", "Paru en 1762, le Contrat social, ...");
                        });
                        embedded.minArrayLike("authors", 1, author -> {
                            author.stringMatcher("id", UUID_REGEX, "99287cef-2c8c-4a4d-a82e-f1a8452dcfe2");
                            author.stringValue("name", "Jean-Jacques Rousseau");
                        });
                    });
                }).build())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "defaultPageNoProvidedLanguage", pactVersion = PactSpecVersion.V3)
    void getCatalogDefaultPage(MockServer mockServer) {
        var properties = new CatalogRestClientProperties(mockServer.getUrl(), false);
        var client = new CatalogSpringClient(RestClient.builder(), null, properties);

        var catalog = client.listBooks("fr", "/api/v1/catalog?page={page}&size={size}", 0, 10);

        SoftAssertions.assertSoftly(s -> {
            s.assertThat(catalog.getSize()).isEqualTo(10);
            s.assertThat(catalog.getNumber()).isEqualTo(0);
            s.assertThat(catalog.getTotalElements()).isEqualTo(67);
            s.assertThat(catalog.getTotalPages()).isEqualTo(7);
            s.assertThat(catalog.hasLink("first")).isTrue();
            s.assertThat(catalog.hasLink("prev")).isFalse();
            s.assertThat(catalog.hasLink("next")).isTrue();
            s.assertThat(catalog.hasLink("last")).isTrue();

            s.assertThat(catalog.getContent()).hasSize(1)
                    .contains(new Book(
                            "b6608a30-1e9b-4ae0-a89d-624c3ca85da4",
                            "Du contrat social",
                            List.of(new Author(UUID.fromString("99287cef-2c8c-4a4d-a82e-f1a8452dcfe2"), "Jean-Jacques Rousseau")),
                            "Paru en 1762, le Contrat social, ..."
                    ));
        });
    }

    @Pact(consumer = "library-website", provider = "library-catalog")
    public RequestResponsePact defaultPageFrench(PactDslWithProvider builder) {
        return builder
                .given("First page of 10 elements contains books")
                .uponReceiving("Default website page request in french")
                .method("GET")
                .path("/api/v1/catalog")
                .query("page=0&size=10")
                .headers("Accept-Language", "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3")
                .willRespondWith()
                .status(206)
                .headers(Map.of("Content-Type", "application/json"))
                .body(newJsonBody(root -> {
                    root.object("page", page -> {
                        page.integerType("size", 10);
                        page.integerType("total_elements", 67);
                        page.integerType("total_pages", 7);
                        page.integerType("number", 0);
                    });
                    root.object("_links", links -> {
                        links.object("first", first -> first.stringType("href"));
                        links.object("next", next -> next.stringType("href"));
                        links.object("last", last -> last.stringType("href"));
                    });
                    root.object("_embedded", embedded -> {
                        embedded.maxArrayLike("books", 10, book -> {
                            book.stringMatcher("id", UUID_REGEX, "b6608a30-1e9b-4ae0-a89d-624c3ca85da4");
                            book.stringValue("title", "Du contrat social");
                            book.minArrayLike("authors", 1, stringMatcher(UUID_REGEX, "99287cef-2c8c-4a4d-a82e-f1a8452dcfe2"), 1);
                            book.stringValue("description", "Paru en 1762, le Contrat social, ...");
                        });
                        embedded.minArrayLike("authors", 1, author -> {
                            author.stringMatcher("id", UUID_REGEX, "99287cef-2c8c-4a4d-a82e-f1a8452dcfe2");
                            author.stringValue("name", "Jean-Jacques Rousseau");
                        });
                    });
                }).build())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "defaultPageFrench", pactVersion = PactSpecVersion.V3)
    void getCatalogDefaultPageFrench(MockServer mockServer) {
        var properties = new CatalogRestClientProperties(mockServer.getUrl(), false);
        var client = new CatalogSpringClient(RestClient.builder(), null, properties);

        var catalog = client.listBooks("fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3", "/api/v1/catalog?page={page}&size={size}", 0, 10);

        SoftAssertions.assertSoftly(s -> {
            s.assertThat(catalog.getSize()).isEqualTo(10);
            s.assertThat(catalog.getNumber()).isEqualTo(0);
            s.assertThat(catalog.getTotalElements()).isEqualTo(67);
            s.assertThat(catalog.getTotalPages()).isEqualTo(7);
            s.assertThat(catalog.hasLink("first")).isTrue();
            s.assertThat(catalog.hasLink("prev")).isFalse();
            s.assertThat(catalog.hasLink("next")).isTrue();
            s.assertThat(catalog.hasLink("last")).isTrue();

            s.assertThat(catalog.getContent()).hasSize(1)
                    .contains(new Book(
                            "b6608a30-1e9b-4ae0-a89d-624c3ca85da4",
                            "Du contrat social",
                            List.of(new Author(UUID.fromString("99287cef-2c8c-4a4d-a82e-f1a8452dcfe2"), "Jean-Jacques Rousseau")),
                            "Paru en 1762, le Contrat social, ..."
                    ));
        });
    }

    @Pact(consumer = "library-website", provider = "library-catalog")
    public RequestResponsePact defaultPageEnglish(PactDslWithProvider builder) {
        return builder
                .given("First page of 10 elements in english contains books")
                .uponReceiving("Default website page request in english")
                .method("GET")
                .path("/api/v1/catalog")
                .query("page=0&size=10")
                .headers("Accept-Language", "en,en-US;q=0.8,fr-FR;q=0.5,fr;q=0.3")
                .willRespondWith()
                .status(206)
                .headers(Map.of("Content-Type", "application/json"))
                .body(newJsonBody(root -> {
                    root.object("page", page -> {
                        page.integerType("size", 10);
                        page.integerType("total_elements", 67);
                        page.integerType("total_pages", 7);
                        page.integerType("number", 0);
                    });
                    root.object("_links", links -> {
                        links.object("first", first -> first.stringType("href"));
                        links.object("next", next -> next.stringType("href"));
                        links.object("last", last -> last.stringType("href"));
                    });
                    root.object("_embedded", embedded -> {
                        embedded.maxArrayLike("books", 10, book -> {
                            book.stringMatcher("id", UUID_REGEX, "b6608a30-1e9b-4ae0-a89d-624c3ca85da4");
                            book.stringValue("title", "The Social Contract");
                            book.minArrayLike("authors", 1, stringMatcher(UUID_REGEX, "99287cef-2c8c-4a4d-a82e-f1a8452dcfe2"), 1);
                            book.stringValue("description", "The Social Contract, originally published as On the Social Contract; or, Principles of Political Right...");
                        });
                        embedded.minArrayLike("authors", 1, author -> {
                            author.stringMatcher("id", UUID_REGEX, "99287cef-2c8c-4a4d-a82e-f1a8452dcfe2");
                            author.stringValue("name", "Jean-Jacques Rousseau");
                        });
                    });
                }).build())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "defaultPageEnglish", pactVersion = PactSpecVersion.V3)
    void getCatalogDefaultPageEnglish(MockServer mockServer) {
        var properties = new CatalogRestClientProperties(mockServer.getUrl(), false);
        var client = new CatalogSpringClient(RestClient.builder(), null, properties);

        var catalog = client.listBooks("en,en-US;q=0.8,fr-FR;q=0.5,fr;q=0.3", "/api/v1/catalog?page={page}&size={size}", 0, 10);

        SoftAssertions.assertSoftly(s -> {
            s.assertThat(catalog.getSize()).isEqualTo(10);
            s.assertThat(catalog.getNumber()).isEqualTo(0);
            s.assertThat(catalog.getTotalElements()).isEqualTo(67);
            s.assertThat(catalog.getTotalPages()).isEqualTo(7);
            s.assertThat(catalog.hasLink("first")).isTrue();
            s.assertThat(catalog.hasLink("prev")).isFalse();
            s.assertThat(catalog.hasLink("next")).isTrue();
            s.assertThat(catalog.hasLink("last")).isTrue();

            s.assertThat(catalog.getContent()).hasSize(1)
                    .contains(new Book(
                            "b6608a30-1e9b-4ae0-a89d-624c3ca85da4",
                            "The Social Contract",
                            List.of(new Author(UUID.fromString("99287cef-2c8c-4a4d-a82e-f1a8452dcfe2"), "Jean-Jacques Rousseau")),
                            "The Social Contract, originally published as On the Social Contract; or, Principles of Political Right..."
                    ));
        });
    }

    @Pact(consumer = "library-website", provider = "library-catalog")
    public RequestResponsePact otherPage(PactDslWithProvider builder) {
        return builder
                .given("Next page of 25 elements contains books")
                .uponReceiving("Specific website page request")
                .method("GET")
                .path("/api/v1/catalog")
                .query("page=1&size=25")
                .willRespondWith()
                .status(206)
                .headers(Map.of("Content-Type", "application/json"))
                .body(newJsonBody(root -> {
                    root.object("page", page -> {
                        page.integerType("size", 25);
                        page.integerType("total_elements", 67);
                        page.integerType("total_pages", 3);
                        page.integerType("number", 1);
                    });
                    root.object("_links", links -> {
                        links.object("first", first -> first.stringType("href"));
                        links.object("prev", prev -> prev.stringType("href"));
                        links.object("next", next -> next.stringType("href"));
                        links.object("last", last -> last.stringType("href"));
                    });
                    root.object("_embedded", embedded -> {
                        embedded.maxArrayLike("books", 10, book -> {
                            book.stringMatcher("id", UUID_REGEX, "b6608a30-1e9b-4ae0-a89d-624c3ca85da4");
                            book.stringValue("title", "Du contrat social");
                            book.minArrayLike("authors", 1, stringMatcher(UUID_REGEX, "99287cef-2c8c-4a4d-a82e-f1a8452dcfe2"), 1);
                            book.stringValue("description", "Paru en 1762, le Contrat social, ...");
                        });
                        embedded.minArrayLike("authors", 1, author -> {
                            author.stringMatcher("id", UUID_REGEX, "99287cef-2c8c-4a4d-a82e-f1a8452dcfe2");
                            author.stringValue("name", "Jean-Jacques Rousseau");
                        });
                    });
                }).build())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "otherPage", pactVersion = PactSpecVersion.V3)
    void getCatalogPage(MockServer mockServer) {
        var properties = new CatalogRestClientProperties(mockServer.getUrl(), false);
        var client = new CatalogSpringClient(RestClient.builder(), null, properties);

        var catalog = client.listBooks("fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3", "/api/v1/catalog?page={page}&size={size}", 1, 25);

        SoftAssertions.assertSoftly(s -> {
            s.assertThat(catalog.getSize()).isEqualTo(25);
            s.assertThat(catalog.getNumber()).isEqualTo(1);
            s.assertThat(catalog.getTotalElements()).isEqualTo(67);
            s.assertThat(catalog.getTotalPages()).isEqualTo(3);
            s.assertThat(catalog.hasLink("first")).isTrue();
            s.assertThat(catalog.hasLink("prev")).isTrue();
            s.assertThat(catalog.hasLink("next")).isTrue();
            s.assertThat(catalog.hasLink("last")).isTrue();

            s.assertThat(catalog.getContent()).hasSize(1)
                    .contains(new Book(
                            "b6608a30-1e9b-4ae0-a89d-624c3ca85da4",
                            "Du contrat social",
                            List.of(new Author(UUID.fromString("99287cef-2c8c-4a4d-a82e-f1a8452dcfe2"), "Jean-Jacques Rousseau")),
                            "Paru en 1762, le Contrat social, ..."
                    ));
        });
    }

    @Pact(consumer = "library-website", provider = "library-catalog")
    public RequestResponsePact bookDetailNoProvidedLanguage(PactDslWithProvider builder) {
        return builder
                .given("Book detail is reachable")
                .uponReceiving("Website book detail")
                .method("GET")
                .path("/api/v1/books/b6608a30-1e9b-4ae0-a89d-624c3ca85da4")
                .willRespondWith()
                .status(200)
                .headers(Map.of("Content-Type", "application/json"))
                .body(newJsonBody(root -> {
                    root.stringType("id", "b6608a30-1e9b-4ae0-a89d-624c3ca85da4");
                    root.stringType("title", "Du contrat social");
                    root.stringType("description", "Paru en 1762, le Contrat social, ...");
                    root.object("_embedded", embedded -> {
                        embedded.minArrayLike("authors", 1, author -> {
                            author.stringMatcher("id", UUID_REGEX, "99287cef-2c8c-4a4d-a82e-f1a8452dcfe2");
                            author.stringValue("name", "Jean-Jacques Rousseau");
                        });
                        embedded.minArrayLike("editions", 1, edition -> {
                            edition.stringMatcher("isbn", ISBN_REGEX, "9782290385050");
                            edition.object("_links", links ->
                                    links.object("self", selfLink ->
                                            selfLink.stringValue("href", "http://localhost:12345/api/v1/editions/9782290385050")));
                        });
                    });
                }).build())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "bookDetailNoProvidedLanguage", pactVersion = PactSpecVersion.V3)
    void getBookDetail(MockServer mockServer) {
        var properties = new CatalogRestClientProperties(mockServer.getUrl(), false);
        var client = new CatalogSpringClient(RestClient.builder(), null, properties);

        var book = client.retrieveBookDetails("b6608a30-1e9b-4ae0-a89d-624c3ca85da4", "");
        assertThat(book).isEqualTo(new BookDetailDto(
                "b6608a30-1e9b-4ae0-a89d-624c3ca85da4",
                "Du contrat social",
                "Paru en 1762, le Contrat social, ...",
                new BookDetailDto.EmbeddedValues(
                        List.of(new AuthorDto(UUID.fromString("99287cef-2c8c-4a4d-a82e-f1a8452dcfe2"), "Jean-Jacques Rousseau")),
                        List.of(new EditionDto("9782290385050", new EditionDto.Links(new EditionDto.LinkValue("http://localhost:12345/api/v1/editions/9782290385050"))))
                )
        ));
    }

    @Pact(consumer = "library-website", provider = "library-catalog")
    public RequestResponsePact editionDetail(PactDslWithProvider builder) {
        return builder
                .given("Edition detail is reachable")
                .uponReceiving("Website edition detail")
                .method("GET")
                .path("/api/v1/editions/9782290385050")
                .willRespondWith()
                .status(200)
                .headers(Map.of("Content-Type", "application/json"))
                .body(newJsonBody(root -> {
                    root.stringMatcher("isbn", ISBN_REGEX, "9782290385050");
                    root.stringValue("title", "Du contrat social ou Principes du droit politique");
                    root.stringValue("publication_date", "2023-02-08");
                    root.stringValue("publisher", "J'ai lu");
                    root.stringValue("language", "fr");
                }).build())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "editionDetail", pactVersion = PactSpecVersion.V3)
    void getEditionDetail(MockServer mockServer) {
        var properties = new CatalogRestClientProperties(mockServer.getUrl(), false);
        var client = new CatalogSpringClient(RestClient.builder(), null, properties);

        var edition = client.retrieveBookEditions(new BookDetailDto(
                "b6608a30-1e9b-4ae0-a89d-624c3ca85da4",
                "Du contrat social",
                "Paru en 1762, le Contrat social, ...",
                new BookDetailDto.EmbeddedValues(
                        List.of(new AuthorDto(UUID.fromString("99287cef-2c8c-4a4d-a82e-f1a8452dcfe2"), "Jean-Jacques Rousseau")),
                        List.of(new EditionDto("9782290385050", new EditionDto.Links(new EditionDto.LinkValue(mockServer.getUrl() + "/api/v1/editions/9782290385050"))))
                )
        ));
        assertThat(edition).isEqualTo(List.of(
                new EditionDetailDto(
                        "9782290385050",
                        "Du contrat social ou Principes du droit politique",
                        "J'ai lu",
                        LocalDate.parse("2023-02-08"),
                        "fr"
                )
        ));
    }

}
