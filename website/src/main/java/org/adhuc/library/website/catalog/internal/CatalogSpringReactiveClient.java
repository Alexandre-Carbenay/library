package org.adhuc.library.website.catalog.internal;

import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientSsl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
@EnableConfigurationProperties(CatalogRestClientProperties.class)
class CatalogSpringReactiveClient {

    private final WebClient webClient;

    CatalogSpringReactiveClient(WebClient.Builder webClientBuilder,
                                WebClientSsl ssl,
                                CatalogRestClientProperties properties) {
        var builder = webClientBuilder.baseUrl(properties.baseUrl());
        if (properties.sslEnabled()) {
            builder = builder.apply(ssl.fromBundle("catalog"));
        }
        this.webClient = builder.build();
    }

    Mono<BooksPage> listBooks(String acceptLanguages, String uri, Object... uriVariables) {
        return webClient.get()
                .uri(uri, uriVariables)
                .accept(APPLICATION_JSON)
                .header(ACCEPT_LANGUAGE, acceptLanguages)
                .retrieve()
                .bodyToMono(BooksPage.class);
    }

    Mono<BookDetailDto> retrieveBookDetails(String id, String acceptLanguages) {
        return webClient.get()
                .uri("/api/v1/books/{id}", id)
                .accept(APPLICATION_JSON)
                .header(ACCEPT_LANGUAGE, acceptLanguages)
                .retrieve()
                .bodyToMono(BookDetailDto.class);
    }

    Mono<List<EditionDetailDto>> retrieveBookEditions(BookDetailDto book) {
        return Flux.fromStream(book.editions().stream())
                .flatMap(edition -> webClient.get()
                        .uri(edition.selfLink())
                        .accept(APPLICATION_JSON)
                        .retrieve()
                        .bodyToMono(EditionDetailDto.class))
                .collectList();
    }

}
