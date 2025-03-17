package org.adhuc.library.catalog.adapter.rest.editions;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.adhuc.library.catalog.editions.Edition;
import org.springframework.hateoas.RepresentationModel;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;

@SuppressWarnings("FieldCanBeLocal")
@JsonAutoDetect(fieldVisibility = ANY)
public class EditionDetailsModel extends RepresentationModel<EditionDetailsModel> {
    private final String isbn;
    private final String title;
    private final String publicationDate;
    private final String language;
    private final String summary;

    EditionDetailsModel(Edition edition) {
        this.isbn = edition.isbn();
        this.title = edition.title();
        this.publicationDate = edition.publicationDate().toString();
        this.language = edition.language();
        this.summary = edition.summary();
    }
}
