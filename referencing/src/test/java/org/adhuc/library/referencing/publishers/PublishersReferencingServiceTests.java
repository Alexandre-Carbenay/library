package org.adhuc.library.referencing.publishers;

import org.adhuc.library.referencing.publishers.internal.InMemoryPublishersRepository;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Publishers referencing service should")
class PublishersReferencingServiceTests {

    private InMemoryPublishersRepository publishersRepository;
    private PublishersReferencingService service;

    @BeforeEach
    void setUp() {
        publishersRepository = new InMemoryPublishersRepository();
        service = new PublishersReferencingService(publishersRepository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t   "})
    @DisplayName("fail referencing publisher if its name is empty")
    void emptyName(String name) {
        assertThrows(IllegalArgumentException.class, () -> new ReferencePublisher(name));
    }

    @Test
    @DisplayName("refuse referencing already existing publisher")
    void referenceExistingPublisher() {
        var name = PublishersMother.Publishers.name();
        publishersRepository.save(new Publisher(UUID.randomUUID(), name));

        var command = new ReferencePublisher(name);
        assertThrows(DuplicatePublisherException.class, () -> service.referencePublisher(command));
    }

    @Test
    @DisplayName("reference publisher successfully, providing a publisher with generated ID")
    void referencePublisherSuccess() {
        var name = PublishersMother.Publishers.name();
        var command = new ReferencePublisher(name);

        var publisher = service.referencePublisher(command);

        SoftAssertions.assertSoftly(s -> {
            s.assertThat(publisher.id()).isNotNull();
            s.assertThat(publisher.name()).isEqualTo(name);
        });
    }

    @Test
    @DisplayName("save publisher when referencing it")
    void referencePublisherSaveIt() {
        var name = PublishersMother.Publishers.name();
        var command = new ReferencePublisher(name);

        var publisher = service.referencePublisher(command);

        assertThat(publishersRepository.findById(publisher.id())).isPresent().contains(publisher);
    }

}
