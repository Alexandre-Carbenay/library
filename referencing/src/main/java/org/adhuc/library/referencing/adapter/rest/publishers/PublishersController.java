package org.adhuc.library.referencing.adapter.rest.publishers;

import jakarta.validation.Valid;
import org.adhuc.library.referencing.publishers.Publisher;
import org.adhuc.library.referencing.publishers.PublishersConsultationService;
import org.adhuc.library.referencing.publishers.PublishersReferencingService;
import org.adhuc.library.referencing.publishers.ReferencePublisher;
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
@RequestMapping(path = "/api/v1/publishers", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
class PublishersController {

    private final PagedResourcesAssembler<Publisher> pageAssembler;
    private final PublisherModelAssembler publisherModelAssembler;
    private final PublishersConsultationService publishersConsultationService;
    private final PublishersReferencingService publishersReferencingService;

    public PublishersController(PagedResourcesAssembler<Publisher> pageAssembler,
                                PublisherModelAssembler publisherModelAssembler,
                                PublishersConsultationService publishersConsultationService,
                                PublishersReferencingService publishersReferencingService) {
        this.pageAssembler = pageAssembler;
        this.publisherModelAssembler = publisherModelAssembler;
        this.publishersConsultationService = publishersConsultationService;
        this.publishersReferencingService = publishersReferencingService;
    }

    @GetMapping
    ResponseEntity<Object> listPublishers(@RequestParam(required = false, defaultValue = "0") Integer page,
                                          @RequestParam(required = false, defaultValue = "50") Integer size) {
        var request = PageRequest.of(page, size);
        var publishersPage = publishersConsultationService.getPage(request);
        var response = pageAssembler.toModel(publishersPage, linkTo(methodOn(PublishersController.class).listPublishers(page, size)).withSelfRel());

        var responseBody = HalModelBuilder.halModelOf(requireNonNull(response.getMetadata()))
                .links(response.getLinks());
        if (!publishersPage.isEmpty()) {
            var publishers = publisherModelAssembler.toCollectionModel(publishersPage).getContent();
            responseBody = responseBody.embed(publishers, LinkRelation.of("publishers"));
        }

        return ResponseEntity.status(PARTIAL_CONTENT).body(responseBody.build());
    }

    @PostMapping
    ResponseEntity<?> referencePublisher(@Valid @RequestBody PublisherReferencingRequest request) {
        var command = new ReferencePublisher(request.name());
        var publisher = Objects.requireNonNull(publishersReferencingService.referencePublisher(command));

        var model = publisherModelAssembler.toModel(publisher);
        return ResponseEntity.status(CREATED).contentType(HAL_JSON)
                .location(URI.create(model.getLink(SELF).map(Link::getHref).orElseThrow()))
                .body(model);
    }

    @GetMapping("/{id}")
    ResponseEntity<?> getPublisher(@PathVariable UUID id) {
        return ResponseEntity.status(NOT_IMPLEMENTED).build();
    }

}
