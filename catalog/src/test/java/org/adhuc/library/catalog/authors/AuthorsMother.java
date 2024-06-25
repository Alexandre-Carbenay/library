package org.adhuc.library.catalog.authors;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import org.adhuc.library.catalog.books.Book;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static java.time.LocalDate.now;
import static net.jqwik.api.Arbitraries.oneOf;
import static net.jqwik.api.Arbitraries.strings;
import static net.jqwik.time.api.Dates.dates;

public class AuthorsMother {

    public static Arbitrary<Author> authors() {
        return Combinators.combine(Authors.ids(), Authors.names(), Authors.datesOfBirth())
                .flatAs((id, name, dateOfBirth) -> Authors.datesOfDeath(dateOfBirth)
                        .map(dateOfDeath -> new Author(id, name, dateOfBirth, dateOfDeath, List.of())));
    }

    public static AuthorBuilder builder() {
        return new AuthorBuilder();
    }

    public static final class Authors {
        public static Arbitrary<UUID> ids() {
            return Arbitraries.create(UUID::randomUUID);
        }

        public static Arbitrary<String> names() {
            return strings().alpha().withChars(' ').ofMinLength(3).ofMaxLength(100)
                    .filter(s -> !s.isBlank());
        }

        public static Arbitrary<LocalDate> datesOfBirth() {
            return oneOf(
                    aliveDatesOfBirth(),
                    deadDatesOfBirth()
            );
        }

        public static Arbitrary<LocalDate> aliveDatesOfBirth() {
            return dates().atTheEarliest(now().minusYears(100)).atTheLatest(now().minusYears(15));
        }

        public static Arbitrary<LocalDate> deadDatesOfBirth() {
            return dates().atTheEarliest(LocalDate.parse("1500-01-01")).atTheLatest(now().minusYears(15));
        }

        public static Arbitrary<LocalDate> datesOfDeath(LocalDate dateOfBirth) {
            if (dateOfBirth.isBefore(now().minusYears(100))) {
                return dates().atTheEarliest(dateOfBirth.plusYears(15)).atTheLatest(dateOfBirth.plusYears(100));
            }
            return dates().atTheEarliest(dateOfBirth.plusYears(15)).atTheLatest(now()).injectNull(0.5);
        }

        public static Arbitrary<LocalDate> deadDatesOfDeath(LocalDate dateOfBirth) {
            if (dateOfBirth.isBefore(now().minusYears(100))) {
                return dates().atTheEarliest(dateOfBirth.plusYears(15)).atTheLatest(dateOfBirth.plusYears(100));
            }
            return dates().atTheEarliest(dateOfBirth.plusYears(15)).atTheLatest(now());
        }
    }

    public static class AuthorBuilder {
        private Author author = authors().sample();

        public AuthorBuilder id(UUID id) {
            author = new Author(id, author.name(), author.dateOfBirth(), author.dateOfDeath(), author.notableBooks());
            return this;
        }

        public AuthorBuilder name(String name) {
            author = new Author(author.id(), name, author.dateOfBirth(), author.dateOfDeath(), author.notableBooks());
            return this;
        }

        public AuthorBuilder dateOfBirth(LocalDate dateOfBirth) {
            author = new Author(author.id(), author.name(), dateOfBirth, author.dateOfDeath(), author.notableBooks());
            return this;
        }

        public AuthorBuilder dateOfDeath(LocalDate dateOfDeath) {
            author = new Author(author.id(), author.name(), author.dateOfBirth(), dateOfDeath, author.notableBooks());
            return this;
        }

        public AuthorBuilder notableBooks(List<Book> notableBooks) {
            author = new Author(author.id(), author.name(), author.dateOfBirth(), author.dateOfDeath(), notableBooks);
            return this;
        }

        public Author build() {
            return author;
        }
    }

}
