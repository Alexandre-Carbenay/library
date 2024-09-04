package org.adhuc.library.catalog.authors;

import org.jmolecules.architecture.onion.classical.DomainServiceRing;

import java.util.Optional;
import java.util.UUID;

@DomainServiceRing
public interface AuthorsRepository {

    Optional<Author> findById(UUID id);

}
