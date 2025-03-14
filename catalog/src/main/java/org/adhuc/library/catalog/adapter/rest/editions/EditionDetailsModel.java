package org.adhuc.library.catalog.adapter.rest.editions;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.adhuc.library.catalog.books.Book;
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

    EditionDetailsModel(Book book) {
        this.isbn = book.isbn();
        this.title = book.title();
        this.publicationDate = book.publicationDate().toString();
        this.language = book.language();
        this.summary = book.summary();
    }
}
