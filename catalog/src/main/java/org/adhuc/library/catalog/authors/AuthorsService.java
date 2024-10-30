package org.adhuc.library.catalog.authors;

import org.jmolecules.architecture.onion.classical.ApplicationServiceRing;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

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
        Assert.notNull(id, "Cannot get author from null ID");
        return repository.findById(id);
    }

}
