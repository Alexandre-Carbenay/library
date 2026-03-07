package org.adhuc.library.referencing.books;

import org.jmolecules.architecture.onion.classical.ApplicationServiceRing;
import org.springframework.stereotype.Service;

@Service
@ApplicationServiceRing
public class BooksReferencingService {

    private final BooksRepository booksRepository;

    public BooksReferencingService(BooksRepository booksRepository) {
        this.booksRepository = booksRepository;
    }

    public Book referenceBook(ReferenceBook command) {
        ensureNonDuplication(command);
        var book = command.buildBook();
        booksRepository.save(book);
        return book;
    }

    private void ensureNonDuplication(ReferenceBook command) {
        var existingBook = booksRepository.findByTitleInAndAuthorsIn(command.titles(), command.authors());
        if (existingBook.isPresent()) {
            throw command.prepareDuplication(existingBook.get())
                    .orElseThrow(() -> new IllegalStateException("Detected a duplication but cannot find the corresponding detail"));
        }
    }

}
