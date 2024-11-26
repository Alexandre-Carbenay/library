package org.adhuc.library.website.catalog.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.adhuc.library.website.catalog.Author;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AuthorDto(UUID id, String name) {

    public Author toAuthor() {
        return new Author(id, name);
    }

}
