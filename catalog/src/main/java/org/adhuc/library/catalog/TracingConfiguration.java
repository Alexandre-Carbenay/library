package org.adhuc.library.catalog;

import io.micrometer.tracing.exporter.SpanExportingPredicate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This configuration ensures that the Micrometer traces are not exported to the distributed tracing system for some
 * endpoints.
 */
@Configuration
class TracingConfiguration {

    private static final String URI_TAG = "uri";

    @Bean
    SpanExportingPredicate noActuator(@Value("${management.endpoints.web.base-path}") String managementBasePath) {
        return noBasePath(managementBasePath);
    }

    @Bean
    SpanExportingPredicate noSwaggerUi() {
        return noBasePath("/swagger-ui");
    }

    @Bean
    SpanExportingPredicate noApiDocs() {
        return noBasePath("/api/doc");
    }

    private SpanExportingPredicate noBasePath(String basePath) {
        return span -> span.getTags().get(URI_TAG) == null || !span.getTags().get(URI_TAG).startsWith(basePath);
    }

}
