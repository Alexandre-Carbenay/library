package org.adhuc.library.support.rest.validation.openapi;

import com.atlassian.oai.validator.report.ValidationReport;
import org.adhuc.library.support.rest.ProblemError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An {@link OpenApiValidationMessageParser} implementation based on a pattern to detect the validation error message,
 * and extract the errors from message.
 */
abstract class AbstractPatternBasedOpenApiValidationMessageParser implements OpenApiValidationMessageParser {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public List<ProblemError> extractErrors(ValidationReport.Message message) {
        var match = errorPattern().matcher(message.getMessage());

        List<ProblemError> problemErrors = List.of();
        if (match.matches()) {
            log.debug("Extract problem errors from validation report message \"{}\"", message.getMessage());
            problemErrors = extractProblemErrorsInternal(match);
        }
        return !problemErrors.isEmpty() ? problemErrors : List.of(defaultProblemError(message));
    }

    /**
     * Builds a problem error by default, using the specified message as the error detail. This problem error will
     * be used only if no problem error can be {@link #extractProblemErrorsInternal(Matcher) extracted} by child
     * implementation.
     *
     * @param message the validation error message.
     * @return an error source to be used by default.
     */
    protected ProblemError defaultProblemError(ValidationReport.Message message) {
        log.debug("Generate default source from validation report message \"{}\"", message.getMessage());
        return new ProblemError.DefaultError(message.getMessage());
    }

    /**
     * Gets the pattern to be used to extract the {@link ProblemError problem errors} from the message. If the message
     * matches this pattern, the problem errors will then be {@link #extractProblemErrorsInternal(Matcher) extracted}
     * from the match.
     *
     * @return the error pattern to match the message.
     */
    protected abstract Pattern errorPattern();

    /**
     * Extracts the {@link ProblemError problem errors} from the specified matcher.
     *
     * @param matcher the matcher resulting from the match between the {@link #errorPattern()} and the validation message.
     * @return the list of problem errors used to build the error.
     */
    protected abstract List<ProblemError> extractProblemErrorsInternal(Matcher matcher);

}
