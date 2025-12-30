package org.adhuc.library.support.rest.validation.openapi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Defines the {@link OpenApiValidationMessageParser} beans used in the {@link OpenApiRequestValidationExceptionHandler}.
 */
@Configuration
public class OpenApiValidationMessageParsersConfiguration {

    @Bean
    NonNullableBodyElementParser nonNullableBodyElementParser() {
        return new NonNullableBodyElementParser();
    }

    @Bean
    RequiredBodyElementParser requiredBodyElementParser() {
        return new RequiredBodyElementParser();
    }

    @Bean
    RequiredParameterParser requiredParameterParser() {
        return new RequiredParameterParser();
    }

    @Bean
    DefaultValidationMessageParser defaultValidationMessageParser() {
        return new DefaultValidationMessageParser();
    }

}
