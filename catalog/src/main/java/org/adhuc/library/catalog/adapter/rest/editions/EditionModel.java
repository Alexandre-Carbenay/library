package org.adhuc.library.catalog.adapter.rest.editions;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.adhuc.library.catalog.authors.Author;
import org.adhuc.library.catalog.editions.Edition;
import org.springframework.hateoas.RepresentationModel;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;

@SuppressWarnings("FieldCanBeLocal")
@JsonAutoDetect(fieldVisibility = ANY)
public class EditionModel extends RepresentationModel<EditionModel> {
    private final String isbn;
    private final String title;
    private final List<UUID> authors;
    private final String language;
    private final String summary;

    EditionModel(Edition edition) {
        this.isbn = edition.isbn();
        this.title = edition.title();
        this.authors = edition.book().authors().stream().sorted(Comparator.comparing(Author::name)).map(Author::id).toList();
        this.language = edition.language();
        this.summary = edition.summary();
    }
}
