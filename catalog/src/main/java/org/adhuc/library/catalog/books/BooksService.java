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

    private final BooksRepository booksRepository;

    public BooksService(BooksRepository booksRepository) {
        this.booksRepository = booksRepository;
    }

    public Optional<Book> getBook(UUID bookId) {
        Assert.notNull(bookId, "Cannot get book from null ID");
        return booksRepository.findById(bookId);
    }

    public Collection<Book> getNotableBooks(UUID authorId) {
        Assert.notNull(authorId, "Cannot get notable books from null author ID");
        return booksRepository.findNotableByAuthor(authorId);
    }

}
