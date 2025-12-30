package org.adhuc.library.catalog.adapter.rest.editions;

import org.adhuc.library.catalog.editions.Edition;
import org.adhuc.library.catalog.editions.EditionsService;
import org.adhuc.library.support.rest.ProblemError.ParameterError;
import org.apache.commons.validator.routines.ISBNValidator;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

import static org.adhuc.library.support.rest.validation.InvalidRequestBuilder.invalidRequest;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.hateoas.mediatype.hal.HalModelBuilder.halModelOf;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;

@RestController
@RequestMapping(path = "/api/v1/editions", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
public class EditionsController {

    private final EditionDetailsModelAssembler editionModelAssembler;
    private final EditionsService editionsService;
    private final ISBNValidator isbnValidator = new ISBNValidator();

    public EditionsController(EditionDetailsModelAssembler editionModelAssembler,
                              EditionsService editionsService) {
        this.editionModelAssembler = editionModelAssembler;
        this.editionsService = editionsService;
    }

    @GetMapping("/{isbn}")
    public ResponseEntity<?> getEdition(@PathVariable String isbn) {
        if (!isbnValidator.isValid(isbn)) {
            return prepareInvalidIsbnResponse(isbn);
        }
        var edition = editionsService.getEdition(isbn);
        return edition.isPresent()
                ? prepareEditionResponse(edition.get())
                : prepareNotFoundResponse(isbn);
    }

    private ResponseEntity<Object> prepareEditionResponse(Edition edition) {
        var editionDetails = editionModelAssembler.toModel(edition);
        return ResponseEntity.status(OK)
                .body(halModelOf(editionDetails)
                        .links(editionDetails.getLinks())
                        .build()
                );
    }

    private ResponseEntity<Problem> prepareInvalidIsbnResponse(String isbn) {
        var problem = invalidRequest(List.of(
                new ParameterError("Input string '" + isbn + "' is not a valid ISBN", "isbn")
        ));
        return ResponseEntity.badRequest().contentType(APPLICATION_PROBLEM_JSON).body(problem);
    }

    private ResponseEntity<Problem> prepareNotFoundResponse(String isbn) {
        return ResponseEntity.status(NOT_FOUND).contentType(APPLICATION_PROBLEM_JSON)
                .body(Problem.create()
                        .withType(URI.create("/problems/unknown-entity"))
                        .withStatus(NOT_FOUND)
                        .withTitle("Unknown edition")
                        .withDetail("No edition exists with ISBN '" + isbn + "'")
                );
    }

}
