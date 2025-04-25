package org.adhuc.library.catalog.adapter.rest.editions;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.adhuc.library.catalog.editions.Edition;
import org.adhuc.library.catalog.editions.Publisher;
import org.springframework.hateoas.RepresentationModel;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@SuppressWarnings("FieldCanBeLocal")
@JsonAutoDetect(fieldVisibility = ANY)
@JsonInclude(NON_EMPTY)
public class EditionDetailsModel extends RepresentationModel<EditionDetailsModel> {
    private final String isbn;
    private final String title;
    private final String publicationDate;
    private final String publisher;
    private final String language;
    private final String summary;

    EditionDetailsModel(Edition edition) {
        this.isbn = edition.isbn();
        this.title = edition.title();
        this.publicationDate = edition.publicationDate().toString();
        this.publisher = edition.publisher().map(Publisher::name).orElse(null);
        this.language = edition.language();
        this.summary = edition.summary();
    }
}
