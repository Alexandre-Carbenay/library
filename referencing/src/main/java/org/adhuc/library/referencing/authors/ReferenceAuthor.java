package org.adhuc.library.referencing.authors;

import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.Optional;

public class ReferenceAuthor implements AliveOrDead {

    private final String name;
    private final LocalDate dateOfBirth;
    @Nullable
    private final LocalDate dateOfDeath;

    public ReferenceAuthor(String name, LocalDate dateOfBirth) {
        this(name, dateOfBirth, null);
    }

    public ReferenceAuthor(String name, LocalDate dateOfBirth, @Nullable LocalDate dateOfDeath) {
        this.name = name.trim();
        this.dateOfBirth = dateOfBirth;
        this.dateOfDeath = dateOfDeath;
        if (this.name.isEmpty()) {
            throw new IllegalArgumentException("An author cannot have empty name");
        }
        if (!isBornBeforeDead()) {
            throw new IllegalArgumentException("An author cannot be dead before being born");
        }
    }

    public String name() {
        return name;
    }

    public LocalDate dateOfBirth() {
        return dateOfBirth;
    }

    public Optional<LocalDate> dateOfDeath() {
        return Optional.ofNullable(dateOfDeath);
    }

}
