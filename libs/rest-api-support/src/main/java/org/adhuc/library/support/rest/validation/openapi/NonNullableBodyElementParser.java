package org.adhuc.library.support.rest.validation.openapi;

import com.atlassian.oai.validator.report.ValidationReport;
import org.adhuc.library.support.rest.ProblemError;
import org.springframework.core.annotation.Order;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

/**
 * An {@link OpenApiValidationMessageParser} implementation that handles unexpectedly {@code null} body elements.
 * <p>
 * This implementation expects error messages such as:
 * <ul>
 *     <li>[Path '/element'] Instance type (null) does not match any allowed primitive type (allowed: ["string"])</li>
 *     <li>[Path '/parent/child'] Instance type (null) does not match any allowed primitive type (allowed: ["integer"])</li>
 * </ul>
 */
@Order(3)
class NonNullableBodyElementParser extends AbstractPatternBasedOpenApiValidationMessageParser {

    private static final String MESSAGE_KEY = "validation.request.body.schema.type";
    private static final Pattern PATTERN = compile("^\\[Path '(.*)'] Instance type \\(null\\) does not match any allowed primitive type \\(allowed: \\[(.*)]\\)$");
    private static final int POINTER_GROUP = 1;

    @Override
    public boolean canParse(ValidationReport.Message message) {
        return MESSAGE_KEY.equals(message.getKey()) && PATTERN.matcher(message.getMessage()).matches();
    }

    @Override
    protected Pattern errorPattern() {
        return PATTERN;
    }

    @Override
    protected List<ProblemError> extractProblemErrorsInternal(Matcher matcher) {
        return List.of(new ProblemError.PointerError(
                "Not nullable property",
                matcher.group(POINTER_GROUP)
        ));
    }

    @Override
    public String toString() {
        return "non nullable body element parser";
    }

}
