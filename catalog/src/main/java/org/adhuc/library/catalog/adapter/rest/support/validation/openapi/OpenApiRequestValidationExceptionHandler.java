package org.adhuc.library.catalog.adapter.rest.support.validation.openapi;

import com.atlassian.oai.validator.report.ValidationReport;
import com.atlassian.oai.validator.springmvc.InvalidRequestException;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.adhuc.library.catalog.adapter.rest.ProblemError;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

import static org.adhuc.library.catalog.adapter.rest.support.validation.InvalidRequestBuilder.invalidRequest;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;

@RestControllerAdvice
public class OpenApiRequestValidationExceptionHandler {

    @ExceptionHandler(InvalidRequestException.class)
    ResponseEntity<Object> handleInvalidOpenApiRequest(InvalidRequestException e, WebRequest request) {
        var problem = invalidRequest(extractErrors(e.getValidationReport().getMessages()));
        return ResponseEntity.badRequest().contentType(APPLICATION_PROBLEM_JSON).body(problem);
    }

    private List<? extends ProblemError> extractErrors(List<ValidationReport.Message> messages) {
        return messages.stream()
                .map(message -> new ProblemError.ParameterError(message.getMessage(), message.getContext()
                        .flatMap(context -> context.getParameter().map(Parameter::getName)).orElse(null)))
                .toList();
    }

}
