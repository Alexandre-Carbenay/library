package org.adhuc.library.catalog.adapter.rest.support.validation;

import org.adhuc.library.catalog.adapter.rest.ProblemError;
import org.springframework.hateoas.mediatype.problem.Problem;

import java.net.URI;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class InvalidRequestBuilder {

    public static Problem invalidRequest(List<? extends ProblemError> errors) {
        return Problem.create()
                .withType(URI.create("/problems/invalid-request"))
                .withStatus(BAD_REQUEST)
                .withTitle("Request validation error")
                .withDetail("Request parameters or body are invalid compared to the OpenAPI specification. See errors for more information")
                .withProperties(map -> map.put("errors", errors));
    }

}
