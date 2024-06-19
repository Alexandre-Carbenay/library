package org.adhuc.library.catalog.books;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static java.time.LocalDate.now;
import static net.jqwik.api.Arbitraries.integers;
import static net.jqwik.api.Arbitraries.strings;
import static net.jqwik.time.api.Dates.dates;

public final class BooksMother {

    public static Arbitrary<Book> books() {
        return Combinators.combine(
                Books.ids(),
                Books.isbns(),
                Books.titles(),
                Books.publicationDates(),
                Books.authors().set().ofMinSize(1).ofMaxSize(4),
                Books.languages(),
                Books.summaries()
        ).as(Book::new);
    }

    public static BookBuilder builder() {
        return new BookBuilder();
    }

    public static final class Books {
        public static Arbitrary<UUID> ids() {
            return Arbitraries.create(UUID::randomUUID);
        }

        public static Arbitrary<String> isbns() {
            return strings().numeric().ofLength(13);
        }

        public static Arbitrary<String> titles() {
            return strings().alpha().withChars(' ').ofMinLength(3).ofMaxLength(100)
                    .filter(s -> !s.isBlank());
        }

        public static Arbitrary<PublicationDate> publicationDates() {
            return Arbitraries.oneOf(
                    exactPublicationDates(),
                    yearADPublicationDates(),
                    yearBCPublicationDates()
            );
        }

        public static Arbitrary<PublicationDate> exactPublicationDates() {
            return dates().atTheEarliest(LocalDate.parse("1800-01-01")).atTheLatest(now()).map(PublicationDate::of);
        }

        public static Arbitrary<PublicationDate> yearADPublicationDates() {
            return integers().between(0, now().getYear()).map(PublicationDate::of);
        }

        public static Arbitrary<PublicationDate> yearBCPublicationDates() {
            return integers().between(-1000, -1).map(PublicationDate::of);
        }

        public static Arbitrary<Author> authors() {
            return BooksMother.authors();
        }

        public static Arbitrary<String> languages() {
            return Arbitraries.of("French", "English");
        }

        public static Arbitrary<String> summaries() {
            return strings().alpha().numeric().withChars(" ,;.?!:-()[]{}&\"'àéèïöù").ofMinLength(30)
                    .filter(s -> !s.isBlank());
        }
    }

    public static Arbitrary<Author> authors() {
        return Combinators.combine(Authors.ids(), Authors.names()).as(Author::new);
    }

    public static final class Authors {
        public static Arbitrary<UUID> ids() {
            return Arbitraries.create(UUID::randomUUID);
        }

        public static Arbitrary<String> names() {
            return strings().alpha().withChars(' ').ofMinLength(3).ofMaxLength(100)
                    .filter(s -> !s.isBlank());
        }
    }

    public static class BookBuilder {
        private Book book = books().sample();

        public BookBuilder id(UUID id) {
            book = new Book(id, book.isbn(), book.title(), book.publicationDate(), book.authors(), book.language(), book.summary());
            return this;
        }

        public BookBuilder isbn(String isbn) {
            book = new Book(book.id(), isbn, book.title(), book.publicationDate(), book.authors(), book.language(), book.summary());
            return this;
        }

        public BookBuilder title(String title) {
            book = new Book(book.id(), book.isbn(), title, book.publicationDate(), book.authors(), book.language(), book.summary());
            return this;
        }

        public BookBuilder publicationDate(PublicationDate publicationDate) {
            book = new Book(book.id(), book.isbn(), book.title(), publicationDate, book.authors(), book.language(), book.summary());
            return this;
        }

        public BookBuilder authors(Set<Author> authors) {
            book = new Book(book.id(), book.isbn(), book.title(), book.publicationDate(), authors, book.language(), book.summary());
            return this;
        }

        public BookBuilder language(String language) {
            book = new Book(book.id(), book.isbn(), book.title(), book.publicationDate(), book.authors(), language, book.summary());
            return this;
        }

        public BookBuilder summary(String summary) {
            book = new Book(book.id(), book.isbn(), book.title(), book.publicationDate(), book.authors(), book.language(), summary);
            return this;
        }

        public Book build() {
            return book;
        }
    }

}
