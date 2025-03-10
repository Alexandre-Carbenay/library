package org.adhuc.library.website.catalog.internal;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "library.website.rest-client.catalog")
record CatalogRestClientProperties(String baseUrl, boolean sslEnabled) {

}
