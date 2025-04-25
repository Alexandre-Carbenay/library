package org.adhuc.library.website.catalog.internal;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.spring6.circuitbreaker.configure.CircuitBreakerConfiguration;
import io.github.resilience4j.spring6.timelimiter.configure.TimeLimiterConfiguration;
import io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerProperties;
import io.github.resilience4j.springboot3.timelimiter.autoconfigure.TimeLimiterProperties;
import org.assertj.core.api.SoftAssertions;
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
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.stream.IntStream;

import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType.COUNT_BASED;
import static io.github.resilience4j.timelimiter.TimeLimiterConfig.custom;
import static java.time.Duration.ofMillis;
import static org.adhuc.library.website.catalog.BooksMother.DU_CONTRAT_SOCIAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.annotation.DirtiesContext.MethodMode.BEFORE_METHOD;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestToUriTemplate;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

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

    private static final int TIMEOUT_DURATION_MS = 100;

    @Autowired
    private MockRestServiceServer mockServer;
    @Autowired
    private CatalogRestClient catalogRestClient;

    @Test
    @DisplayName("list books page without timeout")
    void getPage() {
        var response = new DefaultResourceLoader().getResource("classpath:client/catalog/page-0-size-10.json");
        mockServer.expect(requestToUriTemplate("http://localhost:12345/test/api/v1/catalog?page=0&size=10"))
                .andExpect(header("Accept-Language", "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3"))
                .andRespond(withStatus(PARTIAL_CONTENT).body(response).contentType(APPLICATION_JSON));

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
    }

    @Test
    @DisplayName("fail listing books page when timeout is reached")
    void getPageTimeout() {
        var response = new DefaultResourceLoader().getResource("classpath:client/catalog/page-0-size-10.json");
        mockServer.expect(requestToUriTemplate("http://localhost:12345/test/api/v1/catalog?page=0&size=10")).andRespond(request -> {
            try {
                Thread.sleep(TIMEOUT_DURATION_MS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            return withStatus(PARTIAL_CONTENT).body(response).contentType(APPLICATION_JSON).createResponse(request);
        });

        assertThrows(NoFallbackAvailableException.class, () -> catalogRestClient.listBooks("fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3"));
    }

    @Test
    @DisplayName("fail listing books page when server return an error")
    void getPageServerError() {
        mockServer.expect(requestToUriTemplate("http://localhost:12345/test/api/v1/catalog?page=0&size=10"))
                .andRespond(withStatus(INTERNAL_SERVER_ERROR));

        assertThrows(NoFallbackAvailableException.class, () -> catalogRestClient.listBooks("fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3"));
    }

    @Test
    @DirtiesContext(methodMode = BEFORE_METHOD)
    @DisplayName("open the circuit breaker when server fails multiple times while listing books page")
    void getPageMultipleErrorsOpenCircuitBreaker() {
        IntStream.rangeClosed(1, 5)
                .forEach(i -> mockServer.expect(requestToUriTemplate("http://localhost:12345/test/api/v1/catalog?page=0&size=10"))
                        .andRespond(withStatus(INTERNAL_SERVER_ERROR)));
        IntStream.rangeClosed(1, 10)
                .forEach(i -> assertThrows(NoFallbackAvailableException.class, () -> catalogRestClient.listBooks("fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3")));
        mockServer.verify();
    }

    @Test
    @DisplayName("get book details without timeout")
    void getBook() {
        var response = new DefaultResourceLoader().getResource("classpath:client/catalog/book-contrat-social.json");
        mockServer.expect(requestToUriTemplate("http://localhost:12345/test/api/v1/books/{id}", DU_CONTRAT_SOCIAL.id()))
                .andExpect(header("Accept-Language", "fr"))
                .andRespond(withStatus(OK).body(response).contentType(APPLICATION_JSON));

        var actual = catalogRestClient.getBook(DU_CONTRAT_SOCIAL.id(), "fr");
        assertThat(actual).isEqualTo(DU_CONTRAT_SOCIAL);
    }

    @Test
    @DisplayName("fail retrieving book details when timeout is reached")
    void getBookTimeout() {
        var response = new DefaultResourceLoader().getResource("classpath:client/catalog/book-contrat-social.json");
        mockServer.expect(requestToUriTemplate("http://localhost:12345/test/api/v1/books/{id}", DU_CONTRAT_SOCIAL.id()))
                .andRespond(request -> {
                    try {
                        Thread.sleep(TIMEOUT_DURATION_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    return withStatus(OK).body(response).contentType(APPLICATION_JSON).createResponse(request);
                });

        assertThrows(NoFallbackAvailableException.class, () -> catalogRestClient.getBook(DU_CONTRAT_SOCIAL.id(), "fr"));
    }

    @Test
    @DisplayName("fail retrieving book details when server return an error")
    void getBookServerError() {
        mockServer.expect(requestToUriTemplate("http://localhost:12345/test/api/v1/books/{id}", DU_CONTRAT_SOCIAL.id()))
                .andRespond(withStatus(INTERNAL_SERVER_ERROR));

        assertThrows(NoFallbackAvailableException.class, () -> catalogRestClient.getBook(DU_CONTRAT_SOCIAL.id(), "fr"));
    }

    @Test
    @DirtiesContext(methodMode = BEFORE_METHOD)
    @DisplayName("open the circuit breaker when server fails multiple times while retrieving book details")
    void getBookMultipleErrorsOpenCircuitBreaker() {
        IntStream.rangeClosed(1, 5)
                .forEach(i -> mockServer.expect(requestToUriTemplate("http://localhost:12345/test/api/v1/books/{id}", DU_CONTRAT_SOCIAL.id()))
                        .andRespond(withStatus(INTERNAL_SERVER_ERROR)));
        IntStream.rangeClosed(1, 10)
                .forEach(i -> assertThrows(NoFallbackAvailableException.class, () -> catalogRestClient.getBook(DU_CONTRAT_SOCIAL.id(), "fr")));
        mockServer.verify();
    }

    @TestConfiguration
    static class CatalogRestClientTestConfiguration {
        @Bean
        CatalogRestClientProperties properties() {
            return new CatalogRestClientProperties("http://localhost:12345/test", false);
        }

        @Bean
        CatalogRestClient catalogRestClient(RestClient.Builder restClientBuilder, CircuitBreakerFactory<?, ?> circuitBreakerFactory,
                                            CatalogRestClientProperties properties) {
            return new CatalogRestClient(restClientBuilder, null, circuitBreakerFactory, properties);
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
