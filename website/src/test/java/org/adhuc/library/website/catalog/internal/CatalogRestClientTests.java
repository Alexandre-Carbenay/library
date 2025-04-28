package org.adhuc.library.website.catalog.internal;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.adhuc.library.website.catalog.Book;
import org.adhuc.library.website.support.pagination.NavigablePage;
import org.adhuc.library.website.support.pagination.NavigablePageImpl;
import org.adhuc.library.website.support.pagination.NavigablePageImpl.Link;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.*;
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
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.nio.charset.Charset;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.adhuc.library.website.catalog.BooksMother.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@ExtendWith(MockitoExtension.class)
@DisplayName("Catalog REST client should")
class CatalogRestClientTests {

    private CatalogRestClientProperties properties;
    private MockWebServer mockServer;
    private WebClient.Builder webClientBuilder;
    @Mock
    private CircuitBreakerFactory<?, ?> circuitBreakerFactory;
    private CatalogRestClient catalogRestClient;

    @BeforeEach
    void setUp() throws Exception {
        mockServer = new MockWebServer();
        mockServer.start(12345);
        properties = new CatalogRestClientProperties(mockServer.url("/").toString(), false);

        webClientBuilder = WebClient.builder();
    }

    @AfterEach
    void tearDown() throws Exception {
        mockServer.shutdown();
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

            var catalogSpringReactiveClient = new CatalogSpringReactiveClient(webClientBuilder, null, properties);
            catalogRestClient = new CatalogRestClient(catalogSpringReactiveClient, circuitBreakerFactory);
        }

        @Test
        @DisplayName("list books for the default page")
        void getDefaultPage() throws Exception {
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

        @ParameterizedTest
        @MethodSource("pageProvider")
        @DisplayName("list books for a page")
        void getPage(int page, int size, String acceptLanguages, Resource responseBody, int expectedTotalPages, int expectedSize, boolean hasPrev, boolean hasNext) throws Exception {
            var response = new MockResponse()
                    .setResponseCode(206)
                    .setHeader("Content-Type", APPLICATION_JSON_VALUE)
                    .setBody(responseBody.getContentAsString(Charset.defaultCharset()));
            mockServer.enqueue(response);

            var actual = catalogRestClient.listBooks(PageRequest.of(page, size), acceptLanguages);
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

            assertThat(mockServer.getRequestCount()).isEqualTo(1);
            var request = mockServer.takeRequest();
            SoftAssertions.assertSoftly(s -> {
                s.assertThat(request.getPath()).isEqualTo("/api/v1/catalog?page=%d&size=%d", page, size);
                s.assertThat(request.getHeader("Accept-Language")).isEqualTo(acceptLanguages);
            });
        }

        static Stream<Arguments> pageProvider() {
            var resourceLoader = new DefaultResourceLoader();
            return Stream.of(
                    Arguments.of(0, 10, "fr",
                            resourceLoader.getResource("classpath:client/catalog/page-0-size-10.json"), 7, 10, false, true),
                    Arguments.of(0, 10, "en",
                            resourceLoader.getResource("classpath:client/catalog/page-0-size-10.json"), 7, 10, false, true),
                    Arguments.of(0, 10, "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3",
                            resourceLoader.getResource("classpath:client/catalog/page-0-size-10.json"), 7, 10, false, true),
                    Arguments.of(1, 10, "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3",
                            resourceLoader.getResource("classpath:client/catalog/page-1-size-10.json"), 7, 10, true, true),
                    Arguments.of(6, 10, "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3",
                            resourceLoader.getResource("classpath:client/catalog/page-6-size-10.json"), 7, 7, true, false),
                    Arguments.of(0, 50, "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3",
                            resourceLoader.getResource("classpath:client/catalog/page-0-size-50.json"), 2, 50, false, true),
                    Arguments.of(1, 50, "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3",
                            resourceLoader.getResource("classpath:client/catalog/page-1-size-50.json"), 2, 17, true, false)
            );
        }

        @ParameterizedTest
        @MethodSource("pageBooksProvider")
        @DisplayName("list books for a page with expected books contained and non expected books not contained")
        void getPageContainingBooks(int page, int size, String acceptLanguages, Resource responseBody, List<Book> expectedBooks, List<Book> nonExpectedBooks) throws Exception {
            var response = new MockResponse()
                    .setResponseCode(206)
                    .setHeader("Content-Type", APPLICATION_JSON_VALUE)
                    .setBody(responseBody.getContentAsString(Charset.defaultCharset()));
            mockServer.enqueue(response);

            var actual = catalogRestClient.listBooks(PageRequest.of(page, size), acceptLanguages);
            assertThat(actual.getContent()).containsAll(expectedBooks).doesNotContainAnyElementsOf(nonExpectedBooks);

            assertThat(mockServer.getRequestCount()).isEqualTo(1);
            var request = mockServer.takeRequest();
            SoftAssertions.assertSoftly(s -> {
                s.assertThat(request.getPath()).isEqualTo("/api/v1/catalog?page=%d&size=%d", page, size);
                s.assertThat(request.getHeader("Accept-Language")).isEqualTo(acceptLanguages);
            });
        }

        static Stream<Arguments> pageBooksProvider() {
            var resourceLoader = new DefaultResourceLoader();
            return Stream.of(
                    Arguments.of(0, 10, "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3",
                            resourceLoader.getResource("classpath:client/catalog/page-0-size-10.json"),
                            List.of(DU_CONTRAT_SOCIAL, LA_COMMUNAUTE_DE_L_ANNEAU),
                            List.of(A_DANCE_WITH_DRAGONS_FR)
                    ),
                    Arguments.of(1, 10, "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3",
                            resourceLoader.getResource("classpath:client/catalog/page-1-size-10.json"),
                            List.of(A_DANCE_WITH_DRAGONS_FR),
                            List.of(DU_CONTRAT_SOCIAL, LA_COMMUNAUTE_DE_L_ANNEAU)
                    ),
                    Arguments.of(0, 50, "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3",
                            resourceLoader.getResource("classpath:client/catalog/page-0-size-50.json"),
                            List.of(DU_CONTRAT_SOCIAL, LA_COMMUNAUTE_DE_L_ANNEAU, A_DANCE_WITH_DRAGONS_FR, BULLSHIT_JOBS),
                            List.of(LA_FERME_DES_ANIMAUX)
                    )
            );
        }

        @ParameterizedTest
        @MethodSource("pageBooksFromLinkProvider")
        @DisplayName("list books for a link from the previously browsed page with expected books contained and non expected books not contained")
        void getPageFromLink(NavigablePage<Book> currentPage, String linkName, String acceptLanguages,
                             String url, Resource responseBody, List<Book> expectedBooks, List<Book> nonExpectedBooks) throws Exception {
            var response = new MockResponse()
                    .setResponseCode(206)
                    .setHeader("Content-Type", APPLICATION_JSON_VALUE)
                    .setBody(responseBody.getContentAsString(Charset.defaultCharset()));
            mockServer.enqueue(response);

            var actual = catalogRestClient.listBooks(currentPage, linkName, acceptLanguages);
            assertThat(actual.getContent()).containsAll(expectedBooks).doesNotContainAnyElementsOf(nonExpectedBooks);

            assertThat(mockServer.getRequestCount()).isEqualTo(1);
            var request = mockServer.takeRequest();
            SoftAssertions.assertSoftly(s -> {
                s.assertThat(request.getPath()).isEqualTo(url);
                s.assertThat(request.getHeader("Accept-Language")).isEqualTo(acceptLanguages);
            });
        }

        static Stream<Arguments> pageBooksFromLinkProvider() {
            var resourceLoader = new DefaultResourceLoader();

            var page0Size10 = new NavigablePageImpl<>(
                    List.of(DU_CONTRAT_SOCIAL, LA_COMMUNAUTE_DE_L_ANNEAU),
                    PageRequest.of(0, 10), 67,
                    List.of(
                            new Link("first", "http://localhost:12345/test/api/v1/catalog?page=0&size=10"),
                            new Link("next", "http://localhost:12345/test/api/v1/catalog?page=1&size=10"),
                            new Link("last", "http://localhost:12345/test/api/v1/catalog?page=6&size=10")
                    )
            );
            var page1Size10 = new NavigablePageImpl<>(
                    List.of(A_DANCE_WITH_DRAGONS_FR),
                    PageRequest.of(1, 10), 67,
                    List.of(
                            new Link("first", "http://localhost:12345/test/api/v1/catalog?page=0&size=10"),
                            new Link("prev", "http://localhost:12345/test/api/v1/catalog?page=0&size=10"),
                            new Link("next", "http://localhost:12345/test/api/v1/catalog?page=2&size=10"),
                            new Link("last", "http://localhost:12345/test/api/v1/catalog?page=6&size=10")
                    )
            );
            var page1Size50 = new NavigablePageImpl<>(
                    List.of(LA_FERME_DES_ANIMAUX),
                    PageRequest.of(1, 50), 67,
                    List.of(
                            new Link("first", "http://localhost:12345/test/api/v1/catalog?page=0&size=50"),
                            new Link("prev", "http://localhost:12345/test/api/v1/catalog?page=0&size=50"),
                            new Link("last", "http://localhost:12345/test/api/v1/catalog?page=1&size=50")
                    )
            );

            return Stream.of(
                    Arguments.of(page1Size10, "first",
                            "fr",
                            "/test/api/v1/catalog?page=0&size=10",
                            resourceLoader.getResource("classpath:client/catalog/page-0-size-10.json"),
                            List.of(DU_CONTRAT_SOCIAL, LA_COMMUNAUTE_DE_L_ANNEAU),
                            List.of(A_DANCE_WITH_DRAGONS_FR)
                    ),
                    Arguments.of(page1Size10, "first",
                            "en",
                            "/test/api/v1/catalog?page=0&size=10",
                            resourceLoader.getResource("classpath:client/catalog/page-0-size-10.json"),
                            List.of(DU_CONTRAT_SOCIAL, LA_COMMUNAUTE_DE_L_ANNEAU),
                            List.of(A_DANCE_WITH_DRAGONS_FR)
                    ),
                    Arguments.of(page1Size10, "first",
                            "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3",
                            "/test/api/v1/catalog?page=0&size=10",
                            resourceLoader.getResource("classpath:client/catalog/page-0-size-10.json"),
                            List.of(DU_CONTRAT_SOCIAL, LA_COMMUNAUTE_DE_L_ANNEAU),
                            List.of(A_DANCE_WITH_DRAGONS_FR)
                    ),
                    Arguments.of(page1Size10, "prev",
                            "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3",
                            "/test/api/v1/catalog?page=0&size=10",
                            resourceLoader.getResource("classpath:client/catalog/page-0-size-10.json"),
                            List.of(DU_CONTRAT_SOCIAL, LA_COMMUNAUTE_DE_L_ANNEAU),
                            List.of(A_DANCE_WITH_DRAGONS_FR)
                    ),
                    Arguments.of(page0Size10, "next",
                            "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3",
                            "/test/api/v1/catalog?page=1&size=10",
                            resourceLoader.getResource("classpath:client/catalog/page-1-size-10.json"),
                            List.of(A_DANCE_WITH_DRAGONS_FR),
                            List.of(DU_CONTRAT_SOCIAL, LA_COMMUNAUTE_DE_L_ANNEAU)
                    ),
                    Arguments.of(page0Size10, "last",
                            "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3",
                            "/test/api/v1/catalog?page=6&size=10",
                            resourceLoader.getResource("classpath:client/catalog/page-6-size-10.json"),
                            List.of(LA_FERME_DES_ANIMAUX),
                            List.of(DU_CONTRAT_SOCIAL, LA_COMMUNAUTE_DE_L_ANNEAU, HAMLET)
                    ),
                    Arguments.of(page1Size50, "prev",
                            "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3",
                            "/test/api/v1/catalog?page=0&size=50",
                            resourceLoader.getResource("classpath:client/catalog/page-0-size-50.json"),
                            List.of(DU_CONTRAT_SOCIAL, LA_COMMUNAUTE_DE_L_ANNEAU, A_DANCE_WITH_DRAGONS_FR, BULLSHIT_JOBS),
                            List.of(LA_FERME_DES_ANIMAUX)
                    )
            );
        }

        @ParameterizedTest
        @MethodSource("pageBooksFromUnknownLinkProvider")
        @DisplayName("refuse listing books for an unknown link from the previously browsed page")
        void getPageFromUnknownLink(NavigablePage<Book> currentPage, String linkName) {
            assertThrows(IllegalArgumentException.class, () -> catalogRestClient.listBooks(currentPage, linkName, "fr"));
        }

        static Stream<Arguments> pageBooksFromUnknownLinkProvider() {
            var page0Size10 = new NavigablePageImpl<>(
                    List.of(DU_CONTRAT_SOCIAL, LA_COMMUNAUTE_DE_L_ANNEAU),
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

        @Test
        @DisplayName("fail retrieving unknown book")
        void getUnknownBook() throws Exception {
            var response = new MockResponse()
                    .setResponseCode(404)
                    .setHeader("Content-Type", APPLICATION_JSON_VALUE)
                    .setBody("""
                            {
                              "type": "/problems/unknown-entity",
                              "status": 404,
                              "title": "Unknown book",
                              "detail": "No book exists with id 'b94329cb-8767-4438-b802-d85a268fb3e3'"
                            }
                            """);
            mockServer.enqueue(response);

            var id = "b94329cb-8767-4438-b802-d85a268fb3e3";
            assertThrows(WebClientResponseException.NotFound.class, () -> catalogRestClient.getBook(id, "fr"));

            assertThat(mockServer.getRequestCount()).isEqualTo(1);
            var request = mockServer.takeRequest();
            SoftAssertions.assertSoftly(s -> {
                s.assertThat(request.getPath()).isEqualTo("/api/v1/books/%s", id);
                s.assertThat(request.getHeader("Accept-Language")).isEqualTo("fr");
            });
        }

        @ParameterizedTest
        @MethodSource("knownBookProvider")
        @DisplayName("get book details for a book ID")
        void getBook(String id, String acceptLanguages, List<MockResponse> responses, Book expected) throws Exception {
            responses.forEach(mockServer::enqueue);

            var actual = catalogRestClient.getBook(id, acceptLanguages);
            assertThat(actual).isEqualTo(expected);

            assertThat(mockServer.getRequestCount()).isEqualTo(3);
            var request = mockServer.takeRequest();
            SoftAssertions.assertSoftly(s -> {
                s.assertThat(request.getPath()).isEqualTo("/api/v1/books/%s", id);
                s.assertThat(request.getHeader("Accept-Language")).isEqualTo(acceptLanguages);
            });
        }

        static Stream<Arguments> knownBookProvider() throws Exception {
            var resourceLoader = new DefaultResourceLoader();
            return Stream.of(
                    Arguments.of(
                            DU_CONTRAT_SOCIAL.id(),
                            "fr",
                            List.of(
                                    new MockResponse()
                                            .setResponseCode(200)
                                            .setHeader("Content-Type", APPLICATION_JSON_VALUE)
                                            .setBody(resourceLoader.getResource("classpath:client/catalog/book-contrat-social.json")
                                                    .getContentAsString(Charset.defaultCharset())),
                                    new MockResponse()
                                            .setResponseCode(200)
                                            .setHeader("Content-Type", APPLICATION_JSON_VALUE)
                                            .setBody(resourceLoader.getResource("classpath:client/catalog/edition-contrat-social-9782081275232.json")
                                                    .getContentAsString(Charset.defaultCharset())),
                                    new MockResponse()
                                            .setResponseCode(200)
                                            .setHeader("Content-Type", APPLICATION_JSON_VALUE)
                                            .setBody(resourceLoader.getResource("classpath:client/catalog/edition-contrat-social-9782290385050.json")
                                                    .getContentAsString(Charset.defaultCharset()))
                            ),
                            DU_CONTRAT_SOCIAL_WITH_EDITIONS
                    ),
                    Arguments.of(
                            A_DANCE_WITH_DRAGONS_FR.id(),
                            "fr",
                            List.of(
                                    new MockResponse()
                                            .setResponseCode(200)
                                            .setHeader("Content-Type", APPLICATION_JSON_VALUE)
                                            .setBody(resourceLoader.getResource("classpath:client/catalog/book-dance-with-dragons-fr.json")
                                                    .getContentAsString(Charset.defaultCharset())),
                                    new MockResponse()
                                            .setResponseCode(200)
                                            .setHeader("Content-Type", APPLICATION_JSON_VALUE)
                                            .setBody(resourceLoader.getResource("classpath:client/catalog/edition-dance-with-dragons-9780553801477.json")
                                                    .getContentAsString(Charset.defaultCharset())),
                                    new MockResponse()
                                            .setResponseCode(200)
                                            .setHeader("Content-Type", APPLICATION_JSON_VALUE)
                                            .setBody(resourceLoader.getResource("classpath:client/catalog/edition-dance-with-dragons-9782290221709.json")
                                                    .getContentAsString(Charset.defaultCharset()))
                            ),
                            A_DANCE_WITH_DRAGONS_FR_WITH_EDITIONS
                    ),
                    Arguments.of(
                            A_DANCE_WITH_DRAGONS_FR.id(),
                            "fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3",
                            List.of(
                                    new MockResponse()
                                            .setResponseCode(200)
                                            .setHeader("Content-Type", APPLICATION_JSON_VALUE)
                                            .setBody(resourceLoader.getResource("classpath:client/catalog/book-dance-with-dragons-fr.json")
                                                    .getContentAsString(Charset.defaultCharset())),
                                    new MockResponse()
                                            .setResponseCode(200)
                                            .setHeader("Content-Type", APPLICATION_JSON_VALUE)
                                            .setBody(resourceLoader.getResource("classpath:client/catalog/edition-dance-with-dragons-9780553801477.json")
                                                    .getContentAsString(Charset.defaultCharset())),
                                    new MockResponse()
                                            .setResponseCode(200)
                                            .setHeader("Content-Type", APPLICATION_JSON_VALUE)
                                            .setBody(resourceLoader.getResource("classpath:client/catalog/edition-dance-with-dragons-9782290221709.json")
                                                    .getContentAsString(Charset.defaultCharset()))
                            ),
                            A_DANCE_WITH_DRAGONS_FR_WITH_EDITIONS
                    ),
                    Arguments.of(
                            A_DANCE_WITH_DRAGONS_EN.id(),
                            "en",
                            List.of(
                                    new MockResponse()
                                            .setResponseCode(200)
                                            .setHeader("Content-Type", APPLICATION_JSON_VALUE)
                                            .setBody(resourceLoader.getResource("classpath:client/catalog/book-dance-with-dragons-en.json")
                                                    .getContentAsString(Charset.defaultCharset())),
                                    new MockResponse()
                                            .setResponseCode(200)
                                            .setHeader("Content-Type", APPLICATION_JSON_VALUE)
                                            .setBody(resourceLoader.getResource("classpath:client/catalog/edition-dance-with-dragons-9780553801477.json")
                                                    .getContentAsString(Charset.defaultCharset())),
                                    new MockResponse()
                                            .setResponseCode(200)
                                            .setHeader("Content-Type", APPLICATION_JSON_VALUE)
                                            .setBody(resourceLoader.getResource("classpath:client/catalog/edition-dance-with-dragons-9782290221709.json")
                                                    .getContentAsString(Charset.defaultCharset()))
                            ),
                            A_DANCE_WITH_DRAGONS_EN_WITH_EDITIONS
                    )
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

            var catalogSpringReactiveClient = new CatalogSpringReactiveClient(webClientBuilder, null, properties);
            catalogRestClient = new CatalogRestClient(catalogSpringReactiveClient, circuitBreakerFactory);
        }

        @Test
        @DisplayName("fail listing books for a page")
        void getPage() {
            var error = assertThrows(NoFallbackAvailableException.class, () -> catalogRestClient.listBooks(PageRequest.of(0, 10), "fr"));
            assertThat(error).hasMessage("No fallback available");
        }

        @Test
        @DisplayName("fail retrieving book")
        void getBook() {
            var error = assertThrows(NoFallbackAvailableException.class, () -> catalogRestClient.getBook("b6608a30-1e9b-4ae0-a89d-624c3ca85da4", "fr"));
            assertThat(error).hasMessage("No fallback available");
        }
    }

}
