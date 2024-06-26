package org.adhuc.library.catalog.adapter.rest.books;

import org.adhuc.library.catalog.adapter.rest.Error;
import org.adhuc.library.catalog.adapter.rest.authors.AuthorModelAssembler;
import org.adhuc.library.catalog.books.Book;
import org.adhuc.library.catalog.books.BooksService;
import org.springframework.hateoas.LinkRelation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.hateoas.mediatype.hal.HalModelBuilder.halModelOf;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "/api/v1/books", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
public class BooksController {

    private final BookDetailsModelAssembler bookModelAssembler;
    private final AuthorModelAssembler authorModelAssembler;
    private final BooksService booksService;

    public BooksController(BookDetailsModelAssembler bookModelAssembler,
                           AuthorModelAssembler authorModelAssembler,
                           BooksService booksService) {
        this.bookModelAssembler = bookModelAssembler;
        this.authorModelAssembler = authorModelAssembler;
        this.booksService = booksService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBook(@PathVariable UUID id) {
        var book = booksService.getBook(id);
        return book.isPresent()
                ? prepareBookResponse(book.get())
                : prepareNotFoundResponse(id);
    }

    private ResponseEntity<Object> prepareBookResponse(Book book) {
        var bookDetails = bookModelAssembler.toModel(book);
        var authors = authorModelAssembler.toCollectionModel(book.authors()).getContent();
        return ResponseEntity.status(OK)
                .body(halModelOf(bookDetails)
                        .links(bookDetails.getLinks())
                        .embed(authors, LinkRelation.of("authors"))
                        .build()
                );
    }

    private ResponseEntity<Error> prepareNotFoundResponse(UUID id) {
        return ResponseEntity.status(NOT_FOUND)
                .body(new Error(
                        ZonedDateTime.now(),
                        404,
                        "ENTITY_NOT_FOUND",
                        STR."No book exists with id '\{id}'"
                ));
    }

}
