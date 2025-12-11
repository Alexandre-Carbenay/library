package org.adhuc.library.support.rest.validation.openapi;

import com.atlassian.oai.validator.report.ValidationReport;
import com.atlassian.oai.validator.springmvc.InvalidRequestException;
import org.adhuc.library.support.rest.ProblemError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Collection;
import java.util.List;

import static org.adhuc.library.support.rest.validation.InvalidRequestBuilder.invalidRequest;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;

/**
 * A {@link RestControllerAdvice} that handles validation exceptions to retrieve list of errors from the exception and
 * build the response body, returning a {@link org.springframework.http.HttpStatus#BAD_REQUEST} status.
 * <p>
 * It uses an {@link org.springframework.core.annotation.Order ordered} list of {@link OpenApiValidationMessageParser}s
 * to retrieve the error sources from an {@link InvalidRequestException}.
 */
@RestControllerAdvice
public class OpenApiRequestValidationExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenApiRequestValidationExceptionHandler.class);

    private final List<OpenApiValidationMessageParser> messageParsers;

    public OpenApiRequestValidationExceptionHandler(List<OpenApiValidationMessageParser> messageParsers) {
        LOGGER.info("Initialize request validation exception handler with {} message parsers ({})", messageParsers.size(), messageParsers);
        Assert.notEmpty(messageParsers, "OpenAPI validation message parsers cannot be empty");
        this.messageParsers = messageParsers;
    }

    @ExceptionHandler(InvalidRequestException.class)
    ResponseEntity<Object> handleInvalidOpenApiRequest(InvalidRequestException e, WebRequest request) {
        var problem = invalidRequest(extractErrors(e.getValidationReport().getMessages()));
        return ResponseEntity.badRequest().contentType(APPLICATION_PROBLEM_JSON).body(problem);
    }

    private List<? extends ProblemError> extractErrors(List<ValidationReport.Message> messages) {
        return messages.stream()
                .map(this::extractErrors)
                .flatMap(Collection::stream)
                .toList();
    }

    private List<? extends ProblemError> extractErrors(ValidationReport.Message message) {
        return messageParsers.stream()
                .filter(parser -> parser.canParse(message))
                .findFirst()
                .map(parser -> parser.extractErrors(message))
                .orElse(List.of());
    }

}
