package org.adhuc.library.catalog.books;

import org.jmolecules.architecture.onion.classical.ApplicationServiceRing;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Service
@ApplicationServiceRing
public class BooksService {

    private final BooksRepository repository;

    public BooksService(BooksRepository repository) {
        this.repository = repository;
    }

    public Optional<Book> getBook(UUID id) {
        return repository.findById(id);
    }

    public Collection<Book> getNotableBooks(UUID authorId) {
        return repository.findNotableByAuthor(authorId);
    }

}
