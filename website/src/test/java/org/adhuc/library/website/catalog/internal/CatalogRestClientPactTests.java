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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static au.com.dius.pact.consumer.dsl.PactDslJsonRootValue.stringMatcher;
import static org.mockito.Mockito.when;

@ExtendWith({PactConsumerTestExt.class, MockitoExtension.class})
public class CatalogRestClientPactTests {

    private static final String UUID_REGEX = "^[0-9a-f]{8}\\b-[0-9a-f]{4}\\b-[0-9a-f]{4}\\b-[0-9a-f]{4}\\b-[0-9a-f]{12}$";

    @Mock
    private CircuitBreakerFactory<?, ?> circuitBreakerFactory;

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
        when(circuitBreakerFactory.create("catalog")).thenReturn(new CircuitBreaker() {
            @Override
            public <T> T run(Supplier<T> toRun, Function<Throwable, T> fallback) {
                return toRun.get();
            }
        });

        var properties = new CatalogRestClientProperties(mockServer.getUrl(), false);
        var restTemplate = new RestTemplate();
        var restClientBuilder = RestClient.builder(restTemplate);
        var catalogRestClient = new CatalogRestClient(restClientBuilder, null, circuitBreakerFactory, properties);

        var catalog = catalogRestClient.listBooks("fr");

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
        when(circuitBreakerFactory.create("catalog")).thenReturn(new CircuitBreaker() {
            @Override
            public <T> T run(Supplier<T> toRun, Function<Throwable, T> fallback) {
                return toRun.get();
            }
        });

        var properties = new CatalogRestClientProperties(mockServer.getUrl(), false);
        var restTemplate = new RestTemplate();
        var restClientBuilder = RestClient.builder(restTemplate);
        var catalogRestClient = new CatalogRestClient(restClientBuilder, null, circuitBreakerFactory, properties);

        var catalog = catalogRestClient.listBooks("fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3");

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
        when(circuitBreakerFactory.create("catalog")).thenReturn(new CircuitBreaker() {
            @Override
            public <T> T run(Supplier<T> toRun, Function<Throwable, T> fallback) {
                return toRun.get();
            }
        });

        var properties = new CatalogRestClientProperties(mockServer.getUrl(), false);
        var restTemplate = new RestTemplate();
        var restClientBuilder = RestClient.builder(restTemplate);
        var catalogRestClient = new CatalogRestClient(restClientBuilder, null, circuitBreakerFactory, properties);

        var catalog = catalogRestClient.listBooks("en,en-US;q=0.8,fr-FR;q=0.5,fr;q=0.3");

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
        when(circuitBreakerFactory.create("catalog")).thenReturn(new CircuitBreaker() {
            @Override
            public <T> T run(Supplier<T> toRun, Function<Throwable, T> fallback) {
                return toRun.get();
            }
        });

        var properties = new CatalogRestClientProperties(mockServer.getUrl(), false);
        var restTemplate = new RestTemplate();
        var restClientBuilder = RestClient.builder(restTemplate);
        var catalogRestClient = new CatalogRestClient(restClientBuilder, null, circuitBreakerFactory, properties);

        var catalog = catalogRestClient.listBooks(PageRequest.of(1, 25), "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3");

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
                            "Du contrat social",
                            List.of(new Author(UUID.fromString("99287cef-2c8c-4a4d-a82e-f1a8452dcfe2"), "Jean-Jacques Rousseau")),
                            "Paru en 1762, le Contrat social, ..."
                    ));
        });
    }

}
