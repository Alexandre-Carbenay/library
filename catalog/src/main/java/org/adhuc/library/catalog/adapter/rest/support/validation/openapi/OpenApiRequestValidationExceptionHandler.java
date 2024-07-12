package org.adhuc.library.catalog.adapter.rest.support.validation.openapi;

import com.atlassian.oai.validator.report.ValidationReport;
import com.atlassian.oai.validator.springmvc.InvalidRequestException;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.adhuc.library.catalog.adapter.rest.Error;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.ZonedDateTime;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestControllerAdvice
public class OpenApiRequestValidationExceptionHandler {

    @ExceptionHandler(InvalidRequestException.class)
    ResponseEntity<Object> handleInvalidOpenApiRequest(InvalidRequestException e, WebRequest request) {
        var error = new Error(ZonedDateTime.now(), BAD_REQUEST.value(), "INVALID_REQUEST", "Request validation error",
                extractErrorSources(e.getValidationReport().getMessages()));
        return ResponseEntity.badRequest().body(error);
    }

    private List<? extends Error.ErrorSource> extractErrorSources(List<ValidationReport.Message> messages) {
        return messages.stream()
                .map(message -> new Error.ParameterError(message.getMessage(), message.getContext()
                        .flatMap(context -> context.getParameter().map(Parameter::getName)).orElse(null)))
                .toList();
    }

}
