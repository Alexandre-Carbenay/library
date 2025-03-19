package org.adhuc.library.catalog.adapter.rest.books;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.adhuc.library.catalog.books.Book;
import org.springframework.hateoas.RepresentationModel;

import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;

@SuppressWarnings("FieldCanBeLocal")
@JsonAutoDetect(fieldVisibility = ANY)
class BookDetailsModel extends RepresentationModel<BookDetailsModel> {
    private final UUID id;
    private final String title;
    private final String description;

    BookDetailsModel(Book book) {
        this(book, book.originalLanguage());
    }

    BookDetailsModel(Book book, String language) {
        this.id = book.id();
        this.title = book.titleIn(language);
        this.description = book.descriptionIn(language);
    }
}
