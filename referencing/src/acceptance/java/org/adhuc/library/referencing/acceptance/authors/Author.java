package org.adhuc.library.referencing.acceptance.authors;

import org.jspecify.annotations.Nullable;

public record Author(@Nullable String id, String name, String dateOfBirth, @Nullable String dateOfDeath) {

    public Author(String name, String dateOfBirth) {
        this(null, name, dateOfBirth, null);
    }

    public Author(String name, String dateOfBirth, String dateOfDeath) {
        this(null, name, dateOfBirth, dateOfDeath);
    }

    public boolean hasName(String authorName) {
        return name.equals(authorName);
    }

}
