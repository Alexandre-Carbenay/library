package org.adhuc.library.support.rest.validation;

import org.adhuc.library.support.rest.validation.openapi.OpenApiValidationConfigurer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@AutoConfiguration
@Import(OpenApiValidationConfigurer.class)
@EnableConfigurationProperties(RequestValidationProperties.class)
public class RequestValidationAutoConfiguration {

    private final RequestValidationProperties properties;

    public RequestValidationAutoConfiguration(RequestValidationProperties properties) {
        this.properties = properties;
    }

    @Bean
    Resource openApiSpecification(ResourceLoader resourceLoader) {
        return resourceLoader.getResource(properties.openApi().location());
    }

}
