package org.adhuc.library.referencing.editions;

import org.adhuc.library.referencing.books.Book;
import org.adhuc.library.referencing.books.BooksRepository;
import org.adhuc.library.referencing.publishers.PublishersRepository;
import org.jmolecules.architecture.onion.classical.ApplicationServiceRing;
import org.springframework.stereotype.Service;

@Service
@ApplicationServiceRing
public class EditionsReferencingService {

    private final BooksRepository booksRepository;
    private final PublishersRepository publishersRepository;
    private final EditionsRepository editionsRepository;

    public EditionsReferencingService(BooksRepository booksRepository, PublishersRepository publishersRepository, EditionsRepository editionsRepository) {
        this.booksRepository = booksRepository;
        this.publishersRepository = publishersRepository;
        this.editionsRepository = editionsRepository;
    }

    public Edition referenceEdition(ReferenceEdition command) {
        ensureNonDuplication(command);
        var book = ensureExistingBook(command);
        ensureExistingPublisher(command);
        var edition = command.buildEdition(book);
        editionsRepository.save(edition);
        return edition;
    }

    private void ensureNonDuplication(ReferenceEdition command) {
        var existingEdition = editionsRepository.findByIsbn(command.isbn());
        if (existingEdition.isPresent()) {
            throw new DuplicateEditionException(command.isbn());
        }
    }

    private Book ensureExistingBook(ReferenceEdition command) {
        var existingBook = booksRepository.findById(command.book());
        if (existingBook.isEmpty()) {
            throw new UnknownEditionBookException(command.isbn(), command.book());
        }
        return existingBook.get();
    }

    private void ensureExistingPublisher(ReferenceEdition command) {
        var existingPublisher = publishersRepository.findById(command.publisher());
        if (existingPublisher.isEmpty()) {
            throw new UnknownEditionPublisherException(command.isbn(), command.publisher());
        }
    }

}
