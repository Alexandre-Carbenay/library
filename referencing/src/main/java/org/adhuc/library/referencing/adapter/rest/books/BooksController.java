package org.adhuc.library.referencing.adapter.rest.books;

import jakarta.validation.Valid;
import org.adhuc.library.referencing.books.Book;
import org.adhuc.library.referencing.books.BooksConsultationService;
import org.adhuc.library.referencing.books.BooksReferencingService;
import org.adhuc.library.referencing.books.ReferenceBook;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.mediatype.hal.HalModelBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static org.springframework.hateoas.IanaLinkRelations.SELF;
import static org.springframework.hateoas.MediaTypes.HAL_JSON;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "/api/v1/books", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
class BooksController {

    private final PagedResourcesAssembler<Book> pageAssembler;
    private final BookModelAssembler bookModelAssembler;
    private final BooksConsultationService booksConsultationService;
    private final BooksReferencingService booksReferencingService;

    BooksController(PagedResourcesAssembler<Book> pageAssembler, BookModelAssembler bookModelAssembler,
                    BooksConsultationService booksConsultationService, BooksReferencingService booksReferencingService) {
        this.pageAssembler = pageAssembler;
        this.bookModelAssembler = bookModelAssembler;
        this.booksConsultationService = booksConsultationService;
        this.booksReferencingService = booksReferencingService;
    }

    @GetMapping
    ResponseEntity<Object> listBooks(@RequestParam(required = false, defaultValue = "0") Integer page,
                                     @RequestParam(required = false, defaultValue = "50") Integer size) {
        var request = PageRequest.of(page, size);
        var booksPage = booksConsultationService.getPage(request);
        var response = pageAssembler.toModel(booksPage, linkTo(methodOn(BooksController.class).listBooks(page, size)).withSelfRel());

        var responseBody = HalModelBuilder.halModelOf(requireNonNull(response.getMetadata()))
                .links(response.getLinks());
        if (!booksPage.isEmpty()) {
            var authors = bookModelAssembler.toCollectionModel(booksPage).getContent();
            responseBody = responseBody.embed(authors, LinkRelation.of("books"));
        }

        return ResponseEntity.status(PARTIAL_CONTENT).body(responseBody.build());
    }

    @PostMapping
    ResponseEntity<?> referenceBook(@Valid @RequestBody BookReferencingRequest request) {
        var command = new ReferenceBook(List.copyOf(request.authors()), request.originalLanguage(), request.details().stream()
                .map(detail -> new Book.LocalizedDetail(detail.language(), detail.title(), detail.description()))
                .toList()
        );
        var book = Objects.requireNonNull(booksReferencingService.referenceBook(command));

        var model = bookModelAssembler.toModel(book);
        return ResponseEntity.status(CREATED).contentType(HAL_JSON)
                .location(URI.create(model.getLink(SELF).map(Link::getHref).orElseThrow()))
                .body(model);
    }

    @GetMapping("/{id}")
    ResponseEntity<?> getBook(@PathVariable UUID id) {
        return ResponseEntity.status(NOT_IMPLEMENTED).build();
    }

}
