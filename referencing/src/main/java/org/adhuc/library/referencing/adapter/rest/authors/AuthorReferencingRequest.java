package org.adhuc.library.referencing.adapter.rest.authors;

import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;

record AuthorReferencingRequest(@NotBlank String name, LocalDate dateOfBirth, @Nullable LocalDate dateOfDeath) {
}
