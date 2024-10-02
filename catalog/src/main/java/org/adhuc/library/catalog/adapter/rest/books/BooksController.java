package org.adhuc.library.catalog.adapter.rest.books;

import org.adhuc.library.catalog.adapter.rest.ProblemError.ParameterError;
import org.adhuc.library.catalog.adapter.rest.authors.AuthorModelAssembler;
import org.adhuc.library.catalog.books.Book;
import org.adhuc.library.catalog.books.BooksService;
import org.apache.commons.validator.routines.ISBNValidator;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

import static org.adhuc.library.catalog.adapter.rest.support.validation.InvalidRequestBuilder.invalidRequest;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.hateoas.mediatype.hal.HalModelBuilder.halModelOf;
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
    private final ISBNValidator isbnValidator = new ISBNValidator();

    public BooksController(BookDetailsModelAssembler bookModelAssembler,
                           AuthorModelAssembler authorModelAssembler,
                           BooksService booksService) {
        this.bookModelAssembler = bookModelAssembler;
        this.authorModelAssembler = authorModelAssembler;
        this.booksService = booksService;
    }

    @GetMapping("/{isbn}")
    public ResponseEntity<?> getBook(@PathVariable String isbn) {
        if (!isbnValidator.isValid(isbn)) {
            return prepareInvalidIsbnResponse(isbn);
        }
        var book = booksService.getBook(isbn);
        return book.isPresent()
                ? prepareBookResponse(book.get())
                : prepareNotFoundResponse(isbn);
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

    private ResponseEntity<Problem> prepareInvalidIsbnResponse(String isbn) {
        var problem = invalidRequest(List.of(
                new ParameterError(STR."Input string \"\{isbn}\" is not a valid ISBN", "isbn")
        ));
        return ResponseEntity.badRequest().contentType(APPLICATION_PROBLEM_JSON).body(problem);
    }

    private ResponseEntity<Problem> prepareNotFoundResponse(String isbn) {
        return ResponseEntity.status(NOT_FOUND).contentType(APPLICATION_PROBLEM_JSON)
                .body(Problem.create()
                        .withType(URI.create("/problems/unknown-entity"))
                        .withStatus(NOT_FOUND)
                        .withTitle("Unknown book")
                        .withDetail(STR."No book exists with ISBN '\{isbn}'")
                );
    }

}
