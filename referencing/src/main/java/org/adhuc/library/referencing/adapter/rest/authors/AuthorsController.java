package org.adhuc.library.referencing.adapter.rest.authors;

import org.adhuc.library.referencing.authors.Author;
import org.adhuc.library.referencing.authors.AuthorsConsultationService;
import org.adhuc.library.referencing.authors.AuthorsReferencingService;
import org.adhuc.library.referencing.authors.ReferenceAuthor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.mediatype.hal.HalModelBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
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
@RequestMapping(path = "/api/v1/authors", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
class AuthorsController {

    private final PagedResourcesAssembler<Author> pageAssembler;
    private final AuthorModelAssembler authorModelAssembler;
    private final AuthorsConsultationService authorsConsultationService;
    private final AuthorsReferencingService authorsReferencingService;

    AuthorsController(PagedResourcesAssembler<Author> pageAssembler,
                      AuthorModelAssembler authorModelAssembler,
                      AuthorsConsultationService authorsConsultationService,
                      AuthorsReferencingService authorsReferencingService) {
        this.pageAssembler = pageAssembler;
        this.authorModelAssembler = authorModelAssembler;
        this.authorsConsultationService = authorsConsultationService;
        this.authorsReferencingService = authorsReferencingService;
    }

    @GetMapping
    ResponseEntity<Object> listAuthors(@RequestParam(required = false, defaultValue = "0") Integer page,
                                       @RequestParam(required = false, defaultValue = "50") Integer size) {
        var request = PageRequest.of(page, size);
        var authorsPage = authorsConsultationService.getPage(request);
        var response = pageAssembler.toModel(authorsPage, linkTo(methodOn(AuthorsController.class).listAuthors(page, size)).withSelfRel());

        var responseBody = HalModelBuilder.halModelOf(requireNonNull(response.getMetadata()))
                .links(response.getLinks());
        if (!authorsPage.isEmpty()) {
            var authors = authorModelAssembler.toCollectionModel(authorsPage).getContent();
            responseBody = responseBody.embed(authors, LinkRelation.of("authors"));
        }

        return ResponseEntity.status(PARTIAL_CONTENT).body(responseBody.build());
    }

    @PostMapping
    ResponseEntity<?> referenceAuthor(@RequestBody AuthorReferencingRequest request) {
        var command = new ReferenceAuthor(request.name(), request.dateOfBirth(), request.dateOfDeath());
        var author = Objects.requireNonNull(authorsReferencingService.referenceAuthor(command));

        var model = authorModelAssembler.toModel(author);
        return ResponseEntity.status(CREATED).contentType(HAL_JSON)
                .location(URI.create(model.getLink(SELF).map(Link::getHref).orElseThrow()))
                .body(model);
    }

    @GetMapping("/{id}")
    ResponseEntity<?> getAuthor(@PathVariable UUID id) {
        return ResponseEntity.status(NOT_IMPLEMENTED).build();
    }

}
