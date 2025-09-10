package org.adhuc.library.catalog.editions;

import net.datafaker.Faker;

import java.util.UUID;

public final class PublishersMother {

    public static Publisher publisher() {
        return new Publisher(
                Publishers.id(),
                Publishers.name()
        );
    }

    public static PublisherBuilder builder() {
        return new PublisherBuilder();
    }

    public static final class Publishers {
        private static final Faker FAKER = new Faker();

        public static UUID id() {
            return UUID.randomUUID();
        }

        public static String name() {
            return FAKER.book().publisher();
        }
    }

    public static class PublisherBuilder {
        private Publisher publisher = publisher();

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

        public static final Publisher LES_LIENS_QUI_LIBERENT = new Publisher(
                UUID.fromString("41d587d4-34b9-472e-a48a-80d6b63d1bcd"),
                "Les Liens qui Libèrent"
        );

    }

}
