package org.adhuc.library.catalog.adapter.rest.authors;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.adhuc.library.catalog.books.Author;
import org.springframework.hateoas.RepresentationModel;

import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;

@JsonAutoDetect(fieldVisibility = ANY)
public class AuthorModel extends RepresentationModel<AuthorModel> {
    private final UUID id;
    private final String name;

    public AuthorModel(Author author) {
        this.id = author.id();
        this.name = author.name();
    }
}
