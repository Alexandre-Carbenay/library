package org.adhuc.library.website.catalog.internal;

import org.adhuc.library.website.catalog.Book;
import org.adhuc.library.website.catalog.CatalogClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@EnableConfigurationProperties(CatalogRestClientProperties.class)
class CatalogRestClient implements CatalogClient {

    private final RestClient restClient;

    CatalogRestClient(RestClient.Builder restClientBuilder, CatalogRestClientProperties properties) {
        this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
    }

    @Override
    public Page<Book> listBooks() {
        return listBooks(PageRequest.of(0, 10));
    }

    @Override
    public Page<Book> listBooks(Pageable pageable) {
        return restClient.get()
                .uri("/catalog?page={page}&size={size}", pageable.getPageNumber(), pageable.getPageSize())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(BooksPage.class);
    }

}
