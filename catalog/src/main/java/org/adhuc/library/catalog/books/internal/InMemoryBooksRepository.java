package org.adhuc.library.catalog.books.internal;

import org.adhuc.library.catalog.books.Book;
import org.adhuc.library.catalog.books.BooksRepository;
import org.jmolecules.architecture.onion.classical.InfrastructureRing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@InfrastructureRing
class InMemoryBooksRepository implements BooksRepository {

    @Override
    public Page<Book> find(Pageable request) {
        return Page.empty();
    }

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        return Optional.empty();
    }

    @Override
    public Collection<Book> findNotableByAuthor(UUID authorId) {
        return List.of();
    }

}
