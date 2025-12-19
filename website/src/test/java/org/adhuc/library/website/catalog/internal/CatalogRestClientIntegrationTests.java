package org.adhuc.library.website.catalog.internal;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.spring6.circuitbreaker.configure.CircuitBreakerConfiguration;
import io.github.resilience4j.spring6.timelimiter.configure.TimeLimiterConfiguration;
import io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerProperties;
import io.github.resilience4j.springboot3.timelimiter.autoconfigure.TimeLimiterProperties;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JAutoConfiguration;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestClient;

import java.util.stream.IntStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType.COUNT_BASED;
import static io.github.resilience4j.timelimiter.TimeLimiterConfig.custom;
import static java.nio.charset.Charset.defaultCharset;
import static java.time.Duration.ofMillis;
import static org.adhuc.library.website.catalog.BooksMother.DU_CONTRAT_SOCIAL;
import static org.adhuc.library.website.catalog.BooksMother.DU_CONTRAT_SOCIAL_WITH_EDITIONS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

@Tag("integration")
@Tag("apiClient")
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
@RestClientTest
@ContextConfiguration(classes = {
        Resilience4JAutoConfiguration.class,
        CircuitBreakerConfiguration.class,
        CircuitBreakerProperties.class,
        TimeLimiterConfiguration.class,
        TimeLimiterProperties.class,
        CatalogRestClientIntegrationTests.CatalogRestClientTestConfiguration.class
})
@DisplayName("Catalog REST client with circuit breaker should")
class CatalogRestClientIntegrationTests {

    private static final int TIMEOUT_DURATION_MS = 500;
    private static final int DURATION_BELOW_LATENCY_MS = 400;

    private WireMockServer mockServer;
    @Autowired
    private CatalogRestClient catalogRestClient;

    @BeforeEach
    void setUp() {
        mockServer = new WireMockServer(12345);
        mockServer.start();
    }

    @AfterEach
    void tearDown() {
        mockServer.stop();
    }

    @Test
    @DisplayName("list books page without timeout")
    void getPage() throws Exception {
        var responseBody = new DefaultResourceLoader().getResource("classpath:client/catalog/page-0-size-10.json");
        mockServer.stubFor(get(urlEqualTo("/api/v1/catalog?page=0&size=10"))
                .willReturn(aResponse()
                        .withStatus(206)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody.getContentAsString(defaultCharset()))
                )
        );

        var actual = catalogRestClient.listBooks("fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3");
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(actual.getSize()).isEqualTo(10);
            s.assertThat(actual.getTotalElements()).isEqualTo(67);
            s.assertThat(actual.getTotalPages()).isEqualTo(7);
            s.assertThat(actual.getNumber()).isEqualTo(0);
            s.assertThat(actual.getContent().size()).isEqualTo(10);
            s.assertThat(actual.hasLink("first")).isTrue();
            s.assertThat(actual.hasLink("prev")).isFalse();
            s.assertThat(actual.hasLink("next")).isTrue();
            s.assertThat(actual.hasLink("last")).isTrue();
        });

        mockServer.verify(getRequestedFor(urlEqualTo("/api/v1/catalog?page=0&size=10"))
                .withHeader("Accept-Language", equalTo("fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3")));
    }

    @Test
    @DisplayName("fail listing books page when timeout is reached")
    void getPageTimeout() throws Exception {
        var responseBody = new DefaultResourceLoader().getResource("classpath:client/catalog/page-0-size-10.json");
        mockServer.stubFor(get(urlEqualTo("/api/v1/catalog?page=0&size=10"))
                .willReturn(aResponse()
                        .withFixedDelay(TIMEOUT_DURATION_MS)
                        .withStatus(206)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody.getContentAsString(defaultCharset()))
                )
        );

        assertThrows(NoFallbackAvailableException.class, () -> catalogRestClient.listBooks("fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3"));
    }

    @Test
    @DisplayName("fail listing books page when server return an error")
    void getPageServerError() {
        mockServer.stubFor(get(urlEqualTo("/api/v1/catalog?page=0&size=10"))
                .willReturn(aResponse().withStatus(500)));

        assertThrows(NoFallbackAvailableException.class, () -> catalogRestClient.listBooks("fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3"));
    }

    @Test
    @DisplayName("open the circuit breaker when server fails multiple times while listing books page")
    void getPageMultipleErrorsOpenCircuitBreaker() {
        IntStream.rangeClosed(1, 5).forEach(i ->
                mockServer.stubFor(get(urlEqualTo("/api/v1/catalog?page=0&size=10"))
                        .willReturn(aResponse().withStatus(500))));

        IntStream.rangeClosed(1, 10)
                .forEach(i -> assertThrows(NoFallbackAvailableException.class, () -> catalogRestClient.listBooks("fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3")));

        mockServer.verify(5, getRequestedFor(urlEqualTo("/api/v1/catalog?page=0&size=10"))
                .withHeader("Accept-Language", equalTo("fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3")));
    }

    @Test
    @DisplayName("get book details without timeout")
    void getBook() throws Exception {
        var resourceLoader = new DefaultResourceLoader();
        mockServer.stubFor(get(urlEqualTo("/api/v1/books/b6608a30-1e9b-4ae0-a89d-624c3ca85da4"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(resourceLoader.getResource("classpath:client/catalog/book-contrat-social.json")
                                .getContentAsString(defaultCharset()))
                )
        );
        mockServer.stubFor(get(urlEqualTo("/api/v1/editions/9782081275232"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(resourceLoader.getResource("classpath:client/catalog/edition-contrat-social-9782081275232.json")
                                .getContentAsString(defaultCharset()))
                )
        );
        mockServer.stubFor(get(urlEqualTo("/api/v1/editions/9782290385050"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(resourceLoader.getResource("classpath:client/catalog/edition-contrat-social-9782290385050.json")
                                .getContentAsString(defaultCharset()))
                )
        );

        var actual = catalogRestClient.getBook(DU_CONTRAT_SOCIAL.id(), "fr");
        assertThat(actual).isEqualTo(DU_CONTRAT_SOCIAL_WITH_EDITIONS);

        mockServer.verify(getRequestedFor(urlEqualTo("/api/v1/books/b6608a30-1e9b-4ae0-a89d-624c3ca85da4"))
                .withHeader("Accept-Language", equalTo("fr")));
        mockServer.verify(getRequestedFor(urlEqualTo("/api/v1/editions/9782081275232")));
        mockServer.verify(getRequestedFor(urlEqualTo("/api/v1/editions/9782290385050")));
    }

    @Test
    @DisplayName("get book details with latency below the timeout for each edition")
    void getBookLatencyOnEditionsBelowTimeout() throws Exception {
        var resourceLoader = new DefaultResourceLoader();
        mockServer.stubFor(get(urlEqualTo("/api/v1/books/b6608a30-1e9b-4ae0-a89d-624c3ca85da4"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(resourceLoader.getResource("classpath:client/catalog/book-contrat-social.json")
                                .getContentAsString(defaultCharset()))
                )
        );
        mockServer.stubFor(get(urlEqualTo("/api/v1/editions/9782081275232"))
                .willReturn(aResponse()
                        .withFixedDelay(DURATION_BELOW_LATENCY_MS)
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(resourceLoader.getResource("classpath:client/catalog/edition-contrat-social-9782081275232.json")
                                .getContentAsString(defaultCharset()))
                )
        );
        mockServer.stubFor(get(urlEqualTo("/api/v1/editions/9782290385050"))
                .willReturn(aResponse()
                        .withFixedDelay(DURATION_BELOW_LATENCY_MS)
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(resourceLoader.getResource("classpath:client/catalog/edition-contrat-social-9782290385050.json")
                                .getContentAsString(defaultCharset()))
                )
        );

        assertThrows(NoFallbackAvailableException.class, () -> catalogRestClient.getBook(DU_CONTRAT_SOCIAL.id(), "fr"));

        mockServer.verify(getRequestedFor(urlEqualTo("/api/v1/books/b6608a30-1e9b-4ae0-a89d-624c3ca85da4"))
                .withHeader("Accept-Language", equalTo("fr")));
        mockServer.verify(getRequestedFor(urlEqualTo("/api/v1/editions/9782081275232")));
        mockServer.verify(getRequestedFor(urlEqualTo("/api/v1/editions/9782290385050")));
    }

    @Test
    @DisplayName("fail retrieving book details when timeout is reached on book endpoint call")
    void getBookTimeout() throws Exception {
        mockServer.stubFor(get(urlEqualTo("/api/v1/books/b6608a30-1e9b-4ae0-a89d-624c3ca85da4"))
                .willReturn(aResponse()
                        .withFixedDelay(TIMEOUT_DURATION_MS)
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(new DefaultResourceLoader().getResource("classpath:client/catalog/book-contrat-social.json")
                                .getContentAsString(defaultCharset()))
                )
        );

        assertThrows(NoFallbackAvailableException.class, () -> catalogRestClient.getBook(DU_CONTRAT_SOCIAL.id(), "fr"));

        mockServer.verify(getRequestedFor(urlEqualTo("/api/v1/books/b6608a30-1e9b-4ae0-a89d-624c3ca85da4"))
                .withHeader("Accept-Language", equalTo("fr")));
    }

    @Test
    @DisplayName("fail retrieving book details when timeout is reached on the sum of all calls")
    void getBookGlobalTimeout() throws Exception {
        var resourceLoader = new DefaultResourceLoader();
        mockServer.stubFor(get(urlEqualTo("/api/v1/books/b6608a30-1e9b-4ae0-a89d-624c3ca85da4"))
                .willReturn(aResponse()
                        .withFixedDelay(TIMEOUT_DURATION_MS / 2)
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(resourceLoader.getResource("classpath:client/catalog/book-contrat-social.json")
                                .getContentAsString(defaultCharset()))
                )
        );
        mockServer.stubFor(get(urlEqualTo("/api/v1/editions/9782081275232"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(resourceLoader.getResource("classpath:client/catalog/edition-contrat-social-9782081275232.json")
                                .getContentAsString(defaultCharset()))
                )
        );
        mockServer.stubFor(get(urlEqualTo("/api/v1/editions/9782290385050"))
                .willReturn(aResponse()
                        .withFixedDelay(TIMEOUT_DURATION_MS / 2)
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(resourceLoader.getResource("classpath:client/catalog/edition-contrat-social-9782290385050.json")
                                .getContentAsString(defaultCharset()))
                )
        );

        assertThrows(NoFallbackAvailableException.class, () -> catalogRestClient.getBook(DU_CONTRAT_SOCIAL.id(), "fr"));

        mockServer.verify(getRequestedFor(urlEqualTo("/api/v1/books/b6608a30-1e9b-4ae0-a89d-624c3ca85da4"))
                .withHeader("Accept-Language", equalTo("fr")));
        mockServer.verify(getRequestedFor(urlEqualTo("/api/v1/editions/9782081275232")));
        mockServer.verify(getRequestedFor(urlEqualTo("/api/v1/editions/9782290385050")));
    }

    @Test
    @DisplayName("fail retrieving book details when server return an error")
    void getBookServerError() {
        mockServer.stubFor(get(urlEqualTo("/api/v1/books/b6608a30-1e9b-4ae0-a89d-624c3ca85da4"))
                .willReturn(aResponse().withStatus(500)));

        assertThrows(NoFallbackAvailableException.class, () -> catalogRestClient.getBook(DU_CONTRAT_SOCIAL.id(), "fr"));
    }

    @Test
    @DisplayName("open the circuit breaker when server fails multiple times while retrieving book details")
    void getBookMultipleErrorsOpenCircuitBreaker() {
        IntStream.rangeClosed(1, 5).forEach(i ->
                mockServer.stubFor(get(urlEqualTo("/api/v1/books/b6608a30-1e9b-4ae0-a89d-624c3ca85da4"))
                        .willReturn(aResponse().withStatus(500))));

        IntStream.rangeClosed(1, 10)
                .forEach(i -> assertThrows(NoFallbackAvailableException.class, () -> catalogRestClient.getBook(DU_CONTRAT_SOCIAL.id(), "fr")));

        mockServer.verify(5, getRequestedFor(urlEqualTo("/api/v1/books/b6608a30-1e9b-4ae0-a89d-624c3ca85da4"))
                .withHeader("Accept-Language", equalTo("fr")));
    }

    @TestConfiguration
    static class CatalogRestClientTestConfiguration {
        @Bean
        CatalogRestClientProperties properties() {
            return new CatalogRestClientProperties("http://localhost:12345", false);
        }

        @Bean
        RestClient.Builder restClientBuilder() {
            return RestClient.builder();
        }

        @Bean
        CatalogSpringClient catalogSpringReactiveClient(RestClient.Builder restClientBuilder, CatalogRestClientProperties properties) {
            return new CatalogSpringClient(restClientBuilder, null, properties);
        }

        @Bean
        CatalogRestClient catalogRestClient(CatalogSpringClient client, CircuitBreakerFactory<?, ?> circuitBreakerFactory) {
            return new CatalogRestClient(client, circuitBreakerFactory);
        }

        @Bean
        Customizer<Resilience4JCircuitBreakerFactory> testCustomizer() {
            return factory -> factory
                    .configureDefault(id -> new Resilience4JConfigBuilder(id)
                            .timeLimiterConfig(custom().timeoutDuration(ofMillis(TIMEOUT_DURATION_MS)).build())
                            .circuitBreakerConfig(CircuitBreakerConfig.custom()
                                    .slidingWindowSize(5).slidingWindowType(COUNT_BASED).build()
                            )
                            .build());
        }
    }

}
