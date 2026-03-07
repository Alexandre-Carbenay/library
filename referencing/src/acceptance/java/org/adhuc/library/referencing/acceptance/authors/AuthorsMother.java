package org.adhuc.library.referencing.acceptance.authors;

import net.datafaker.Faker;

import java.time.LocalDate;

public class AuthorsMother {
    private static final Faker FAKER = new Faker();

    public static LocalDate dateOfBirth() {
        return FAKER.timeAndDate().birthday(20, 100);
    }
}
