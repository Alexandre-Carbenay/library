package org.adhuc.library.catalog.editions;

import org.jmolecules.architecture.onion.classical.DomainServiceRing;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@DomainServiceRing
public interface EditionsRepository {

    Optional<Edition> findByIsbn(String isbn);

    Collection<Edition> findByBookIds(Collection<UUID> bookIds);

    Collection<Edition> findNotableByAuthor(UUID authorId);

}
