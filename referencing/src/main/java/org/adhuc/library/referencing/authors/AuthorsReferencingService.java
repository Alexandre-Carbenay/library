package org.adhuc.library.referencing.authors;

import org.jmolecules.architecture.onion.classical.ApplicationServiceRing;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@ApplicationServiceRing
public class AuthorsReferencingService {

    private final AuthorsRepository authorsRepository;

    public AuthorsReferencingService(AuthorsRepository authorsRepository) {
        this.authorsRepository = authorsRepository;
    }

    public Author referenceAuthor(ReferenceAuthor command) {
        var author = new Author(UUID.randomUUID(), command.name(), command.dateOfBirth(), command.dateOfDeath().orElse(null));
        authorsRepository.save(author);
        return author;
    }

}
