package org.adhuc.library.support.rest.validation.openapi;

import com.atlassian.oai.validator.report.ValidationReport;
import org.adhuc.library.support.rest.ProblemError;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

/**
 * An {@link OpenApiValidationMessageParser} default implementation. It expects error messages using a
 * <code>[Path '/element'] Reason description</code> pattern.
 * <p>
 * This default implementation is used to determine the error details if no other implementation
 * {@link #canParse(ValidationReport.Message) can parse} the validation error message.
 */
// No order: this default implementation must be the last one in the list
class DefaultValidationMessageParser extends AbstractPatternBasedOpenApiValidationMessageParser {

    private static final Pattern PATTERN = compile("^\\[Path '(.*)'] (.*)$");
    private static final int POINTER_GROUP = 1;
    private static final int REASON_GROUP = 2;

    @Override
    public boolean canParse(ValidationReport.Message message) {
        return true;
    }

    @Override
    protected Pattern errorPattern() {
        return PATTERN;
    }

    @Override
    protected List<ProblemError> extractProblemErrorsInternal(Matcher matcher) {
        return List.of(new ProblemError.PointerError(
                matcher.group(REASON_GROUP),
                matcher.group(POINTER_GROUP)
        ));
    }

}
