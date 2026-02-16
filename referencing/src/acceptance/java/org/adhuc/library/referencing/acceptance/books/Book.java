package org.adhuc.library.referencing.acceptance.books;

import org.adhuc.library.referencing.acceptance.authors.Author;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public record Book(@Nullable String id, String originalLanguage, List<Author> authors, List<BookDetail> details) {

    public boolean hasTitle(String bookTitle) {
        return details.stream().anyMatch(detail -> detail.title.equals(bookTitle));
    }

    public record BookDetail(String language, String title, String description) {
    }

    public static Predicate<Book> hasWikipediaLink() {
        return book -> false;
    }

}
