package org.adhuc.library.support.rest.validation;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "org.adhuc.library.support.rest.validation", ignoreUnknownFields = false)
public record RequestValidationProperties(@DefaultValue("true") Boolean enabled, @DefaultValue OpenApi openApi) {

    public record OpenApi(@DefaultValue("classpath:api/openapi.yml") String location) {
    }

}
