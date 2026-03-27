package org.adhuc.library.referencing.editions;

import java.time.LocalDate;
import java.util.UUID;

public record Edition(String isbn, String language, UUID book, UUID publisher, LocalDate publicationDate, String title, String summary) {
}
