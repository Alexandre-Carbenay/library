package org.adhuc.library.referencing.publishers;

import org.jmolecules.architecture.onion.classical.ApplicationServiceRing;
import org.springframework.stereotype.Service;

@Service
@ApplicationServiceRing
public class PublishersReferencingService {

    private final PublishersRepository publishersRepository;

    public PublishersReferencingService(PublishersRepository publishersRepository) {
        this.publishersRepository = publishersRepository;
    }

    public Publisher referencePublisher(ReferencePublisher command) {
        ensureNonDuplication(command);
        var publisher = command.buildPublisher();
        publishersRepository.save(publisher);
        return publisher;
    }

    private void ensureNonDuplication(ReferencePublisher command) {
        var existingPublisher = publishersRepository.findByName(command.name());
        if (existingPublisher.isPresent()) {
            throw new DuplicatePublisherException(command.name());
        }
    }

}
