package org.adhuc.library.catalog.adapter.rest.books;

import org.adhuc.library.catalog.adapter.rest.authors.AuthorModelAssembler;
import org.adhuc.library.catalog.books.Book;
import org.adhuc.library.catalog.books.BooksService;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.hateoas.mediatype.hal.HalModelBuilder.halModelOf;
import static org.springframework.http.HttpHeaders.CONTENT_LANGUAGE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;

@RestController
@RequestMapping(path = "/api/v1/books", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
public class BooksController {

    private final BookDetailsModelAssembler bookModelAssembler;
    private final AuthorModelAssembler authorModelAssembler;
    private final BooksService booksService;

    public BooksController(BookDetailsModelAssembler bookModelAssembler, AuthorModelAssembler authorModelAssembler, BooksService booksService) {
        this.bookModelAssembler = bookModelAssembler;
        this.authorModelAssembler = authorModelAssembler;
        this.booksService = booksService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBook(@PathVariable UUID id, @RequestHeader HttpHeaders headers) {
        var book = booksService.getBook(id);
        return book.isPresent()
                ? prepareBookResponse(book.get(), headers.getAcceptLanguageAsLocales())
                : prepareNotFoundResponse(id);
    }

    private ResponseEntity<Object> prepareBookResponse(Book book, List<Locale> languages) {
        var responseLanguage = determineResponseLanguage(book, languages);

        var bookDetails = bookModelAssembler.toModel(book, responseLanguage);
        var authors = authorModelAssembler.toCollectionModel(book.authors()).getContent();
        return ResponseEntity.status(OK)
                .header(CONTENT_LANGUAGE, responseLanguage)
                .body(halModelOf(bookDetails)
                        .links(bookDetails.getLinks())
                        .embed(authors, LinkRelation.of("authors"))
                        .build()
                );
    }

    private String determineResponseLanguage(Book book, List<Locale> languages) {
        return languages.stream()
                .filter(book::acceptsLanguage)
                .map(Locale::getLanguage)
                .findFirst()
                .orElseGet(book::originalLanguage);
    }

    private ResponseEntity<Problem> prepareNotFoundResponse(UUID id) {
        return ResponseEntity.status(NOT_FOUND).contentType(APPLICATION_PROBLEM_JSON)
                .body(Problem.create()
                        .withType(URI.create("/problems/unknown-entity"))
                        .withStatus(NOT_FOUND)
                        .withTitle("Unknown book")
                        .withDetail(STR."No book exists with id '\{id}'")
                );
    }

}
