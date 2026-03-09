package org.adhuc.library.referencing.adapter.rest.publishers;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.adhuc.library.referencing.publishers.Publisher;
import org.springframework.hateoas.RepresentationModel;

import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;

@JsonAutoDetect(fieldVisibility = ANY)
class PublisherModel extends RepresentationModel<PublisherModel> {
    private final UUID id;
    private final String name;

    public PublisherModel(Publisher publisher) {
        this.id = publisher.id();
        this.name = publisher.name();
    }

    public UUID id() {
        return id;
    }
}
