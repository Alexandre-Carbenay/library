package org.adhuc.library.catalog.books;

import org.adhuc.library.catalog.editions.Edition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface BooksRepository {

    Page<Book> find(Pageable request);

    Optional<Book> findById(UUID id);

}
