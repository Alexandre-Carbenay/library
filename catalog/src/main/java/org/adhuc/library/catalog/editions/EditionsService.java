package org.adhuc.library.catalog.editions;

import org.jmolecules.architecture.onion.classical.ApplicationServiceRing;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

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
        Assert.notNull(isbn, "Cannot get edition from null ISBN");
        return repository.findByIsbn(isbn);
    }

    public Collection<Edition> getBookEditions(UUID bookId) {
        Assert.notNull(bookId, "Cannot get editions from null book");
        return repository.findByBookId(bookId);
    }

    public Collection<Edition> getNotableEditions(UUID authorId) {
        Assert.notNull(authorId, "Cannot get notable editions from null author ID");
        return repository.findNotableByAuthor(authorId);
    }
}
