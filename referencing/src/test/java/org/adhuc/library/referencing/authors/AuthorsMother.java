package org.adhuc.library.referencing.authors;

import net.datafaker.Faker;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static java.time.LocalDate.now;
import static java.time.ZoneId.systemDefault;
import static java.time.ZoneOffset.UTC;
import static org.adhuc.library.referencing.authors.AuthorsMother.Authors.aliveDateOfBirth;
import static org.adhuc.library.referencing.authors.AuthorsMother.Authors.deadDateOfDeath;

public class AuthorsMother {

    public static List<Author> authors(int size) {
        return IntStream.range(0, size)
                .mapToObj(_ -> author())
                .toList();
    }

    public static Author author() {
        var dateOfBirth = Authors.dateOfBirth();
        return new Author(
                Authors.id(),
                Authors.name(),
                dateOfBirth,
                Authors.dateOfDeath(dateOfBirth)
        );
    }

    public static AuthorBuilder builder() {
        return new AuthorBuilder();
    }

    public static final class Authors {
        private static final int MINIMAL_AGE = 15;
        private static final int MAXIMAL_AGE = 100;
        private static final Faker FAKER = new Faker();

        public static UUID id() {
            return UUID.randomUUID();
        }

        public static String name() {
            return FAKER.book().author();
        }

        public static LocalDate dateOfBirth() {
            return FAKER.timeAndDate().birthday();
        }

        public static LocalDate aliveDateOfBirth() {
            return FAKER.timeAndDate().birthday(MINIMAL_AGE, MAXIMAL_AGE);
        }

        @Nullable
        public static LocalDate dateOfDeath(LocalDate dateOfBirth) {
            var canBeAlive = dateOfBirth.isAfter(now().minusYears(100));
            var isAlive = canBeAlive && FAKER.bool().bool();
            if (isAlive) {
                return null;
            }
            var maxDateOfDeath = canBeAlive ? now() : dateOfBirth.plusYears(MAXIMAL_AGE);
            return FAKER.timeAndDate().between(
                    dateOfBirth.plusYears(MINIMAL_AGE).atStartOfDay().toInstant(UTC),
                    maxDateOfDeath.atStartOfDay().toInstant(UTC)
            ).atZone(systemDefault()).toLocalDate();
        }

        public static LocalDate deadDateOfDeath(LocalDate dateOfBirth) {
            var canBeAlive = dateOfBirth.isAfter(now().minusYears(100));
            var maxDateOfDeath = canBeAlive ? now() : dateOfBirth.plusYears(MAXIMAL_AGE);
            return FAKER.timeAndDate().between(
                    dateOfBirth.plusYears(MINIMAL_AGE).atStartOfDay().toInstant(UTC),
                    maxDateOfDeath.atStartOfDay().toInstant(UTC)
            ).atZone(systemDefault()).toLocalDate();
        }
    }

    public static class AuthorBuilder {
        private Author author = author();

        public AuthorBuilder id(UUID id) {
            author = new Author(id, author.name(), author.dateOfBirth(), author.dateOfDeath().orElse(null));
            return this;
        }

        public AuthorBuilder name(String name) {
            author = new Author(author.id(), name, author.dateOfBirth(), author.dateOfDeath().orElse(null));
            return this;
        }

        public AuthorBuilder dateOfBirth(LocalDate dateOfBirth) {
            author = new Author(author.id(), author.name(), dateOfBirth, author.dateOfDeath().orElse(null));
            return this;
        }

        public AuthorBuilder alive() {
            var dateOfBirth = aliveDateOfBirth();
            author = new Author(author.id(), author.name(), dateOfBirth, null);
            return this;
        }

        public AuthorBuilder dateOfDeath(@Nullable LocalDate dateOfDeath) {
            author = new Author(author.id(), author.name(), author.dateOfBirth(), dateOfDeath);
            return this;
        }

        public AuthorBuilder dead() {
            var dateOfDeath = deadDateOfDeath(author.dateOfBirth());
            author = new Author(author.id(), author.name(), author.dateOfBirth(), dateOfDeath);
            return this;
        }

        public Author build() {
            return author;
        }
    }

}
