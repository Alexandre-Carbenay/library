package org.adhuc.library.website.catalog;

import java.time.LocalDate;

public record Edition(String isbn, String title, String publisher, LocalDate publicationDate, String language) {
}
