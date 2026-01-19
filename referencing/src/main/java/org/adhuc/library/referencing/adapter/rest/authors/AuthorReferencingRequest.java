package org.adhuc.library.referencing.adapter.rest.authors;

import jakarta.validation.constraints.NotBlank;
import org.adhuc.library.referencing.authors.AliveOrDead;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.Optional;

@BornBeforeDead(pointerName = "/date_of_death")
public class AuthorReferencingRequest implements AliveOrDead {
    @NotBlank
    private final String name;
    private final LocalDate dateOfBirth;
    @Nullable
    private final LocalDate dateOfDeath;

    public AuthorReferencingRequest(String name, LocalDate dateOfBirth, @Nullable LocalDate dateOfDeath) {
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.dateOfDeath = dateOfDeath;
    }

    public String name() {
        return name;
    }

    @Override
    public LocalDate dateOfBirth() {
        return dateOfBirth;
    }

    @Override
    public Optional<LocalDate> dateOfDeath() {
        return Optional.ofNullable(dateOfDeath);
    }

    // Classic getters are required for validation messages formatting

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    @Nullable
    public LocalDate getDateOfDeath() {
        return dateOfDeath;
    }
}
