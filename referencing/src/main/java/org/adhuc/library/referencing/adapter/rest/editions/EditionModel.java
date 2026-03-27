package org.adhuc.library.referencing.adapter.rest.editions;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.adhuc.library.referencing.adapter.rest.books.BookModel;
import org.adhuc.library.referencing.editions.Edition;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDate;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;

@JsonAutoDetect(fieldVisibility = ANY)
public class EditionModel extends RepresentationModel<BookModel> {
    private final String isbn;
    private final String language;
    private final UUID book;
    private final UUID publisher;
    private final LocalDate publicationDate;
    private final String title;
    private final String summary;

    public EditionModel(Edition edition) {
        this.isbn = edition.isbn();
        this.language = edition.language();
        this.book = edition.book();
        this.publisher = edition.publisher();
        this.publicationDate = edition.publicationDate();
        this.title = edition.title();
        this.summary = edition.summary();
    }

    String isbn() {
        return isbn;
    }
}
