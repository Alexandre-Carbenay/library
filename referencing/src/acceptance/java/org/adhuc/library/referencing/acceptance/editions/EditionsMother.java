package org.adhuc.library.referencing.acceptance.editions;

import net.datafaker.Faker;

import java.time.LocalDate;
import java.time.ZoneId;

public class EditionsMother {

    private static final Faker FAKER = new Faker();

    public static LocalDate publicationDate() {
        return LocalDate.ofInstant(FAKER.timeAndDate().past(), ZoneId.systemDefault());
    }

    public static String summary() {
        return FAKER.text().text(30, 1000);
    }

}
