package org.adhuc.library.catalog.adapter.rest.authors;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.adhuc.library.catalog.authors.Author;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDate;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;

@SuppressWarnings("FieldCanBeLocal")
@JsonAutoDetect(fieldVisibility = ANY)
@JsonInclude(NON_ABSENT)
public class AuthorDetailsModel extends RepresentationModel<AuthorModel> {
    private final UUID id;
    private final String name;
    private final LocalDate dateOfBirth;
    private final LocalDate dateOfDeath;

    public AuthorDetailsModel(Author author) {
        this.id = author.id();
        this.name = author.name();
        this.dateOfBirth = author.dateOfBirth();
        this.dateOfDeath = author.dateOfDeath();
    }
}
