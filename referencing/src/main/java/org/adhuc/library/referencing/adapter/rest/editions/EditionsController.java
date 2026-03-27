package org.adhuc.library.referencing.adapter.rest.editions;

import jakarta.validation.Valid;
import org.adhuc.library.referencing.editions.Edition;
import org.adhuc.library.referencing.editions.EditionsConsultationService;
import org.adhuc.library.referencing.editions.EditionsReferencingService;
import org.adhuc.library.referencing.editions.ReferenceEdition;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.mediatype.hal.HalModelBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static org.springframework.hateoas.IanaLinkRelations.SELF;
import static org.springframework.hateoas.MediaTypes.HAL_JSON;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "/api/v1/editions", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
class EditionsController {

    private final PagedResourcesAssembler<Edition> pageAssembler;
    private final EditionModelAssembler editionModelAssembler;
    private final EditionsConsultationService editionsConsultationService;
    private final EditionsReferencingService editionsReferencingService;

    EditionsController(PagedResourcesAssembler<Edition> pageAssembler, EditionModelAssembler editionModelAssembler,
                       EditionsConsultationService editionsConsultationService, EditionsReferencingService editionsReferencingService) {
        this.pageAssembler = pageAssembler;
        this.editionModelAssembler = editionModelAssembler;
        this.editionsConsultationService = editionsConsultationService;
        this.editionsReferencingService = editionsReferencingService;
    }

    @GetMapping
    ResponseEntity<Object> listEditions(@RequestParam(required = false, defaultValue = "0") Integer page,
                                        @RequestParam(required = false, defaultValue = "50") Integer size) {
        var request = PageRequest.of(page, size);
        var editionsPage = editionsConsultationService.getPage(request);
        var response = pageAssembler.toModel(editionsPage, linkTo(methodOn(EditionsController.class).listEditions(page, size)).withSelfRel());

        var responseBody = HalModelBuilder.halModelOf(requireNonNull(response.getMetadata()))
                .links(response.getLinks());
        if (!editionsPage.isEmpty()) {
            var editions = editionModelAssembler.toCollectionModel(editionsPage).getContent();
            responseBody = responseBody.embed(editions, LinkRelation.of("editions"));
        }

        return ResponseEntity.status(PARTIAL_CONTENT).body(responseBody.build());
    }

    @PostMapping
    ResponseEntity<?> referenceEdition(@Valid @RequestBody EditionReferencingRequest request) {
        var command = new ReferenceEdition(request.isbn(), request.language(), request.book(), request.publisher(),
                request.publicationDate(), request.title(), request.summary());
        var edition = Objects.requireNonNull(editionsReferencingService.referenceEdition(command));

        var model = editionModelAssembler.toModel(edition);
        return ResponseEntity.status(CREATED).contentType(HAL_JSON)
                .location(URI.create(model.getLink(SELF).map(Link::getHref).orElseThrow()))
                .body(model);
    }

    @GetMapping("/{isbn}")
    ResponseEntity<?> getEdition(@PathVariable String isbn) {
        return ResponseEntity.status(NOT_IMPLEMENTED).build();
    }

}
