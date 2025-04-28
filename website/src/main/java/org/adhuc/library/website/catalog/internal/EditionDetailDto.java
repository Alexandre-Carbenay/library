package org.adhuc.library.website.catalog.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.adhuc.library.website.catalog.Edition;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EditionDetailDto(String isbn, String title, String publisher,
                               @JsonProperty("publication_date") LocalDate publicationDate, String language) {

    public Edition toEdition() {
        return new Edition(isbn, title, publisher, publicationDate, language);
    }

}
