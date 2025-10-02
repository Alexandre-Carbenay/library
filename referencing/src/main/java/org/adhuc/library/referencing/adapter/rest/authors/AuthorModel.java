package org.adhuc.library.referencing.adapter.rest.authors;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.adhuc.library.referencing.authors.Author;
import org.jspecify.annotations.Nullable;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDate;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;

@SuppressWarnings("FieldCanBeLocal")
@JsonAutoDetect(fieldVisibility = ANY)
public class AuthorModel extends RepresentationModel<AuthorModel> {
    private final UUID id;
    private final String name;
    private final LocalDate dateOfBirth;
    @Nullable
    private final LocalDate dateOfDeath;

    public AuthorModel(Author author) {
        this.id = author.id();
        this.name = author.name();
        this.dateOfBirth = author.dateOfBirth();
        this.dateOfDeath = author.dateOfDeath().orElse(null);
    }
}
