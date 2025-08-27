package org.adhuc.library.catalog.editions;

import org.jmolecules.architecture.onion.classical.ApplicationServiceRing;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Service
@ApplicationServiceRing
public class EditionsService {

    private final EditionsRepository repository;

    public EditionsService(EditionsRepository repository) {
        this.repository = repository;
    }

    public Optional<Edition> getEdition(String isbn) {
        return repository.findByIsbn(isbn);
    }

    public Collection<Edition> getBookEditions(UUID bookId) {
        return repository.findByBookId(bookId);
    }
}
