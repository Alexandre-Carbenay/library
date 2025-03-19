package org.adhuc.library.catalog.books;

import java.util.Optional;
import java.util.UUID;

public interface BooksRepository {

    Optional<Book> findById(UUID id);

}
