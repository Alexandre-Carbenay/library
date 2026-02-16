package org.adhuc.library.referencing.adapter.rest.books;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.adhuc.library.referencing.books.Book;
import org.springframework.hateoas.RepresentationModel;

import java.util.List;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;

@JsonAutoDetect(fieldVisibility = ANY)
public class BookModel extends RepresentationModel<BookModel> {
    private final UUID id;
    private final List<UUID> authors;
    private final String originalLanguage;
    private final List<BookDetailModel> details;

    public BookModel(Book book) {
        this.id = book.id();
        this.authors = List.copyOf(book.authors());
        this.originalLanguage = book.originalLanguage();
        this.details = book.details().stream().map(BookDetailModel::new).toList();
    }

    public UUID id() {
        return id;
    }

    public record BookDetailModel(String language, String title, String description) {
        public BookDetailModel(Book.LocalizedDetail detail) {
            this(detail.language(), detail.title(), detail.description());
        }
    }
}
