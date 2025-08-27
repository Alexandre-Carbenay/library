package org.adhuc.library.catalog.books;

import org.jmolecules.architecture.onion.classical.ApplicationServiceRing;
import org.springframework.stereotype.Service;

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
        return booksRepository.findById(bookId);
    }

    public Collection<Book> getNotableBooks(UUID authorId) {
        return booksRepository.findNotableByAuthor(authorId);
    }

}
