package org.adhuc.library.referencing.books;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface BooksRepository {

    Page<Book> findPage(Pageable request);

    Optional<Book> findById(UUID id);

    Optional<Book> findByTitleInAndAuthorsIn(Collection<String> titles, Collection<UUID> authors);

    void save(Book book);
}
