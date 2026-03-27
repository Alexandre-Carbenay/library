package org.adhuc.library.referencing.editions;

import net.datafaker.Faker;
import org.adhuc.library.referencing.books.BooksMother.Books;
import org.adhuc.library.referencing.publishers.PublishersMother.Publishers;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.adhuc.library.referencing.editions.EditionsMother.Editions.*;

public class EditionsMother {

    public static List<Edition> editions(int size) {
        return IntStream.range(0, size)
                .mapToObj(_ -> edition())
                .toList();
    }

    public static Edition edition() {
        return new Edition(isbn(), language(), book(), publisher(), publicationDate(), title(), summary());
    }

    public static EditionBuilder builder() {
        return new EditionBuilder();
    }

    public static final class Editions {
        private static final Faker FAKER = new Faker();
        private static final List<String> LANGUAGES = List.of("fr", "en", "de", "it");

        public static String isbn() {
            return FAKER.code().isbn13();
        }

        public static String language() {
            return LANGUAGES.get(FAKER.random().nextInt(4));
        }

        public static UUID book() {
            return Books.id();
        }

        public static UUID publisher() {
            return Publishers.id();
        }

        public static LocalDate publicationDate() {
            return LocalDate.ofInstant(FAKER.timeAndDate().past(), ZoneId.systemDefault());
        }

        public static String title() {
            return FAKER.book().title();
        }

        public static String summary() {
            return FAKER.text().text(30, 1000);
        }
    }

    public static class EditionBuilder {
        private Edition edition = edition();

        public EditionBuilder isbn(String isbn) {
            edition = new Edition(isbn, edition.language(), edition.book(), edition.publisher(),
                    edition.publicationDate(), edition.title(), edition.summary());
            return this;
        }

        public EditionBuilder language(String language) {
            edition = new Edition(edition.isbn(), language, edition.book(), edition.publisher(),
                    edition.publicationDate(), edition.title(), edition.summary());
            return this;
        }

        public EditionBuilder book(UUID book) {
            edition = new Edition(edition.isbn(), edition.language(), book, edition.publisher(),
                    edition.publicationDate(), edition.title(), edition.summary());
            return this;
        }

        public EditionBuilder publisher(UUID publisher) {
            edition = new Edition(edition.isbn(), edition.language(), edition.book(), publisher,
                    edition.publicationDate(), edition.title(), edition.summary());
            return this;
        }

        public EditionBuilder publicationDate(LocalDate publicationDate) {
            edition = new Edition(edition.isbn(), edition.language(), edition.book(), edition.publisher(),
                    publicationDate, edition.title(), edition.summary());
            return this;
        }

        public EditionBuilder title(String title) {
            edition = new Edition(edition.isbn(), edition.language(), edition.book(), edition.publisher(),
                    edition.publicationDate(), title, edition.summary());
            return this;
        }

        public EditionBuilder summary(String summary) {
            edition = new Edition(edition.isbn(), edition.language(), edition.book(), edition.publisher(),
                    edition.publicationDate(), edition.title(), summary);
            return this;
        }

        public Edition build() {
            return edition;
        }
    }

}
