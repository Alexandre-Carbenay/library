package org.adhuc.library.catalog.adapter.autoload;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "library.catalog.data.auto-load")
public record CatalogAutoLoadProperties(AutoLoadDefinition authors, AutoLoadDefinition books) {

    public record AutoLoadDefinition(String resource) {
    }

}
