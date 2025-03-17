package org.adhuc.library.catalog.editions;

import java.time.LocalDate;

public sealed interface PublicationDate permits PublicationDate.ExactPublicationDate, PublicationDate.YearPublicationDate {

    static PublicationDate of(LocalDate date) {
        return new ExactPublicationDate(date);
    }

    static PublicationDate of(int year) {
        return new YearPublicationDate(year);
    }

    record ExactPublicationDate(LocalDate date) implements PublicationDate {
        @Override
        public String toString() {
            return date.toString();
        }
    }

    record YearPublicationDate(int year) implements PublicationDate {
        @Override
        public String toString() {
            return Integer.toString(year);
        }
    }

}
