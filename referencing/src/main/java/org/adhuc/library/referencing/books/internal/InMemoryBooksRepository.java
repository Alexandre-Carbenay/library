package org.adhuc.library.referencing.books.internal;

import org.adhuc.library.referencing.books.Book;
import org.adhuc.library.referencing.books.BooksRepository;
import org.jmolecules.architecture.onion.classical.InfrastructureRing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@InfrastructureRing
public class InMemoryBooksRepository implements BooksRepository {

    private final Map<UUID, Book> books = new HashMap<>();

    @Override
    public Page<Book> findPage(Pageable request) {
        var pageBooks = books.values().stream()
                .skip(request.getOffset())
                .limit(request.getPageSize())
                .toList();
        return new PageImpl<>(pageBooks, request, books.size());
    }

    @Override
    public Optional<Book> findByTitleInAndAuthorsIn(Collection<String> titles, Collection<UUID> authors) {
        return books.values().stream()
                .filter(book -> book.hasSimilarAuthors(authors))
                .filter(book -> book.hasSimilarTitles(titles))
                .findFirst();
    }

    @Override
    public Optional<Book> findById(UUID id) {
        return Optional.ofNullable(books.get(id));
    }

    @Override
    public void save(Book book) {
        books.put(book.id(), book);
    }

    public void saveAll(List<Book> books) {
        books.forEach(this::save);
    }

}
