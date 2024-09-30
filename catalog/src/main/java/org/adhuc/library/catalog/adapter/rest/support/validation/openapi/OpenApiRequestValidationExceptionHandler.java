package org.adhuc.library.catalog.adapter.rest.support.validation.openapi;

import com.atlassian.oai.validator.report.ValidationReport;
import com.atlassian.oai.validator.springmvc.InvalidRequestException;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.adhuc.library.catalog.adapter.rest.ProblemError;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;

@RestControllerAdvice
public class OpenApiRequestValidationExceptionHandler {

    @ExceptionHandler(InvalidRequestException.class)
    ResponseEntity<Object> handleInvalidOpenApiRequest(InvalidRequestException e, WebRequest request) {
        var problem = Problem.create()
                .withType(URI.create("/problems/invalid-request"))
                .withStatus(BAD_REQUEST)
                .withTitle("Request validation error")
                .withDetail("Request parameters or body are invalid compared to the OpenAPI specification. See errors for more information")
                .withProperties(map -> map.put("errors", extractErrors(e.getValidationReport().getMessages())));
        return ResponseEntity.badRequest().contentType(APPLICATION_PROBLEM_JSON).body(problem);
    }

    private List<? extends ProblemError> extractErrors(List<ValidationReport.Message> messages) {
        return messages.stream()
                .map(message -> new ProblemError.ParameterError(message.getMessage(), message.getContext()
                        .flatMap(context -> context.getParameter().map(Parameter::getName)).orElse(null)))
                .toList();
    }

}
