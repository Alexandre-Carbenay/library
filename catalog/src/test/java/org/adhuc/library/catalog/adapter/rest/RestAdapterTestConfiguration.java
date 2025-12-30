package org.adhuc.library.catalog.adapter.rest;

import org.adhuc.library.support.rest.validation.RequestValidationAutoConfiguration;
import org.adhuc.library.support.rest.validation.openapi.OpenApiRequestValidationExceptionHandler;
import org.adhuc.library.support.rest.validation.openapi.OpenApiValidationConfigurer;
import org.adhuc.library.support.rest.validation.openapi.OpenApiValidationMessageParsersConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@ImportAutoConfiguration(RequestValidationAutoConfiguration.class)
@Import({
        OpenApiValidationConfigurer.class,
        OpenApiRequestValidationExceptionHandler.class,
        OpenApiValidationMessageParsersConfiguration.class,
        PaginationSerializationConfiguration.class
})
public class RestAdapterTestConfiguration {
}
