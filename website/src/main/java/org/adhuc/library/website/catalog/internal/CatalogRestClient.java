package org.adhuc.library.website.catalog.internal;

import org.adhuc.library.website.catalog.Book;
import org.adhuc.library.website.catalog.CatalogClient;
import org.adhuc.library.website.support.pagination.NavigablePage;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(CatalogRestClientProperties.class)
class CatalogRestClient implements CatalogClient {

    private final CatalogSpringReactiveClient client;
    private final CircuitBreaker circuitBreaker;

    CatalogRestClient(CatalogSpringReactiveClient client,
                      CircuitBreakerFactory<?, ?> circuitBreakerFactory) {
        this.client = client;
        this.circuitBreaker = circuitBreakerFactory.create("catalog");
    }

    @Override
    public NavigablePage<Book> listBooks(String acceptLanguages) {
        return listBooks(PageRequest.of(0, 10), acceptLanguages);
    }

    NavigablePage<Book> listBooks(Pageable pageable, String acceptLanguages) {
        return circuitBreaker.run(() -> client.
                listBooks(acceptLanguages, "/api/v1/catalog?page={page}&size={size}", pageable.getPageNumber(), pageable.getPageSize())
                .block()
        );
    }

    @Override
    public NavigablePage<Book> listBooks(NavigablePage<Book> current, String linkName, String acceptLanguages) {
        if (!current.hasLink(linkName)) {
            throw new IllegalArgumentException("Cannot browse to page through link " + linkName + " from current");
        }
        return circuitBreaker.run(() -> client.
                listBooks(acceptLanguages, current.getLink(linkName).orElseThrow())
                .block()
        );
    }

    @Override
    public Book getBook(String id, String acceptLanguages) {
        return circuitBreaker.run(() -> client.retrieveBookDetails(id, acceptLanguages)
                .map(book -> {
                    var editions = client.retrieveBookEditions(book).block().stream()
                            .map(EditionDetailDto::toEdition)
                            .toList();
                    return book.asBook(editions);
                }).block());
    }

}
