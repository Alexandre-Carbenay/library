package org.adhuc.library.support.rest.validation.jsr303;

import jakarta.validation.ConstraintViolation;
import org.adhuc.library.support.rest.ProblemError;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.Optional;

import static org.adhuc.library.support.rest.validation.InvalidRequestBuilder.invalidRequest;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ControllerAdvice
public class Jsr303ValidationExceptionHandler extends ResponseEntityExceptionHandler {

    // Attribute key to retrieve the "pointerName" value from constraints
    private static final String POINTER_NAME_CONSTRAINT_ATTRIBUTE_KEY = "pointerName";

    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                            HttpHeaders headers,
                                                                            HttpStatusCode status,
                                                                            WebRequest request) {
        var error = invalidRequest(extractErrorsFromException(ex));
        return handleExceptionInternal(ex, error, new HttpHeaders(), BAD_REQUEST, request);
    }

    private List<ProblemError> extractErrorsFromException(MethodArgumentNotValidException e) {
        return e.getAllErrors().stream()
                .map(this::extractErrorFrom)
                .toList();
    }

    private ProblemError extractErrorFrom(ObjectError error) {
        return switch (error) {
            case FieldError fieldError -> extractFieldError(fieldError);
            default -> extractObjectError(error);
        };
    }

    private ProblemError extractFieldError(FieldError error) {
        var detail = String.format("String \"%s\" %s", error.getRejectedValue(), error.getDefaultMessage());
        var pointer = "/" + extractFieldPointer(error);
        return new ProblemError.PointerError(detail, pointer);
    }

    private ProblemError extractObjectError(ObjectError error) {
        var detail = Optional.ofNullable(error.getDefaultMessage()).orElse("Unknown error");
        return extractPointer(error)
                .map(pointer -> (ProblemError) new ProblemError.PointerError(detail, pointer))
                .orElse(new ProblemError.DefaultError(detail));
    }

    private Optional<String> extractPointer(ObjectError error) {
        try {
            var violation = error.unwrap(ConstraintViolation.class);
            var value = violation.getConstraintDescriptor().getAttributes().get(POINTER_NAME_CONSTRAINT_ATTRIBUTE_KEY);
            return (value != null && String.class.isAssignableFrom(value.getClass())) ? Optional.of((String) value) : Optional.empty();
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    private String extractFieldPointer(FieldError error) {
        var snakeCaseField = error.getField()
                .replaceAll("([A-Z])(?=[A-Z])", "$1_")
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .toLowerCase();
        return snakeCaseField;
    }

}
