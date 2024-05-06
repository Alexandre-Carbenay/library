package org.adhuc.library.catalog.adapter.rest.authors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.http.HttpStatus.NOT_IMPLEMENTED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "/api/v1/authors", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
public class AuthorsController {

    @GetMapping("/{id}")
    public ResponseEntity<AuthorModel> getAuthor(@PathVariable UUID id) {
        return ResponseEntity.status(NOT_IMPLEMENTED).build();
    }

}
