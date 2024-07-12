package org.adhuc.library.catalog.adapter.rest.support.validation.openapi;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.report.LevelResolverFactory;
import com.atlassian.oai.validator.springmvc.OpenApiValidationFilter;
import com.atlassian.oai.validator.springmvc.OpenApiValidationInterceptor;
import com.atlassian.oai.validator.whitelist.ValidationErrorsWhitelist;
import com.atlassian.oai.validator.whitelist.rule.WhitelistRule;
import io.micrometer.core.instrument.util.IOUtils;
import jakarta.servlet.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.charset.Charset;

import static com.atlassian.oai.validator.whitelist.rule.WhitelistRules.allOf;
import static com.atlassian.oai.validator.whitelist.rule.WhitelistRules.isRequest;

@Configuration
public class RequestValidationConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestValidationConfiguration.class);

    @Bean
    Resource openApiSpecification(ResourceLoader resourceLoader) {
        return resourceLoader.getResource("classpath:api/openapi.yml");
    }

    @Configuration
    static class OpenApiValidationConfigurer implements WebMvcConfigurer {

        private static final String API_BASE_PATH = "/api";

        private final OpenApiValidationInterceptor validationInterceptor;

        OpenApiValidationConfigurer(@Qualifier("openApiSpecification") Resource apiSpecificationResource,
                                    @Value("${management.endpoints.web.base-path}") String managementBasePath) throws IOException {
            LOGGER.info("Initialize OpenApi validation configuration based on {} specification and ignoring management endpoints from base path {}",
                    apiSpecificationResource, managementBasePath);
            var apiSpecification = IOUtils.toString(apiSpecificationResource.getInputStream(), Charset.defaultCharset());
            this.validationInterceptor = new OpenApiValidationInterceptor(
                    OpenApiInteractionValidator.createForInlineApiSpecification(apiSpecification)
                            .withLevelResolver(LevelResolverFactory.withAdditionalPropertiesIgnored())
                            .withBasePathOverride(API_BASE_PATH)
                            .withWhitelist(validationErrorsWhitelist(managementBasePath))
                            .build());
        }

        private ValidationErrorsWhitelist validationErrorsWhitelist(String managementBasePath) {
            LOGGER.info("Activate OpenAPI whitelist for Spring Boot management API");
            return ValidationErrorsWhitelist.create()
                    .withRule(
                            "Ignore Spring Boot management API",
                            allOf(
                                    isRequest(),
                                    requestPathStartsWith(managementBasePath)
                            )
                    );
        }

        private WhitelistRule requestPathStartsWith(final String pathPrefix) {
            return (message, operation, request, response) -> operation == null && request.getPath().startsWith(pathPrefix);
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

}
