package org.adhuc.library.referencing.adapter.rest.authors;

import org.jspecify.annotations.Nullable;

import java.time.LocalDate;

record AuthorReferencingRequest(String name, LocalDate dateOfBirth, @Nullable LocalDate dateOfDeath) {
}
