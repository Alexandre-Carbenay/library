package org.adhuc.library.referencing.adapter.rest.editions;

import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.UUID;

record EditionReferencingRequest(
        @Isbn String isbn,
        @NotBlank String language,
        UUID book,
        UUID publisher,
        LocalDate publicationDate,
        @NullOrNotBlank @Nullable String title,
        @NullOrNotBlank @Nullable String summary) {
}
