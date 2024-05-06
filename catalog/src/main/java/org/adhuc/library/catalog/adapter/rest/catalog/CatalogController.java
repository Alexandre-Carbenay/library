package org.adhuc.library.catalog.adapter.rest.catalog;

import org.springframework.hateoas.mediatype.hal.HalModelBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.HttpStatus.PARTIAL_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "/api/v1/catalog", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
public class CatalogController {

    @GetMapping
    public ResponseEntity<Object> getCatalog() {
        var responseBody = HalModelBuilder.halModelOf(new Response(new Page(0, 0, 0, 0)))
                .link(linkTo(methodOn(CatalogController.class).getCatalog()).withSelfRel())
                .build();
        return ResponseEntity.status(PARTIAL_CONTENT).body(responseBody);
    }

    private record Response(Page page) {
    }

    private record Page(int size, int totalElements, int totalPages, int number) {
    }

}
