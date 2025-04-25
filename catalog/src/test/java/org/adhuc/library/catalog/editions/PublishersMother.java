package org.adhuc.library.catalog.editions;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;

import java.util.UUID;

import static net.jqwik.api.Arbitraries.strings;

public final class PublishersMother {

    public static Arbitrary<Publisher> publishers() {
        return Combinators.combine(Publishers.ids(), Publishers.names()).as(Publisher::new);
    }

    public static PublisherBuilder builder() {
        return new PublisherBuilder();
    }

    public static final class Publishers {
        public static Arbitrary<UUID> ids() {
            return Arbitraries.create(UUID::randomUUID);
        }

        public static Arbitrary<String> names() {
            return strings().alpha().withChars(' ').ofMinLength(3).ofMaxLength(100)
                    .filter(s -> !s.isBlank());
        }
    }

    public static class PublisherBuilder {
        private Publisher publisher = publishers().sample();

        public PublisherBuilder id(UUID id) {
            publisher = new Publisher(id, publisher.name());
            return this;
        }

        public PublisherBuilder name(String name) {
            publisher = new Publisher(publisher.id(), name);
            return this;
        }

        public Publisher build() {
            return publisher;
        }
    }

    public static final class Real {

        public static final Publisher GALLIMARD = new Publisher(
                UUID.fromString("14238936-2956-4770-a118-3a08bf7e721f"),
                "Gallimard"
        );

        public static final Publisher LGF = new Publisher(
                UUID.fromString("6f56ac0d-62ba-4c88-91c9-b2f5875fa5e7"),
                "Librairie générale française"
        );

        public static final Publisher HACHETTE = new Publisher(
                UUID.fromString("47beaf39-6cef-4468-b08b-f42ea3153424"),
                "Hachette"
        );

        public static final Publisher GALLIMARD_JEUNESSE = new Publisher(
                UUID.fromString("e2094232-0654-40f7-8e39-acecf7925eea"),
                "Gallimard Jeunesse"
        );

    }

}
