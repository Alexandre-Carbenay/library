package org.adhuc.library.catalog.adapter.rest.authors;

import org.adhuc.library.catalog.adapter.rest.books.BookModel;
import org.adhuc.library.catalog.adapter.rest.books.BookModelAssembler;
import org.adhuc.library.catalog.authors.Author;
import org.adhuc.library.catalog.authors.AuthorsService;
import org.adhuc.library.catalog.books.Book;
import org.adhuc.library.catalog.books.BooksService;
import org.jspecify.annotations.Nullable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.*;

import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.hateoas.mediatype.hal.HalModelBuilder.halModelOf;
import static org.springframework.http.HttpHeaders.CONTENT_LANGUAGE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;

@RestController
@RequestMapping(path = "/api/v1/authors", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
public class AuthorsController {

    private final AuthorDetailsModelAssembler authorModelAssembler;
    private final BookModelAssembler bookModelAssembler;
    private final AuthorsService authorsService;
    private final BooksService booksService;

    public AuthorsController(AuthorDetailsModelAssembler authorModelAssembler,
                             BookModelAssembler bookModelAssembler,
                             AuthorsService authorsService,
                             BooksService booksService) {
        this.authorModelAssembler = authorModelAssembler;
        this.bookModelAssembler = bookModelAssembler;
        this.authorsService = authorsService;
        this.booksService = booksService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAuthor(@PathVariable UUID id, @RequestHeader @Nullable HttpHeaders headers) {
        var languages = headers != null ? headers.getAcceptLanguageAsLocales() : List.<Locale>of();
        var author = authorsService.getAuthor(id);
        return author.isPresent()
                ? prepareAuthorResponse(author.get(), languages)
                : prepareNotFoundResponse(id);
    }

    private ResponseEntity<Object> prepareAuthorResponse(Author author, List<Locale> languages) {
        var notableBooks = booksService.getNotableBooks(author.id());
        var responseLanguage = determineResponseLanguage(notableBooks, languages);
        var authorDetails = authorModelAssembler.toModel(author);
        var responseBody = halModelOf(authorDetails);
        var response = ResponseEntity.status(OK);

        if (!notableBooks.isEmpty()) {
            CollectionModel<BookModel> books;
            if (responseLanguage.isEmpty()) {
                books = bookModelAssembler.toCollectionModel(notableBooks);
            } else {
                books = bookModelAssembler.toCollectionModel(notableBooks, responseLanguage.get());
                response.header(CONTENT_LANGUAGE, responseLanguage.get());
            }
            responseBody.embed(books.getContent(), LinkRelation.of("notable_books"));
        }
        return response.body(responseBody.build());
    }

    private Optional<String> determineResponseLanguage(Collection<Book> books, List<Locale> languages) {
        return languages.stream()
                .filter(language -> !books.stream().filter(book -> book.acceptsLanguage(language)).toList().isEmpty())
                .map(Locale::getLanguage)
                .findFirst();
    }

    private ResponseEntity<Problem> prepareNotFoundResponse(UUID id) {
        return ResponseEntity.status(NOT_FOUND).contentType(APPLICATION_PROBLEM_JSON)
                .body(Problem.create()
                        .withType(URI.create("/problems/unknown-entity"))
                        .withStatus(NOT_FOUND)
                        .withTitle("Unknown author")
                        .withDetail(STR."No author exists with id '\{id}'")
                );
    }

}
