package org.adhuc.library.support.rest.validation;

import org.adhuc.library.support.rest.ProblemError;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class InvalidRequestBuilder {

    public static ProblemDetail invalidRequest(List<? extends ProblemError> errors) {
        var problem = ProblemDetail.forStatus(BAD_REQUEST);
        problem.setType(URI.create("/problems/invalid-request"));
        problem.setTitle("Request validation error");
        problem.setDetail("Request parameters or body are invalid compared to the OpenAPI specification. See errors for more information");
        problem.setProperty("errors", errors);
        return problem;
    }

}
