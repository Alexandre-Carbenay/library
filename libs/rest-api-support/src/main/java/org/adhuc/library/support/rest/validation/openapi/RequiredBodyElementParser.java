package org.adhuc.library.support.rest.validation.openapi;

import com.atlassian.oai.validator.report.ValidationReport;
import org.adhuc.library.support.rest.ProblemError;
import org.springframework.core.annotation.Order;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

/**
 * An {@link OpenApiValidationMessageParser} implementation that handles missing request body element.
 * <p>
 * This implementation expects error messages such as:
 * <ul>
 *     <li>Object has missing required properties (["element"])</li>
 *     <li>[Path '/parent'] Object has missing required properties (["child"])</li>
 * </ul>
 */
@Order(0)
class RequiredBodyElementParser extends AbstractPatternBasedOpenApiValidationMessageParser {

    private static final String MESSAGE_KEY = "validation.request.body.schema.required";
    private static final Pattern PATTERN = compile("^(\\[Path '(.*)'] )?Object has missing required properties \\(\\[\"(.*)\"]\\)$");
    private static final int PARENT_POINTER_GROUP = 2;
    private static final int POINTER_GROUP = 3;

    @Override
    public boolean canParse(ValidationReport.Message message) {
        return MESSAGE_KEY.equals(message.getKey());
    }

    @Override
    protected Pattern errorPattern() {
        return PATTERN;
    }

    @Override
    protected List<ProblemError> extractProblemErrorsInternal(Matcher matcher) {
        return List.of(new ProblemError.PointerError(
                "Missing required property",
                (matcher.group(PARENT_POINTER_GROUP) != null ? matcher.group(PARENT_POINTER_GROUP) : "")
                + POINTER_ELEMENT_DELIMITER + matcher.group(POINTER_GROUP)
        ));
    }

    @Override
    public String toString() {
        return "required body element parser";
    }

}
