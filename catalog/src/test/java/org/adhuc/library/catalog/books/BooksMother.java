package org.adhuc.library.catalog.books;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;

import java.util.UUID;

import static net.jqwik.api.Arbitraries.strings;

public final class BooksMother {

    public static Arbitrary<Book> books() {
        return Combinators.combine(
                Books.ids(),
                Books.isbns(),
                Books.titles(),
                Books.authors().set().ofMinSize(1).ofMaxSize(4),
                Books.languages(),
                Books.summaries()
        ).as(Book::new);
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

}
