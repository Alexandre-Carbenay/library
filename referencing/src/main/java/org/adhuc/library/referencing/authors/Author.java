package org.adhuc.library.referencing.authors;

import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Author {

    private final UUID id;
    private final String name;
    private final LocalDate dateOfBirth;
    @Nullable
    private final LocalDate dateOfDeath;

    public Author(UUID id, String name, LocalDate dateOfBirth) {
        this(id, name, dateOfBirth, null);
    }

    public Author(UUID id, String name, LocalDate dateOfBirth, @Nullable LocalDate dateOfDeath) {
        this.id = id;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.dateOfDeath = dateOfDeath;
    }

    public UUID id() {
        return id;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Author author = (Author) o;
        return Objects.equals(id, author.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Author(" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", dateOfDeath=" + dateOfDeath +
                ')';
    }

}
