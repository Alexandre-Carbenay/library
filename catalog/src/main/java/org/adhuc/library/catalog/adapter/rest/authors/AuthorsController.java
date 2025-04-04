package org.adhuc.library.catalog.adapter.rest.authors;

import org.adhuc.library.catalog.adapter.rest.editions.EditionModelAssembler;
import org.adhuc.library.catalog.authors.Author;
import org.adhuc.library.catalog.authors.AuthorsService;
import org.adhuc.library.catalog.editions.EditionsService;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.hateoas.mediatype.hal.HalModelBuilder.halModelOf;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;

@RestController
@RequestMapping(path = "/api/v1/authors", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
public class AuthorsController {

    private final AuthorDetailsModelAssembler authorModelAssembler;
    private final EditionModelAssembler editionModelAssembler;
    private final AuthorsService authorsService;
    private final EditionsService editionsService;

    public AuthorsController(AuthorDetailsModelAssembler authorModelAssembler,
                             EditionModelAssembler editionModelAssembler,
                             AuthorsService authorsService,
                             EditionsService editionsService) {
        this.authorModelAssembler = authorModelAssembler;
        this.editionModelAssembler = editionModelAssembler;
        this.authorsService = authorsService;
        this.editionsService = editionsService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAuthor(@PathVariable UUID id) {
        var author = authorsService.getAuthor(id);
        return author.isPresent()
                ? prepareAuthorResponse(author.get())
                : prepareNotFoundResponse(id);
    }

    private ResponseEntity<Object> prepareAuthorResponse(Author author) {
        var authorDetails = authorModelAssembler.toModel(author);
        var notableEditions = editionsService.getNotableEditions(author.id());
        var responseBody = halModelOf(authorDetails);
        if (!notableEditions.isEmpty()) {
            responseBody.embed(editionModelAssembler.toCollectionModel(
                            notableEditions).getContent(),
                    LinkRelation.of("notable_books"));
        }
        return ResponseEntity.status(OK)
                .body(responseBody.build());
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
