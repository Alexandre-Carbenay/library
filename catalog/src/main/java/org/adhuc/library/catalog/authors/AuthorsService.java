package org.adhuc.library.catalog.authors;

import org.jmolecules.architecture.onion.classical.ApplicationServiceRing;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@ApplicationServiceRing
public class AuthorsService {

    private final AuthorsRepository repository;

    AuthorsService(AuthorsRepository repository) {
        this.repository = repository;
    }

    public Optional<Author> getAuthor(UUID id) {
        return repository.findById(id);
    }

}
