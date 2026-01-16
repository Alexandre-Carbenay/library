package org.adhuc.library.support.rest.validation;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "org.adhuc.library.support.rest.validation", ignoreUnknownFields = false)
public record RequestValidationProperties(@DefaultValue("true") Boolean enabled, @DefaultValue OpenApi openApi, @DefaultValue Jsr303 jsr303) {

    public record OpenApi(@DefaultValue("classpath:api/openapi.yml") String location) {
    }

    public record Jsr303(@DefaultValue("false") Boolean enabled, @DefaultValue("classpath:validation/messages") String messageBasename) {
    }

}
