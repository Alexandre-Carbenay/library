package org.adhuc.library.support.rest.validation.openapi;

import com.atlassian.oai.validator.report.ValidationReport;
import org.adhuc.library.support.rest.ProblemError;

import java.util.List;

/**
 * An OpenAPI validation message parser, responsible for parsing a {@link ValidationReport.Message} as a list of
 * {@link ProblemError}s to populate a {@link org.springframework.hateoas.mediatype.problem.Problem} with accurate
 * errors, helping transform an OpenAPI validation exception into its problem representation.
 * <p>
 * Multiple parsers implementations will be used to parse the different validation errors. Each parser can define its
 * {@link org.springframework.core.annotation.Order order} to indicate its priority.
 *
 * @see OpenApiRequestValidationExceptionHandler
 * @see org.springframework.hateoas.mediatype.problem.Problem
 * @see org.adhuc.library.support.rest.validation.InvalidRequestBuilder
 */
public interface OpenApiValidationMessageParser {

    String POINTER_ELEMENT_DELIMITER = "/";

    /**
     * Indicates whether a message can be parsed by the parser implementation. Caller will typically use multiple
     * implementations of the parser interface, and choose the appropriate parser based on the result of this method.
     *
     * @param message the message to be parsed.
     * @return {@code true} if problem errors can be extracted from the message using
     * {@link #extractErrors(ValidationReport.Message)}, {@code false} otherwise.
     */
    boolean canParse(ValidationReport.Message message);

    /**
     * Extracts the problem errors from the message. This method should be called only if
     * {@link #canParse(ValidationReport.Message)} has returned {@code true} for this implementation.
     *
     * @param message the message to extract the problems from.
     * @return the resulting problem errors.
     */
    List<ProblemError> extractErrors(ValidationReport.Message message);

}
