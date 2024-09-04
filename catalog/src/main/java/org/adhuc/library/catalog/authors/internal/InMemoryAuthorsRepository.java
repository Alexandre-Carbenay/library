package org.adhuc.library.catalog.authors.internal;

import org.adhuc.library.catalog.authors.Author;
import org.adhuc.library.catalog.authors.AuthorsRepository;
import org.jmolecules.architecture.onion.classical.InfrastructureRing;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@InfrastructureRing
class InMemoryAuthorsRepository implements AuthorsRepository {

    @Override
    public Optional<Author> findById(UUID id) {
        return Optional.empty();
    }

}
