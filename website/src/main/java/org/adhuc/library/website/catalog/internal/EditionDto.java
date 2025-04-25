package org.adhuc.library.website.catalog.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.adhuc.library.website.catalog.Edition;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EditionDto(String isbn, String title, String language) {

    public Edition toEdition() {
        return new Edition(isbn, title, language);
    }

}
