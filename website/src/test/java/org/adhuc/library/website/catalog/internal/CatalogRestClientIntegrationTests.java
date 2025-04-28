package org.adhuc.library.website.catalog.internal;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.spring6.circuitbreaker.configure.CircuitBreakerConfiguration;
import io.github.resilience4j.spring6.timelimiter.configure.TimeLimiterConfiguration;
import io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerProperties;
import io.github.resilience4j.springboot3.timelimiter.autoconfigure.TimeLimiterProperties;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
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
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.Charset;
import java.util.stream.IntStream;

import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType.COUNT_BASED;
import static io.github.resilience4j.timelimiter.TimeLimiterConfig.custom;
import static java.time.Duration.ofMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.adhuc.library.website.catalog.BooksMother.DU_CONTRAT_SOCIAL;
import static org.adhuc.library.website.catalog.BooksMother.DU_CONTRAT_SOCIAL_WITH_EDITIONS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.annotation.DirtiesContext.MethodMode.BEFORE_METHOD;

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

    private MockWebServer mockServer;
    @Autowired
    private CatalogRestClient catalogRestClient;

    @BeforeEach
    void setUp() throws Exception {
        mockServer = new MockWebServer();
        mockServer.start(12345);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockServer.shutdown();
    }

    @Test
    @DisplayName("list books page without timeout")
    void getPage() throws Exception {
        var responseBody = new DefaultResourceLoader().getResource("classpath:client/catalog/page-0-size-10.json");
        var response = new MockResponse()
                .setResponseCode(206)
                .setHeader("Content-Type", APPLICATION_JSON_VALUE)
                .setBody(responseBody.getContentAsString(Charset.defaultCharset()));
        mockServer.enqueue(response);

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

        assertThat(mockServer.getRequestCount()).isEqualTo(1);
        var request = mockServer.takeRequest();
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(request.getPath()).isEqualTo("/api/v1/catalog?page=0&size=10");
            s.assertThat(request.getHeader("Accept-Language")).isEqualTo("fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3");
        });
    }

    @Test
    @DisplayName("fail listing books page when timeout is reached")
    void getPageTimeout() throws Exception {
        var responseBody = new DefaultResourceLoader().getResource("classpath:client/catalog/page-0-size-10.json");
        var response = new MockResponse()
                .setResponseCode(206)
                .setHeader("Content-Type", APPLICATION_JSON_VALUE)
                .setBody(responseBody.getContentAsString(Charset.defaultCharset()))
                .setBodyDelay(TIMEOUT_DURATION_MS, MILLISECONDS);
        mockServer.enqueue(response);

        assertThrows(NoFallbackAvailableException.class, () -> catalogRestClient.listBooks("fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3"));
    }

    @Test
    @DisplayName("fail listing books page when server return an error")
    void getPageServerError() {
        var response = new MockResponse().setResponseCode(500);
        mockServer.enqueue(response);

        assertThrows(NoFallbackAvailableException.class, () -> catalogRestClient.listBooks("fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3"));
    }

    @Test
    @DirtiesContext(methodMode = BEFORE_METHOD)
    @DisplayName("open the circuit breaker when server fails multiple times while listing books page")
    void getPageMultipleErrorsOpenCircuitBreaker() {
        var response = new MockResponse().setResponseCode(500);
        IntStream.rangeClosed(1, 5).forEach(i -> mockServer.enqueue(response));

        IntStream.rangeClosed(1, 10)
                .forEach(i -> assertThrows(NoFallbackAvailableException.class, () -> catalogRestClient.listBooks("fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3")));

        assertThat(mockServer.getRequestCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("get book details without timeout")
    void getBook() throws Exception {
        var resourceLoader = new DefaultResourceLoader();
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", APPLICATION_JSON_VALUE)
                .setBody(resourceLoader.getResource("classpath:client/catalog/book-contrat-social.json")
                        .getContentAsString(Charset.defaultCharset())));
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", APPLICATION_JSON_VALUE)
                .setBody(resourceLoader.getResource("classpath:client/catalog/edition-contrat-social-9782081275232.json")
                        .getContentAsString(Charset.defaultCharset())));
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", APPLICATION_JSON_VALUE)
                .setBody(resourceLoader.getResource("classpath:client/catalog/edition-contrat-social-9782290385050.json")
                        .getContentAsString(Charset.defaultCharset())));

        var actual = catalogRestClient.getBook(DU_CONTRAT_SOCIAL.id(), "fr");
        assertThat(actual).isEqualTo(DU_CONTRAT_SOCIAL_WITH_EDITIONS);

        assertThat(mockServer.getRequestCount()).isEqualTo(3);
        var request = mockServer.takeRequest();
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(request.getPath()).isEqualTo("/api/v1/books/%s", DU_CONTRAT_SOCIAL.id());
            s.assertThat(request.getHeader("Accept-Language")).isEqualTo("fr");
        });
    }

    @Test
    @DisplayName("get book details with latency below the timeout for each edition")
    void getBookLatencyOnEditionsBelowTimeout() throws Exception {
        var resourceLoader = new DefaultResourceLoader();
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", APPLICATION_JSON_VALUE)
                .setBody(new DefaultResourceLoader().getResource("classpath:client/catalog/book-contrat-social.json")
                        .getContentAsString(Charset.defaultCharset())));
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", APPLICATION_JSON_VALUE)
                .setBody(resourceLoader.getResource("classpath:client/catalog/edition-contrat-social-9782081275232.json")
                        .getContentAsString(Charset.defaultCharset()))
                .setBodyDelay(DURATION_BELOW_LATENCY_MS, MILLISECONDS));
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", APPLICATION_JSON_VALUE)
                .setBody(resourceLoader.getResource("classpath:client/catalog/edition-contrat-social-9782290385050.json")
                        .getContentAsString(Charset.defaultCharset()))
                .setBodyDelay(DURATION_BELOW_LATENCY_MS, MILLISECONDS));

        assertThrows(NoFallbackAvailableException.class, () -> catalogRestClient.getBook(DU_CONTRAT_SOCIAL.id(), "fr"));
    }

    @Test
    @DisplayName("fail retrieving book details when timeout is reached on book endpoint call")
    void getBookTimeout() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", APPLICATION_JSON_VALUE)
                .setBody(new DefaultResourceLoader().getResource("classpath:client/catalog/book-contrat-social.json")
                        .getContentAsString(Charset.defaultCharset()))
                .setBodyDelay(TIMEOUT_DURATION_MS, MILLISECONDS));

        assertThrows(NoFallbackAvailableException.class, () -> catalogRestClient.getBook(DU_CONTRAT_SOCIAL.id(), "fr"));
    }

    @Test
    @DisplayName("fail retrieving book details when timeout is reached on the sum of all calls")
    void getBookGlobalTimeout() throws Exception {
        var resourceLoader = new DefaultResourceLoader();
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", APPLICATION_JSON_VALUE)
                .setBody(new DefaultResourceLoader().getResource("classpath:client/catalog/book-contrat-social.json")
                        .getContentAsString(Charset.defaultCharset()))
                .setBodyDelay(TIMEOUT_DURATION_MS / 2, MILLISECONDS));
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", APPLICATION_JSON_VALUE)
                .setBody(resourceLoader.getResource("classpath:client/catalog/edition-contrat-social-9782081275232.json")
                        .getContentAsString(Charset.defaultCharset())));
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", APPLICATION_JSON_VALUE)
                .setBody(resourceLoader.getResource("classpath:client/catalog/edition-contrat-social-9782290385050.json")
                        .getContentAsString(Charset.defaultCharset()))
                .setBodyDelay(TIMEOUT_DURATION_MS / 2, MILLISECONDS));

        assertThrows(NoFallbackAvailableException.class, () -> catalogRestClient.getBook(DU_CONTRAT_SOCIAL.id(), "fr"));
    }

    @Test
    @DisplayName("fail retrieving book details when server return an error")
    void getBookServerError() {
        var response = new MockResponse().setResponseCode(500);
        mockServer.enqueue(response);

        assertThrows(NoFallbackAvailableException.class, () -> catalogRestClient.getBook(DU_CONTRAT_SOCIAL.id(), "fr"));
    }

    @Test
    @DirtiesContext(methodMode = BEFORE_METHOD)
    @DisplayName("open the circuit breaker when server fails multiple times while retrieving book details")
    void getBookMultipleErrorsOpenCircuitBreaker() {
        var response = new MockResponse().setResponseCode(500);
        IntStream.rangeClosed(1, 5).forEach(i -> mockServer.enqueue(response));

        IntStream.rangeClosed(1, 10)
                .forEach(i -> assertThrows(NoFallbackAvailableException.class, () -> catalogRestClient.getBook(DU_CONTRAT_SOCIAL.id(), "fr")));

        assertThat(mockServer.getRequestCount()).isEqualTo(5);
    }

    @TestConfiguration
    static class CatalogRestClientTestConfiguration {
        @Bean
        CatalogRestClientProperties properties() {
            return new CatalogRestClientProperties("http://localhost:12345", false);
        }

        @Bean
        CatalogSpringReactiveClient catalogSpringReactiveClient(WebClient.Builder webClientBuilder, CatalogRestClientProperties properties) {
            return new CatalogSpringReactiveClient(webClientBuilder, null, properties);
        }

        @Bean
        CatalogRestClient catalogRestClient(CatalogSpringReactiveClient client, CircuitBreakerFactory<?, ?> circuitBreakerFactory) {
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
