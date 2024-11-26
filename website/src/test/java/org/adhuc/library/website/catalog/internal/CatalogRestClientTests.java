package org.adhuc.library.website.catalog.internal;

import org.adhuc.library.website.catalog.Book;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Stream;

import static org.adhuc.library.website.catalog.BooksMother.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestToUriTemplate;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@DisplayName("Catalog REST client should")
class CatalogRestClientTests {

    private MockRestServiceServer mockServer;
    private CatalogRestClient catalogRestClient;

    @BeforeEach
    void setUp() {
        var properties = new CatalogRestClientProperties("http://localhost:12345/test");

        var restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();

        var restClientBuilder = RestClient.builder(restTemplate);
        catalogRestClient = new CatalogRestClient(restClientBuilder, properties);
    }

    @Test
    @DisplayName("list books for the default page")
    void getDefaultPage() {
        var response = new DefaultResourceLoader().getResource("classpath:client/catalog/page-0-size-10.json");
        mockServer.expect(requestToUriTemplate("http://localhost:12345/test/catalog?page=0&size=10")).andRespond(
                withSuccess().body(response).contentType(APPLICATION_JSON)
        );

        var actual = catalogRestClient.listBooks();
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(actual.getSize()).isEqualTo(10);
            s.assertThat(actual.getTotalElements()).isEqualTo(67);
            s.assertThat(actual.getTotalPages()).isEqualTo(7);
            s.assertThat(actual.getNumber()).isEqualTo(0);
            s.assertThat(actual.getContent().size()).isEqualTo(10);
        });
    }

    @ParameterizedTest
    @MethodSource("pageProvider")
    @DisplayName("list books for a page")
    void getPage(int page, int size, Resource response, int expectedTotalPages, int expectedSize) {
        mockServer.expect(requestToUriTemplate("http://localhost:12345/test/catalog?page={page}&size={size}", page, size)).andRespond(
                withSuccess().body(response).contentType(APPLICATION_JSON)
        );

        var actual = catalogRestClient.listBooks(PageRequest.of(page, size));
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(actual.getSize()).isEqualTo(size);
            s.assertThat(actual.getTotalElements()).isEqualTo(67);
            s.assertThat(actual.getTotalPages()).isEqualTo(expectedTotalPages);
            s.assertThat(actual.getNumber()).isEqualTo(page);
            s.assertThat(actual.getContent().size()).isEqualTo(expectedSize);
        });
    }

    static Stream<Arguments> pageProvider() {
        var resourceLoader = new DefaultResourceLoader();
        return Stream.of(
                Arguments.of(0, 10, resourceLoader.getResource("classpath:client/catalog/page-0-size-10.json"), 7, 10),
                Arguments.of(1, 10, resourceLoader.getResource("classpath:client/catalog/page-1-size-10.json"), 7, 10),
                Arguments.of(6, 10, resourceLoader.getResource("classpath:client/catalog/page-6-size-10.json"), 7, 7),
                Arguments.of(0, 50, resourceLoader.getResource("classpath:client/catalog/page-0-size-50.json"), 2, 50),
                Arguments.of(1, 50, resourceLoader.getResource("classpath:client/catalog/page-1-size-50.json"), 2, 17)
        );
    }

    @ParameterizedTest
    @MethodSource("pageBooksProvider")
    @DisplayName("list books for a page with expected books contained and non expected books not contained")
    void getPageContainingBooks(int page, int size, Resource response, List<Book> expectedBooks, List<Book> nonExpectedBooks) {
        mockServer.expect(requestToUriTemplate("http://localhost:12345/test/catalog?page={page}&size={size}", page, size)).andRespond(
                withSuccess().body(response).contentType(APPLICATION_JSON)
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

}
