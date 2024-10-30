package org.adhuc.library.catalog.books;

import org.jmolecules.architecture.onion.classical.ApplicationServiceRing;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

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

    public Optional<Book> getBook(String isbn) {
        Assert.notNull(isbn, "Cannot get book from null ISBN");
        return repository.findByIsbn(isbn);
    }

    public Collection<Book> getNotableBooks(UUID authorId) {
        Assert.notNull(authorId, "Cannot get notable books from null author ID");
        return repository.findNotableByAuthor(authorId);
    }

}
