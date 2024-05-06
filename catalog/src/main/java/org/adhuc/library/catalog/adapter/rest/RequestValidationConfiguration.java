package org.adhuc.library.catalog.adapter.rest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@Configuration
public class RequestValidationConfiguration {

    @Bean
    public Resource openApiSpecification(ResourceLoader resourceLoader) {
        return resourceLoader.getResource("classpath:api/openapi.yml");
    }

}
