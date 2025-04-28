package org.adhuc.library.website.catalog.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.adhuc.library.website.catalog.Book;
import org.adhuc.library.website.catalog.Edition;

import java.util.List;
import java.util.stream.IntStream;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BookDetailDto(String id, String title, String description,
                            @JsonProperty("_embedded") EmbeddedValues embedded) {

    public Book asBook(List<Edition> editions) {
        return new Book(
                id,
                title,
                embedded.authors.stream().map(AuthorDto::toAuthor).toList(),
                description,
                editions
        );
    }

    List<EditionDto> editions() {
        return embedded.editions;
    }

    int indexOfEdition(String isbn) {
        return IntStream.range(0, embedded.editions.size())
                .filter(index -> embedded.editions.get(index).isbn().equals(isbn))
                .findFirst()
                .orElseThrow();
    }

    public record EmbeddedValues(List<AuthorDto> authors, List<EditionDto> editions) {

    }

}
