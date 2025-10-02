package org.adhuc.library.referencing.adapter.rest.support.validation.openapi;

import com.atlassian.oai.validator.report.ValidationReport;
import com.atlassian.oai.validator.springmvc.InvalidRequestException;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.adhuc.library.referencing.adapter.rest.ProblemError;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.adhuc.library.referencing.adapter.rest.support.validation.InvalidRequestBuilder.invalidRequest;
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
                .flatMap(message -> extractError(message).stream())
                .toList();
    }

    private Optional<ProblemError> extractError(ValidationReport.Message message) {
        return message.getContext().flatMap(context -> mapParameterError(message.getMessage(), context)
                .or(() -> mapPointerError(message.getMessage(), context))
                .or(() -> Optional.of(mapGeneralError(message.getMessage())))
        );
    }

    private Optional<ProblemError> mapParameterError(String message, ValidationReport.MessageContext context) {
        return context.getParameter().map(Parameter::getName)
                .map(parameter -> new ProblemError.ParameterError(message, parameter));
    }

    private Optional<ProblemError> mapPointerError(String message, ValidationReport.MessageContext context) {
        var matcher = Pattern.compile("^Object has missing required properties \\(\\[\"(.*)\"]\\)$").matcher(message);
        if (matcher.matches()) {
            return Optional.of(new ProblemError.PointerError("Missing required property", matcher.group(1)));
        }
        return Optional.empty();
    }

    private ProblemError mapGeneralError(String message) {
        return new ProblemError.ParameterError(message, null);
    }

}
