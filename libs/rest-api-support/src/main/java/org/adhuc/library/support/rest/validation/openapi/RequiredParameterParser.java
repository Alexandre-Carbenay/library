package org.adhuc.library.support.rest.validation.openapi;

import com.atlassian.oai.validator.report.ValidationReport;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.adhuc.library.support.rest.ProblemError;
import org.springframework.core.annotation.Order;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * An {@link OpenApiValidationMessageParser} implementation that handles missing required parameter, either query
 * parameter or request header.
 */
@Order(2)
@SuppressWarnings("preview")
class RequiredParameterParser implements OpenApiValidationMessageParser {

    @Override
    public boolean canParse(ValidationReport.Message message) {
        return extractParameter(message).isPresent();
    }

    @Override
    public List<ProblemError> extractErrors(ValidationReport.Message message) {
        var parameter = extractParameter(message);
        if (parameter.isEmpty()) {
            return List.of();
        }
        var parameterType = convertToParameterType(parameter.get());
        return List.of(new ProblemError.ParameterError(
                STR."Missing required \{parameterType.detailName}",
                parameter.get().getName()
        ));
    }

    private Optional<Parameter> extractParameter(ValidationReport.Message message) {
        return message.getContext().flatMap(ValidationReport.MessageContext::getParameter);
    }

    private ParameterType convertToParameterType(Parameter parameter) {
        return Arrays.stream(ParameterType.values())
                .filter(type -> type.in.equals(parameter.getIn()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(STR."Cannot handle parameter of type \{parameter.getIn()}"));
    }

    enum ParameterType {
        QUERY("query", "query parameter"),
        HEADER("header", "header");

        private final String in;
        private final String detailName;

        ParameterType(String in, String detailName) {
            this.in = in;
            this.detailName = detailName;
        }
    }

}
