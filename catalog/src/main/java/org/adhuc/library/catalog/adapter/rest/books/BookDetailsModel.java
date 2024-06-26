package org.adhuc.library.catalog.adapter.rest.books;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.adhuc.library.catalog.books.Book;
import org.springframework.hateoas.RepresentationModel;

import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;

@SuppressWarnings("FieldCanBeLocal")
@JsonAutoDetect(fieldVisibility = ANY)
public class BookDetailsModel extends RepresentationModel<BookDetailsModel> {
    private final UUID id;
    private final String isbn;
    private final String title;
    private final String publicationDate;
    private final String language;
    private final String summary;

    BookDetailsModel(Book book) {
        this.id = book.id();
        this.isbn = book.isbn();
        this.title = book.title();
        this.publicationDate = book.publicationDate().toString();
        this.language = book.language();
        this.summary = book.summary();
    }
}
