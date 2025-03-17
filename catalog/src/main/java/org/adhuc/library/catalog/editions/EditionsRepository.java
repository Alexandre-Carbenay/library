package org.adhuc.library.catalog.editions;

import org.jmolecules.architecture.onion.classical.DomainServiceRing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@DomainServiceRing
public interface EditionsRepository {

    Page<Edition> find(Pageable request);

    Optional<Edition> findByIsbn(String isbn);

    Collection<Edition> findNotableByAuthor(UUID authorId);

}
