package org.adhuc.library.referencing.books;

import java.util.Set;
import java.util.UUID;

public class BooksReferencingService {

    public Book referenceBook(ReferenceBook command) {
        return new Book(UUID.randomUUID(), Set.copyOf(command.authors()), command.originalLanguage(), Set.copyOf(command.details()));
    }

}
