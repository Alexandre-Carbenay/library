package org.adhuc.library.referencing.publishers;

import net.datafaker.Faker;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.adhuc.library.referencing.authors.AuthorsMother.Authors.name;
import static org.adhuc.library.referencing.publishers.PublishersMother.Publishers.id;

public class PublishersMother {

    public static List<Publisher> publishers(int size) {
        return IntStream.range(0, size)
                .mapToObj(_ -> publisher())
                .toList();
    }

    public static Publisher publisher() {
        return new Publisher(id(), name());
    }

    public static PublishersMother.PublisherBuilder builder() {
        return new PublishersMother.PublisherBuilder();
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

        public PublishersMother.PublisherBuilder id(UUID id) {
            publisher = new Publisher(id, publisher.name());
            return this;
        }

        public PublishersMother.PublisherBuilder name(String name) {
            publisher = new Publisher(publisher.id(), name);
            return this;
        }

        public Publisher build() {
            return publisher;
        }
    }

}
