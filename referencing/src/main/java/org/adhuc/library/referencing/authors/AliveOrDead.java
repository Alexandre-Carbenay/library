package org.adhuc.library.referencing.authors;

import java.time.LocalDate;
import java.util.Optional;

public interface AliveOrDead {

    LocalDate dateOfBirth();

    Optional<LocalDate> dateOfDeath();

    default boolean isBornBeforeDead() {
        return dateOfDeath().map(death -> dateOfBirth().isBefore(death)).orElse(true);
    }

}
