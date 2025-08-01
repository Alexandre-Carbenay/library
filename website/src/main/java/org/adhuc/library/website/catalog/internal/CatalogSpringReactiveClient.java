package org.adhuc.library.website.catalog.internal;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientSsl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static java.util.Comparator.comparing;
import static org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
@EnableConfigurationProperties(CatalogRestClientProperties.class)
class CatalogSpringReactiveClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogSpringReactiveClient.class);

    private final WebClient webClient;

    CatalogSpringReactiveClient(WebClient.Builder webClientBuilder,
                                @Nullable WebClientSsl ssl,
                                CatalogRestClientProperties properties) {
        var builder = webClientBuilder.baseUrl(properties.baseUrl());
        if (properties.sslEnabled()) {
            if (ssl == null) {
                throw new IllegalStateException("Unable to initialize secured web client without SSL configuration");
            }
            builder = builder.apply(ssl.fromBundle("catalog"));
        }
        this.webClient = builder.build();
    }

    Mono<BooksPage> listBooks(String acceptLanguages, String uri, Object... uriVariables) {
        LOGGER.debug("List books through URI {} with variables {} in accept languages {}", uri, uriVariables, acceptLanguages);
        return webClient.get()
                .uri(uri, uriVariables)
                .accept(APPLICATION_JSON)
                .header(ACCEPT_LANGUAGE, acceptLanguages)
                .retrieve()
                .bodyToMono(BooksPage.class);
    }

    Mono<BookDetailDto> retrieveBookDetails(String id, String acceptLanguages) {
        LOGGER.debug("Retrieve book {} details in accept languages {}", id, acceptLanguages);
        return webClient.get()
                .uri("/api/v1/books/{id}", id)
                .accept(APPLICATION_JSON)
                .header(ACCEPT_LANGUAGE, acceptLanguages)
                .retrieve()
                .bodyToMono(BookDetailDto.class);
    }

    Mono<List<EditionDetailDto>> retrieveBookEditions(BookDetailDto book) {
        return Flux.fromStream(book.editions().stream())
                .flatMap(edition -> {
                    LOGGER.debug("Retrieve book {} edition {}", book.id(), edition.isbn());
                    return webClient.get()
                            .uri(edition.selfLink())
                            .accept(APPLICATION_JSON)
                            .retrieve()
                            .bodyToMono(EditionDetailDto.class);
                })
                .collectList().map(editions -> {
                            var sortedEditions = new ArrayList<>(editions);
                            sortedEditions.sort(comparing(edition -> book.indexOfEdition(edition.isbn())));
                            return sortedEditions;
                        }
                );
    }

}
