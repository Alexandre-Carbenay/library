package org.adhuc.library.catalog.authors;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.UUID;

import static java.time.LocalDate.now;
import static net.jqwik.api.Arbitraries.oneOf;
import static net.jqwik.api.Arbitraries.strings;
import static net.jqwik.time.api.Dates.dates;

public final class AuthorsMother {

    public static Arbitrary<Author> authors() {
        return Combinators.combine(Authors.ids(), Authors.names(), Authors.datesOfBirth())
                .flatAs((id, name, dateOfBirth) -> Authors.datesOfDeath(dateOfBirth)
                        .map(dateOfDeath -> new Author(id, name, dateOfBirth, dateOfDeath)));
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
            author = new Author(id, author.name(), author.dateOfBirth(), author.dateOfDeath());
            return this;
        }

        public AuthorBuilder name(String name) {
            author = new Author(author.id(), name, author.dateOfBirth(), author.dateOfDeath());
            return this;
        }

        public AuthorBuilder dateOfBirth(LocalDate dateOfBirth) {
            author = new Author(author.id(), author.name(), dateOfBirth, author.dateOfDeath());
            return this;
        }

        public AuthorBuilder dateOfDeath(@Nullable LocalDate dateOfDeath) {
            author = new Author(author.id(), author.name(), author.dateOfBirth(), dateOfDeath);
            return this;
        }

        public Author build() {
            return author;
        }
    }

    public static final class Real {

        public static final Author ALBERT_CAMUS = new Author(
                UUID.fromString("f921c511-6341-494a-8152-c92613db248b"),
                "Albert Camus",
                LocalDate.parse("1913-11-07"),
                LocalDate.parse("1960-01-04")
        );
        public static final Author GUSTAVE_FLAUBERT = new Author(
                UUID.fromString("40ad7b89-7aec-45ae-bdd1-410e5a11f44b"),
                "Gustave Flaubert",
                LocalDate.parse("1821-12-12"),
                LocalDate.parse("1880-08-08")
        );
        public static final Author LEON_TOLSTOI = new Author(
                UUID.fromString("2c98db8d-d0c3-4dfb-8cfe-355256300a91"),
                "Léon Tolstoï",
                LocalDate.parse("1828-08-28"),
                LocalDate.parse("1910-11-07")
        );
        public static final Author VIRGINIE_DESPENTES = new Author(
                UUID.fromString("b61b590b-fdf8-436a-80bd-03c1591e482c"),
                "Virginie Despentes",
                LocalDate.parse("1969-06-13"),
                null
        );
        public static final Author RENE_GOSCINNY = new Author(
          UUID.fromString("91f97618-5534-4b50-a541-bde0bf2667b3"),
                "René Goscinny",
                LocalDate.parse("1926-08-14"),
                LocalDate.parse("1977-11-05")
        );
        public static final Author ALBERT_UDERZO = new Author(
                UUID.fromString("a992aaca-b2d1-41b4-ada3-b45405906a9a"),
                "Albert Uderzo",
                LocalDate.parse("1927-04-25"),
                LocalDate.parse("2020-03-24")
        );
        public static final Author JEAN_JACQUES_SEMPE = new Author(
                UUID.fromString("2e849dbc-a392-4178-bc99-24c859306180"),
                "Jean-Jacques Sempé",
                LocalDate.parse("1932-08-17"),
                LocalDate.parse("2022-08-11")
        );
        public static final Author NICOLAS_FRAMONT = new Author(
                UUID.fromString("4db5405a-1d4d-4178-81e6-3c23005fb054"),
                "Nicolas Framont",
                LocalDate.parse("1988-01-01"),
                null
        );

    }

}
