package org.adhuc.library.catalog.books.internal;

import org.adhuc.library.catalog.books.Book;
import org.adhuc.library.catalog.books.BooksRepository;
import org.jmolecules.architecture.onion.classical.InfrastructureRing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
    public Page<Book> find(Pageable request) {
        var pageBooks = books.stream().skip(request.getOffset()).limit(request.getPageSize()).toList();
        return new PageImpl<>(pageBooks, request, books.size());
    }

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        return books.stream()
                .filter(book -> book.isbn().equals(isbn))
                .findFirst();
    }

    @Override
    public Collection<Book> findNotableByAuthor(UUID authorId) {
        return List.of();
    }

    public void saveAll(Collection<Book> books) {
        this.books.addAll(books);
    }

}
