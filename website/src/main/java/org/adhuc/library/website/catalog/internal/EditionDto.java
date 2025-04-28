package org.adhuc.library.website.catalog.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EditionDto(String isbn, @JsonProperty("_links") Links links) {

    public String selfLink() {
        return links.self.href;
    }

    public record Links(LinkValue self) {
    }

    public record LinkValue(String href) {
    }

}
