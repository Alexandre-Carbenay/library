package org.adhuc.library.website.catalog.internal;

import org.adhuc.library.website.catalog.Book;
import org.adhuc.library.website.support.pagination.NavigablePage;
import org.adhuc.library.website.support.pagination.NavigablePageImpl;
import org.adhuc.library.website.support.pagination.NavigablePageImpl.Link;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.adhuc.library.website.catalog.BooksMother.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.PARTIAL_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestToUriTemplate;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("Catalog REST client should")
class CatalogRestClientTests {

    private CatalogRestClientProperties properties;
    private MockRestServiceServer mockServer;
    private RestClient.Builder restClientBuilder;
    @Mock
    private CircuitBreakerFactory<?, ?> circuitBreakerFactory;
    private CatalogRestClient catalogRestClient;

    @BeforeEach
    void setUp() {
        properties = new CatalogRestClientProperties("http://localhost:12345/test", false);

        var restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();

        restClientBuilder = RestClient.builder(restTemplate);
    }

    @Nested
    @DisplayName("with server responding within the expected timeout")
    class WithServerResponding {
        @BeforeEach
        void setUp() {
            when(circuitBreakerFactory.create("catalog")).thenReturn(new CircuitBreaker() {
                @Override
                public <T> T run(Supplier<T> toRun, Function<Throwable, T> fallback) {
                    return toRun.get();
                }
            });

            catalogRestClient = new CatalogRestClient(restClientBuilder, null, circuitBreakerFactory, properties);
        }

        @Test
        @DisplayName("list books for the default page")
        void getDefaultPage() {
            var response = new DefaultResourceLoader().getResource("classpath:client/catalog/page-0-size-10.json");
            mockServer.expect(requestToUriTemplate("http://localhost:12345/test/api/v1/catalog?page=0&size=10")).andRespond(
                    withStatus(PARTIAL_CONTENT).body(response).contentType(APPLICATION_JSON)
            );

            var actual = catalogRestClient.listBooks();
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

        @ParameterizedTest
        @MethodSource("pageProvider")
        @DisplayName("list books for a page")
        void getPage(int page, int size, Resource response, int expectedTotalPages, int expectedSize, boolean hasPrev, boolean hasNext) {
            mockServer.expect(requestToUriTemplate("http://localhost:12345/test/api/v1/catalog?page={page}&size={size}", page, size)).andRespond(
                    withStatus(PARTIAL_CONTENT).body(response).contentType(APPLICATION_JSON)
            );

            var actual = catalogRestClient.listBooks(PageRequest.of(page, size));
            SoftAssertions.assertSoftly(s -> {
                s.assertThat(actual.getSize()).isEqualTo(size);
                s.assertThat(actual.getTotalElements()).isEqualTo(67);
                s.assertThat(actual.getTotalPages()).isEqualTo(expectedTotalPages);
                s.assertThat(actual.getNumber()).isEqualTo(page);
                s.assertThat(actual.getContent().size()).isEqualTo(expectedSize);
                s.assertThat(actual.hasLink("first")).isTrue();
                s.assertThat(actual.hasLink("prev")).isEqualTo(hasPrev);
                s.assertThat(actual.hasLink("next")).isEqualTo(hasNext);
                s.assertThat(actual.hasLink("last")).isTrue();
            });
        }

        static Stream<Arguments> pageProvider() {
            var resourceLoader = new DefaultResourceLoader();
            return Stream.of(
                    Arguments.of(0, 10, resourceLoader.getResource("classpath:client/catalog/page-0-size-10.json"), 7, 10, false, true),
                    Arguments.of(1, 10, resourceLoader.getResource("classpath:client/catalog/page-1-size-10.json"), 7, 10, true, true),
                    Arguments.of(6, 10, resourceLoader.getResource("classpath:client/catalog/page-6-size-10.json"), 7, 7, true, false),
                    Arguments.of(0, 50, resourceLoader.getResource("classpath:client/catalog/page-0-size-50.json"), 2, 50, false, true),
                    Arguments.of(1, 50, resourceLoader.getResource("classpath:client/catalog/page-1-size-50.json"), 2, 17, true, false)
            );
        }

        @ParameterizedTest
        @MethodSource("pageBooksProvider")
        @DisplayName("list books for a page with expected books contained and non expected books not contained")
        void getPageContainingBooks(int page, int size, Resource response, List<Book> expectedBooks, List<Book> nonExpectedBooks) {
            mockServer.expect(requestToUriTemplate("http://localhost:12345/test/api/v1/catalog?page={page}&size={size}", page, size)).andRespond(
                    withStatus(PARTIAL_CONTENT).body(response).contentType(APPLICATION_JSON)
            );

            var actual = catalogRestClient.listBooks(PageRequest.of(page, size));
            assertThat(actual.getContent()).containsAll(expectedBooks).doesNotContainAnyElementsOf(nonExpectedBooks);
        }

        static Stream<Arguments> pageBooksProvider() {
            var resourceLoader = new DefaultResourceLoader();
            return Stream.of(
                    Arguments.of(0, 10, resourceLoader.getResource("classpath:client/catalog/page-0-size-10.json"),
                            List.of(DU_CONTRAT_SOCIAL, LA_FRATERNITE_DE_L_ANNEAU),
                            List.of(A_DANCE_WITH_DRAGONS)
                    ),
                    Arguments.of(1, 10, resourceLoader.getResource("classpath:client/catalog/page-1-size-10.json"),
                            List.of(A_DANCE_WITH_DRAGONS),
                            List.of(DU_CONTRAT_SOCIAL, LA_FRATERNITE_DE_L_ANNEAU)
                    ),
                    Arguments.of(0, 50, resourceLoader.getResource("classpath:client/catalog/page-0-size-50.json"),
                            List.of(DU_CONTRAT_SOCIAL, LA_FRATERNITE_DE_L_ANNEAU, A_DANCE_WITH_DRAGONS),
                            List.of(BULLSHIT_JOBS)
                    )
            );
        }

        @ParameterizedTest
        @MethodSource("pageBooksFromLinkProvider")
        @DisplayName("list books for a link from the previously browsed page with expected books contained and non expected books not contained")
        void getPageFromLink(NavigablePage<Book> currentPage, String linkName, String url, Resource response, List<Book> expectedBooks, List<Book> nonExpectedBooks) {
            mockServer.expect(requestTo(url)).andRespond(
                    withStatus(PARTIAL_CONTENT).body(response).contentType(APPLICATION_JSON)
            );

            var actual = catalogRestClient.listBooks(currentPage, linkName);
            assertThat(actual.getContent()).containsAll(expectedBooks).doesNotContainAnyElementsOf(nonExpectedBooks);
        }

        static Stream<Arguments> pageBooksFromLinkProvider() {
            var resourceLoader = new DefaultResourceLoader();

            var page0Size10 = new NavigablePageImpl<>(
                    List.of(DU_CONTRAT_SOCIAL, LA_FRATERNITE_DE_L_ANNEAU),
                    PageRequest.of(0, 10), 67,
                    List.of(
                            new Link("first", "http://localhost:12345/test/api/v1/catalog?page=0&size=10"),
                            new Link("next", "http://localhost:12345/test/api/v1/catalog?page=1&size=10"),
                            new Link("last", "http://localhost:12345/test/api/v1/catalog?page=6&size=10")
                    )
            );
            var page1Size10 = new NavigablePageImpl<>(
                    List.of(A_DANCE_WITH_DRAGONS),
                    PageRequest.of(1, 10), 67,
                    List.of(
                            new Link("first", "http://localhost:12345/test/api/v1/catalog?page=0&size=10"),
                            new Link("prev", "http://localhost:12345/test/api/v1/catalog?page=0&size=10"),
                            new Link("next", "http://localhost:12345/test/api/v1/catalog?page=2&size=10"),
                            new Link("last", "http://localhost:12345/test/api/v1/catalog?page=6&size=10")
                    )
            );
            var page1Size50 = new NavigablePageImpl<>(
                    List.of(BULLSHIT_JOBS),
                    PageRequest.of(1, 50), 67,
                    List.of(
                            new Link("first", "http://localhost:12345/test/api/v1/catalog?page=0&size=50"),
                            new Link("prev", "http://localhost:12345/test/api/v1/catalog?page=0&size=50"),
                            new Link("last", "http://localhost:12345/test/api/v1/catalog?page=1&size=50")
                    )
            );

            return Stream.of(
                    Arguments.of(page1Size10, "first",
                            "http://localhost:12345/test/api/v1/catalog?page=0&size=10",
                            resourceLoader.getResource("classpath:client/catalog/page-0-size-10.json"),
                            List.of(DU_CONTRAT_SOCIAL, LA_FRATERNITE_DE_L_ANNEAU),
                            List.of(A_DANCE_WITH_DRAGONS)
                    ),
                    Arguments.of(page1Size10, "prev",
                            "http://localhost:12345/test/api/v1/catalog?page=0&size=10",
                            resourceLoader.getResource("classpath:client/catalog/page-0-size-10.json"),
                            List.of(DU_CONTRAT_SOCIAL, LA_FRATERNITE_DE_L_ANNEAU),
                            List.of(A_DANCE_WITH_DRAGONS)
                    ),
                    Arguments.of(page0Size10, "next",
                            "http://localhost:12345/test/api/v1/catalog?page=1&size=10",
                            resourceLoader.getResource("classpath:client/catalog/page-1-size-10.json"),
                            List.of(A_DANCE_WITH_DRAGONS),
                            List.of(DU_CONTRAT_SOCIAL, LA_FRATERNITE_DE_L_ANNEAU)
                    ),
                    Arguments.of(page0Size10, "last",
                            "http://localhost:12345/test/api/v1/catalog?page=6&size=10",
                            resourceLoader.getResource("classpath:client/catalog/page-6-size-10.json"),
                            List.of(HAMLET),
                            List.of(DU_CONTRAT_SOCIAL, LA_FRATERNITE_DE_L_ANNEAU)
                    ),
                    Arguments.of(page1Size50, "prev",
                            "http://localhost:12345/test/api/v1/catalog?page=0&size=50",
                            resourceLoader.getResource("classpath:client/catalog/page-0-size-50.json"),
                            List.of(DU_CONTRAT_SOCIAL, LA_FRATERNITE_DE_L_ANNEAU, A_DANCE_WITH_DRAGONS),
                            List.of(BULLSHIT_JOBS)
                    )
            );
        }

        @ParameterizedTest
        @MethodSource("pageBooksFromUnknownLinkProvider")
        @DisplayName("refuse listing books for an unknown link from the previously browsed page")
        void getPageFromUnknownLink(NavigablePage<Book> currentPage, String linkName) {
            assertThrows(IllegalArgumentException.class, () -> catalogRestClient.listBooks(currentPage, linkName));
        }

        static Stream<Arguments> pageBooksFromUnknownLinkProvider() {
            var page0Size10 = new NavigablePageImpl<>(
                    List.of(DU_CONTRAT_SOCIAL, LA_FRATERNITE_DE_L_ANNEAU),
                    PageRequest.of(0, 10), 67,
                    List.of(
                            new Link("first", "http://localhost:12345/test/api/v1/catalog?page=0&size=10"),
                            new Link("next", "http://localhost:12345/test/api/v1/catalog?page=1&size=10"),
                            new Link("last", "http://localhost:12345/test/api/v1/catalog?page=6&size=10")
                    )
            );
            var page6Size10 = new NavigablePageImpl<>(
                    List.of(HAMLET),
                    PageRequest.of(6, 10), 67,
                    List.of(
                            new Link("first", "http://localhost:12345/test/api/v1/catalog?page=0&size=10"),
                            new Link("prev", "http://localhost:12345/test/api/v1/catalog?page=5&size=10"),
                            new Link("last", "http://localhost:12345/test/api/v1/catalog?page=6&size=10")
                    )
            );

            return Stream.of(
                    Arguments.of(page0Size10, "prev"),
                    Arguments.of(page6Size10, "next")
            );
        }
    }

    @Nested
    @DisplayName("with server reaching the timeout")
    class WithServerTimeout {
        @BeforeEach
        void setUp() {
            when(circuitBreakerFactory.create("catalog")).thenReturn(new CircuitBreaker() {
                @Override
                public <T> T run(Supplier<T> toRun, Function<Throwable, T> fallback) {
                    throw new NoFallbackAvailableException("No fallback available", new RuntimeException("circuit breaker failed"));
                }
            });

            catalogRestClient = new CatalogRestClient(restClientBuilder, null, circuitBreakerFactory, properties);
        }

        @Test
        @DisplayName("fail listing books for a page")
        void getPage() {
            var error = assertThrows(NoFallbackAvailableException.class, () -> catalogRestClient.listBooks(PageRequest.of(0, 10)));
            assertThat(error).hasMessage("No fallback available");
        }
    }

}
