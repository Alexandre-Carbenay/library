package org.adhuc.library.website.catalog.internal;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.restclient.autoconfigure.RestClientSsl;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Comparator.comparing;
import static org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
@EnableConfigurationProperties(CatalogRestClientProperties.class)
class CatalogSpringClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogSpringClient.class);

    private final RestClient restClient;

    CatalogSpringClient(RestClient.Builder restClientBuilder,
                        @Nullable RestClientSsl ssl,
                        CatalogRestClientProperties properties) {
        var builder = restClientBuilder.baseUrl(properties.baseUrl());
        if (properties.sslEnabled()) {
            if (ssl == null) {
                throw new IllegalStateException("Unable to initialize secured web client without SSL configuration");
            }
            builder = builder.apply(ssl.fromBundle("catalog"));
        }
        this.restClient = builder.build();
    }

    BooksPage listBooks(String acceptLanguages, String uri, Object... uriVariables) {
        LOGGER.debug("List books through URI {} with variables {} in accept languages {}", uri, uriVariables, acceptLanguages);
        var response = restClient.get()
                .uri(uri, uriVariables)
                .accept(APPLICATION_JSON)
                .header(ACCEPT_LANGUAGE, acceptLanguages)
                .retrieve()
                .body(BooksPage.class);
        return Objects.requireNonNull(response);
    }

    BookDetailDto retrieveBookDetails(String id, String acceptLanguages) {
        LOGGER.debug("Retrieve book {} details in accept languages {}", id, acceptLanguages);
        var response = restClient.get()
                .uri("/api/v1/books/{id}", id)
                .accept(APPLICATION_JSON)
                .header(ACCEPT_LANGUAGE, acceptLanguages)
                .retrieve()
                .body(BookDetailDto.class);
        return Objects.requireNonNull(response);
    }

    List<EditionDetailDto> retrieveBookEditions(BookDetailDto book) {
        var editions = book.editions().stream()
                .map(edition -> {
                    LOGGER.debug("Retrieve book {} edition {}", book.id(), edition.isbn());
                    return restClient.get()
                            .uri(edition.selfLink())
                            .accept(APPLICATION_JSON)
                            .retrieve()
                            .body(EditionDetailDto.class);
                }).toList();
        var sortedEditions = new ArrayList<>(editions);
        sortedEditions.sort(comparing(edition -> book.indexOfEdition(edition.isbn())));
        return sortedEditions;
    }

}
