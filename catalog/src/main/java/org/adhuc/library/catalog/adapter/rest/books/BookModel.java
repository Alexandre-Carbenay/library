package org.adhuc.library.catalog.adapter.rest.books;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.adhuc.library.catalog.authors.Author;
import org.adhuc.library.catalog.books.Book;
import org.springframework.hateoas.RepresentationModel;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;

@SuppressWarnings("FieldCanBeLocal")
@JsonAutoDetect(fieldVisibility = ANY)
public class BookModel extends RepresentationModel<BookModel> {
    private final UUID id;
    private final String title;
    private final String description;
    private final List<UUID> authors;

    BookModel(Book book) {
        this(book, book.originalLanguage());
    }

    BookModel(Book book, String language) {
        this.id = book.id();
        this.title = book.titleIn(language);
        this.description = book.descriptionIn(language);
        this.authors = book.authors().stream().sorted(Comparator.comparing(Author::name)).map(Author::id).toList();
    }
}
