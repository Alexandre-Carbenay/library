package org.adhuc.library.website.catalog.internal;

import org.adhuc.library.website.catalog.Book;
import org.adhuc.library.website.catalog.CatalogClient;
import org.adhuc.library.website.support.pagination.NavigablePage;
import org.springframework.boot.autoconfigure.web.client.RestClientSsl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import static org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE;

@Component
@EnableConfigurationProperties(CatalogRestClientProperties.class)
class CatalogRestClient implements CatalogClient {

    private final RestClient restClient;
    private final CircuitBreaker circuitBreaker;

    CatalogRestClient(RestClient.Builder restClientBuilder,
                      RestClientSsl ssl,
                      CircuitBreakerFactory<?, ?> circuitBreakerFactory,
                      CatalogRestClientProperties properties) {
        var builder = restClientBuilder.baseUrl(properties.baseUrl());
        if (properties.sslEnabled()) {
            builder = builder.apply(ssl.fromBundle("catalog"));
        }
        this.restClient = builder.build();
        this.circuitBreaker = circuitBreakerFactory.create("catalog");
    }

    @Override
    public NavigablePage<Book> listBooks(String acceptLanguages) {
        return listBooks(PageRequest.of(0, 10), acceptLanguages);
    }

    NavigablePage<Book> listBooks(Pageable pageable, String acceptLanguages) {
        return listBooks(acceptLanguages, "/api/v1/catalog?page={page}&size={size}", pageable.getPageNumber(), pageable.getPageSize());
    }

    @Override
    public NavigablePage<Book> listBooks(NavigablePage<Book> current, String linkName, String acceptLanguages) {
        if (!current.hasLink(linkName)) {
            throw new IllegalArgumentException("Cannot browse to page through link " + linkName + " from current");
        }
        return listBooks(acceptLanguages, current.getLink(linkName).orElseThrow());
    }

    private NavigablePage<Book> listBooks(String acceptLanguages, String uri, Object... uriVariables) {
        return circuitBreaker.run(() -> restClient.get()
                .uri(uri, uriVariables)
                .accept(MediaType.APPLICATION_JSON)
                .header(ACCEPT_LANGUAGE, acceptLanguages)
                .retrieve()
                .body(BooksPage.class));
    }

}
