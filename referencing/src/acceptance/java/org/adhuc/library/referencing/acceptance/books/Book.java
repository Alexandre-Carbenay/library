package org.adhuc.library.referencing.acceptance.books;

import org.adhuc.library.referencing.acceptance.authors.Author;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public record Book(@Nullable String id, String originalLanguage, List<Author> authors, List<BookDetail> details) {

    public String titleIn(String language) {
        return details.stream()
                .filter(detail -> detail.language.equals(language))
                .map(BookDetail::title)
                .findFirst().orElseThrow(() -> new IllegalStateException("No title for book in language " + language));
    }

    public boolean hasTitle(String bookTitle) {
        return details.stream().anyMatch(detail -> detail.title.equals(bookTitle));
    }

    public String descriptionIn(String language) {
        return details.stream()
                .filter(detail -> detail.language.equals(language))
                .map(BookDetail::description)
                .findFirst().orElseThrow(() -> new IllegalStateException("No description for book in language " + language));
    }

    public boolean hasAuthor(Author author) {
        return authors.stream().anyMatch(bookAuthor -> Objects.equals(bookAuthor.id(), author.id()));
    }

    public boolean hasDetailIn(String language) {
        return details.stream().anyMatch(detail -> detail.language.equals(language));
    }

    public BookDetail detailIn(String language) {
        return details.stream().filter(detail -> detail.language.equals(language)).findFirst().orElseThrow();
    }

    public record BookDetail(String language, String title, String description) {
        public boolean hasTitle(String title) {
            return this.title.equals(title);
        }
    }

    public static Predicate<Book> hasWikipediaLink() {
        return book -> false;
    }

}
