package org.adhuc.library.catalog.books;

import org.jmolecules.architecture.onion.classical.DomainServiceRing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@DomainServiceRing
public interface BooksRepository {

    Page<Book> find(Pageable request);

    Optional<Book> findByIsbn(String isbn);

    Collection<Book> findNotableByAuthor(UUID authorId);

}
