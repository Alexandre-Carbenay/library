package org.adhuc.library.catalog.adapter.rest.books;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.adhuc.library.catalog.books.Author;
import org.adhuc.library.catalog.books.Book;
import org.springframework.hateoas.RepresentationModel;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;

@JsonAutoDetect(fieldVisibility = ANY)
public class BookModel extends RepresentationModel<BookModel> {
    private final UUID id;
    private final String isbn;
    private final String title;
    private final List<UUID> authors;
    private final String language;
    private final String summary;

    public BookModel(Book book) {
        this.id = book.id();
        this.isbn = book.isbn();
        this.title = book.title();
        this.authors = book.authors().stream().sorted(Comparator.comparing(Author::name)).map(Author::id).toList();
        this.language = book.language();
        this.summary = book.summary();
    }
}
