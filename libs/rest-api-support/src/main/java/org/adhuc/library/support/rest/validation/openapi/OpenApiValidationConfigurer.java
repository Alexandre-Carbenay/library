package org.adhuc.library.support.rest.validation.openapi;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.report.LevelResolverFactory;
import com.atlassian.oai.validator.springmvc.OpenApiValidationFilter;
import com.atlassian.oai.validator.springmvc.OpenApiValidationInterceptor;
import com.atlassian.oai.validator.whitelist.ValidationErrorsWhitelist;
import com.atlassian.oai.validator.whitelist.rule.WhitelistRule;
import jakarta.servlet.Filter;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import static com.atlassian.oai.validator.report.ValidationReport.Level.ERROR;
import static com.atlassian.oai.validator.whitelist.rule.WhitelistRules.anyOf;

/**
 * Configures the OpenAPI validation based on the atlassian {@link OpenApiValidationFilter}, using the OpenAPI
 * specification provided by in the {@code openApiSpecification} {@link Resource} bean.
 */
@Configuration
public class OpenApiValidationConfigurer implements WebMvcConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenApiValidationConfigurer.class);

    private static final String API_BASE_PATH = "/api";

    private final OpenApiValidationInterceptor validationInterceptor;

    OpenApiValidationConfigurer(@Qualifier("openApiSpecification") Resource apiSpecificationResource) throws IOException {
        LOGGER.info("Initialize OpenApi validation configuration based on {} specification", apiSpecificationResource);
        var apiSpecification = IOUtils.toString(apiSpecificationResource.getInputStream(), Charset.defaultCharset());
        this.validationInterceptor = new OpenApiValidationInterceptor(
                OpenApiInteractionValidator.createForInlineApiSpecification(apiSpecification)
                        .withLevelResolver(LevelResolverFactory.withAdditionalPropertiesIgnored())
                        .withBasePathOverride(API_BASE_PATH)
                        .withWhitelist(validationErrorsWhitelist())
                        .build()
        );
    }

    private ValidationErrorsWhitelist validationErrorsWhitelist() {
        LOGGER.info("Activate OpenAPI whitelist for OpenAPI specification documentation");
        var openApiDocPaths = List.of("/swagger-ui/", "/openapi.yml", "/api/doc/");
        return ValidationErrorsWhitelist.create()
                .withRule(
                        "Ignore OpenAPI specification documentation",
                        anyOf(
                                requestPathStartsWith(openApiDocPaths),
                                responseInErrorWithMessageAboutPath(openApiDocPaths)
                        )
                );
    }

    private WhitelistRule requestPathStartsWith(final List<String> pathPrefixes) {
        return (message, operation, request, response) ->
                request != null && pathPrefixes.stream().anyMatch(prefix -> request.getPath().startsWith(prefix));
    }

    private WhitelistRule responseInErrorWithMessageAboutPath(List<String> pathPrefixes) {
        return (message, operation, request, response) ->
                response != null && message.getLevel().equals(ERROR) && message.getMessage().contains("No API path found that matches request")
                        && pathPrefixes.stream().anyMatch(prefix -> message.getMessage().contains(prefix));
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(validationInterceptor);
    }

    @Bean
    Filter validationFilter() {
        return new OpenApiValidationFilter(true, true);
    }

}
