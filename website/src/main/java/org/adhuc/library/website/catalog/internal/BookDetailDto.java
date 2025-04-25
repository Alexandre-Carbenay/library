package org.adhuc.library.website.catalog.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.adhuc.library.website.catalog.Book;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BookDetailDto(String id, String title, String description,
                            @JsonProperty("_embedded") EmbeddedValues embedded) {

    public Book asBook() {
        return new Book(
                id,
                title,
                embedded.authors.stream().map(AuthorDto::toAuthor).toList(),
                description,
                embedded.editions.stream().map(EditionDto::toEdition).toList()
                );
    }

    public record EmbeddedValues(List<AuthorDto> authors, List<EditionDto> editions) {

    }

}
