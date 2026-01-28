package org.adhuc.library.referencing.books;

import net.datafaker.Faker;
import org.adhuc.library.referencing.authors.AuthorsMother;
import org.adhuc.library.referencing.books.Book.LocalizedDetail;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toSet;
import static org.adhuc.library.referencing.books.BooksMother.Books.*;

public class BooksMother {

    public static List<Book> books(int size) {
        return IntStream.range(0, size)
                .mapToObj(_ -> book())
                .toList();
    }

    public static Book book() {
        var originalLanguage = language();
        return new Book(id(), authors(), originalLanguage, detailsSets(originalLanguage));
    }

    public static BookBuilder builder() {
        return new BookBuilder();
    }

    public static LocalizedDetailsBuilder detailsBuilder() {
        return new LocalizedDetailsBuilder();
    }

    public static final class Books {
        private static final Faker FAKER = new Faker();
        private static final List<String> LANGUAGES = List.of("fr", "en", "de", "it");

        public static UUID id() {
            return UUID.randomUUID();
        }

        public static UUID author() {
            return AuthorsMother.author().id();
        }

        public static Set<UUID> authors() {
            var numberOfAuthors = FAKER.random().nextInt(1, 3);
            return authors(numberOfAuthors);
        }

        public static Set<UUID> authors(int numberOfAuthors) {
            return IntStream.range(0, numberOfAuthors)
                    .mapToObj(_ -> author())
                    .collect(toSet());
        }

        public static String language() {
            return LANGUAGES.get(FAKER.random().nextInt(4));
        }

        public static List<String> otherLanguages(String language) {
            var otherLanguages = LANGUAGES.stream().filter(other -> !other.equals(language));
            var maximumOtherLanguages = LANGUAGES.size() - 1;
            var numberOfLanguages = FAKER.random().nextInt(1, maximumOtherLanguages);
            return otherLanguages.limit(numberOfLanguages).toList();
        }

        public static Set<LocalizedDetail> detailsSets(String originalLanguage) {
            return detailsSets(originalLanguage, Set.copyOf(otherLanguages(originalLanguage)));
        }

        public static Set<LocalizedDetail> detailsSets(String originalLanguage, Set<String> otherLanguages) {
            var originalDetails = details(originalLanguage);
            var otherDetails = otherLanguages.stream()
                    .map(Books::details)
                    .toList();
            var details = new HashSet<>(otherDetails);
            details.add(originalDetails);
            return details;
        }

        public static LocalizedDetail details(String language) {
            return new LocalizedDetail(language, title(), description());
        }

        public static String title() {
            return FAKER.book().title();
        }

        public static String description() {
            return FAKER.text().text(30, 1000);
        }
    }

    public static class BookBuilder {
        private Book book = book();

        public BookBuilder id(UUID id) {
            book = new Book(id, book.authors(), book.originalLanguage(), book.details());
            return this;
        }

        public BookBuilder authors(Set<UUID> authors) {
            book = new Book(book.id(), authors, book.originalLanguage(), book.details());
            return this;
        }

        public BookBuilder oneAuthor() {
            book = new Book(book.id(), Set.of(author()), book.originalLanguage(), book.details());
            return this;
        }

        public BookBuilder multipleAuthors() {
            book = new Book(book.id(), Set.of(author(), author()), book.originalLanguage(), book.details());
            return this;
        }

        public BookBuilder originalLanguage(String originalLanguage) {
            book = new Book(book.id(), book.authors(), originalLanguage, Books.detailsSets(originalLanguage));
            return this;
        }

        public BookBuilder detailInOriginalLanguage() {
            return this.details(Books.details(book.originalLanguage()));
        }

        public BookBuilder detailInOtherLanguage() {
            var language = Books.otherLanguages(book.originalLanguage()).getFirst();
            return this.details(Books.details(language));
        }

        public BookBuilder plusDetailInOtherLanguage() {
            var language = Books.otherLanguages(book.originalLanguage()).getFirst();
            var details = new HashSet<>(book.details());
            details.add(Books.details(language));
            return this.details(details);
        }

        public BookBuilder details(LocalizedDetail localizedDetail) {
            book = new Book(book.id(), book.authors(), book.originalLanguage(), Set.of(localizedDetail));
            return this;
        }

        public BookBuilder details(Set<LocalizedDetail> localizedDetails) {
            book = new Book(book.id(), book.authors(), book.originalLanguage(), localizedDetails);
            return this;
        }

        public Book build() {
            return book;
        }
    }

    public static class LocalizedDetailsBuilder {
        private String language;
        private String title;
        private String description;

        private LocalizedDetailsBuilder() {
            language = Books.language();
            title = Books.title();
            description = Books.description();
        }

        public LocalizedDetailsBuilder language(String language) {
            this.language = language;
            return this;
        }

        public LocalizedDetailsBuilder title(String title) {
            this.title = title;
            return this;
        }

        public LocalizedDetailsBuilder description(String description) {
            this.description = description;
            return this;
        }

        public LocalizedDetail build() {
            return new LocalizedDetail(language, title, description);
        }
    }

}
