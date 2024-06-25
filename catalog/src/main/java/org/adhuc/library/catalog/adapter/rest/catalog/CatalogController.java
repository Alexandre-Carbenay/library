package org.adhuc.library.catalog.adapter.rest.catalog;

import org.adhuc.library.catalog.adapter.rest.authors.AuthorModelAssembler;
import org.adhuc.library.catalog.adapter.rest.books.BookModelAssembler;
import org.adhuc.library.catalog.authors.Author;
import org.adhuc.library.catalog.books.Book;
import org.adhuc.library.catalog.books.CatalogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.mediatype.hal.HalModelBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.HttpStatus.PARTIAL_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "/api/v1/catalog", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
public class CatalogController {

    private final PagedResourcesAssembler<Book> pageAssembler;
    private final BookModelAssembler bookModelAssembler;
    private final AuthorModelAssembler authorModelAssembler;
    private final CatalogService catalogService;

    public CatalogController(PagedResourcesAssembler<Book> pageAssembler,
                             BookModelAssembler bookModelAssembler,
                             AuthorModelAssembler authorModelAssembler,
                             CatalogService catalogService) {
        this.pageAssembler = pageAssembler;
        this.bookModelAssembler = bookModelAssembler;
        this.authorModelAssembler = authorModelAssembler;
        this.catalogService = catalogService;
    }

    @GetMapping
    public ResponseEntity<Object> getCatalog(@RequestParam(required = false, defaultValue = "0") Integer page,
                                             @RequestParam(required = false, defaultValue = "50") Integer size) {
        var pageRequest = PageRequest.of(page, size);
        var catalogPage = catalogService.getPage(pageRequest);
        var response = pageAssembler.toModel(
                catalogPage,
                linkTo(methodOn(CatalogController.class).getCatalog(pageRequest.getPageNumber(), pageRequest.getPageSize())).withSelfRel()
        );

        var responseBody = HalModelBuilder.halModelOf(requireNonNull(response.getMetadata()))
                .links(response.getLinks());
        if (!catalogPage.isEmpty()) {
            var books = bookModelAssembler.toCollectionModel(catalogPage).getContent();
            var authors = authorModelAssembler.toCollectionModel(booksAuthors(catalogPage)).getContent();
            responseBody = responseBody
                    .embed(books, LinkRelation.of("books"))
                    .embed(authors, LinkRelation.of("authors"));
        }
        return ResponseEntity.status(PARTIAL_CONTENT).body(responseBody.build());
    }

    private Set<Author> booksAuthors(Page<Book> books) {
        return books.stream().map(Book::authors).flatMap(Collection::stream).collect(toSet());
    }

}
