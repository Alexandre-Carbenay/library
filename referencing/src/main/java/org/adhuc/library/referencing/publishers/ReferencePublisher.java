package org.adhuc.library.referencing.publishers;

import java.util.UUID;

public record ReferencePublisher(String name) {

    public ReferencePublisher(String name) {
        this.name = name.trim();
        if (this.name.isEmpty()) {
            throw new IllegalArgumentException("A publisher cannot have empty name");
        }
    }

    Publisher buildPublisher() {
        return new Publisher(UUID.randomUUID(), name);
    }

}
