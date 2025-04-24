package org.adhuc.library.catalog.books;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public interface BooksRepository {

    Page<Book> findByLanguage(Locale language, Pageable request);

    Optional<Book> findById(UUID id);

    Collection<Book> findNotableByAuthor(UUID authorId);

}
