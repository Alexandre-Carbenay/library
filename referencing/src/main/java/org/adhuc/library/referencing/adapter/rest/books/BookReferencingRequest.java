package org.adhuc.library.referencing.adapter.rest.books;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.UUID;

@UniqueLanguageInDetails
public record BookReferencingRequest(List<UUID> authors, @NotBlank String originalLanguage, @Valid List<BookReferencingDetail> details) {
    public record BookReferencingDetail(@NotBlank String language, @NotBlank String title, @NotBlank String description) {
    }
}
