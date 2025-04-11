package org.adhuc.library.catalog.adapter.rest.catalog;

import org.adhuc.library.catalog.adapter.rest.authors.AuthorModelAssembler;
import org.adhuc.library.catalog.adapter.rest.books.BookModelAssembler;
import org.adhuc.library.catalog.adapter.rest.editions.EditionModelAssembler;
import org.adhuc.library.catalog.authors.Author;
import org.adhuc.library.catalog.books.Book;
import org.adhuc.library.catalog.books.CatalogService;
import org.adhuc.library.catalog.editions.EditionsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.mediatype.hal.HalModelBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.HttpHeaders.CONTENT_LANGUAGE;
import static org.springframework.http.HttpStatus.PARTIAL_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "/api/v1/catalog", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
public class CatalogController {

    private final PagedResourcesAssembler<Book> pageAssembler;
    private final BookModelAssembler bookModelAssembler;
    private final EditionModelAssembler editionModelAssembler;
    private final AuthorModelAssembler authorModelAssembler;
    private final CatalogService catalogService;
    private final EditionsService editionsService;

    public CatalogController(PagedResourcesAssembler<Book> pageAssembler,
                             BookModelAssembler bookModelAssembler,
                             EditionModelAssembler editionModelAssembler,
                             AuthorModelAssembler authorModelAssembler,
                             CatalogService catalogService,
                             EditionsService editionsService) {
        this.pageAssembler = pageAssembler;
        this.bookModelAssembler = bookModelAssembler;
        this.editionModelAssembler = editionModelAssembler;
        this.authorModelAssembler = authorModelAssembler;
        this.catalogService = catalogService;
        this.editionsService = editionsService;
    }

    @GetMapping
    public ResponseEntity<Object> getCatalog(@RequestParam(required = false, defaultValue = "0") Integer page,
                                             @RequestParam(required = false, defaultValue = "50") Integer size,
                                             @RequestHeader HttpHeaders headers) {
        var pageRequest = PageRequest.of(page, size);
        var catalogLanguage = getRequestLocaleOrDefault(headers);
        var catalogPage = catalogService.getPage(pageRequest, catalogLanguage);
        var response = pageAssembler.toModel(
                catalogPage,
                linkTo(methodOn(CatalogController.class).getCatalog(pageRequest.getPageNumber(), pageRequest.getPageSize(), headers)).withSelfRel()
        );

        var responseBody = HalModelBuilder.halModelOf(requireNonNull(response.getMetadata()))
                .links(response.getLinks());
        if (!catalogPage.isEmpty()) {
            var books = bookModelAssembler.toCollectionModel(catalogPage, catalogLanguage.getLanguage()).getContent();
            var editionsInPage = editionsService.getBooksEditions(catalogPage.stream().map(Book::id).toList());
            var editions = editionModelAssembler.toCollectionModel(editionsInPage).getContent();
            var authors = authorModelAssembler.toCollectionModel(booksAuthors(catalogPage)).getContent();
            responseBody = responseBody
                    // Temporarily keep the editions relation to let API consumers move to the editions relation
                    .embed(books, LinkRelation.of("books"))
                    .embed(editions, LinkRelation.of("editions"))
                    .embed(authors, LinkRelation.of("authors"));
        }
        return ResponseEntity.status(PARTIAL_CONTENT).header(CONTENT_LANGUAGE, catalogLanguage.getLanguage()).body(responseBody.build());
    }

    private Locale getRequestLocaleOrDefault(HttpHeaders headers) {
        var acceptLanguages = headers.getAcceptLanguageAsLocales();
        return acceptLanguages.isEmpty()
                ? Locale.FRENCH
                : acceptLanguages.getFirst();
    }

    private Set<Author> booksAuthors(Page<Book> books) {
        return books.stream().map(Book::authors).flatMap(Collection::stream).collect(toSet());
    }

}
