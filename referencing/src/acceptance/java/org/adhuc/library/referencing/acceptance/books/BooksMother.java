package org.adhuc.library.referencing.acceptance.books;

import net.datafaker.Faker;

public class BooksMother {

    private static final Faker FAKER = new Faker();

    public static String description() {
        return FAKER.text().text(30, 1000);
    }

}
