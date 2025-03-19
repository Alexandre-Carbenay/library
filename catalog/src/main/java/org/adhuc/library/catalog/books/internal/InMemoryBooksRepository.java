package org.adhuc.library.catalog.books.internal;

import org.adhuc.library.catalog.books.Book;
import org.adhuc.library.catalog.books.BooksRepository;
import org.jmolecules.architecture.onion.classical.InfrastructureRing;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@InfrastructureRing
public class InMemoryBooksRepository implements BooksRepository {

    private final List<Book> books = new ArrayList<>();

    public Collection<Book> findAll() {
        return List.copyOf(books);
    }

    @Override
    public Optional<Book> findById(UUID id) {
        return books.stream()
                .filter(book -> book.id().equals(id))
                .findFirst();
    }

    public void saveAll(Collection<Book> books) {
        this.books.addAll(books);
    }

}
