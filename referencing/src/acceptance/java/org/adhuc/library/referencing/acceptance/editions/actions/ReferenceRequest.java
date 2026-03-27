package org.adhuc.library.referencing.acceptance.editions.actions;

import org.jspecify.annotations.Nullable;

import java.time.LocalDate;

public record ReferenceRequest(@Nullable String isbn,
                               @Nullable String language,
                               @Nullable String book,
                               @Nullable String publisher,
                               @Nullable LocalDate publicationDate,
                               @Nullable String title,
                               @Nullable String summary) {
}
