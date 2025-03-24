package org.adhuc.library.website.catalog.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.adhuc.library.website.catalog.Book;

import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BookDto(String title, List<UUID> authors, String description) {

    public Book withAuthorsFrom(List<AuthorDto> authors) {
        return new Book(
                title,
                this.authors.stream()
                        .flatMap(id -> authors.stream()
                                .filter(author -> author.id().equals(id))
                                .findFirst()
                                .map(AuthorDto::toAuthor)
                                .stream()
                        )
                        .toList(),
                description
        );
    }
}
