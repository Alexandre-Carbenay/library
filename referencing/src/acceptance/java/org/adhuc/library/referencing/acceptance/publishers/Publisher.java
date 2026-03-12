package org.adhuc.library.referencing.acceptance.publishers;

import org.jspecify.annotations.Nullable;

public record Publisher(@Nullable String id, String name) {

    public Publisher(String name) {
        this(null, name);
    }

    public boolean hasName(String publisherName) {
        return name.equals(publisherName);
    }

}
