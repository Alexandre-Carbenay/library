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
    public Page<Book> findByLanguage(Locale language, Pageable request) {
        var booksWithLanguage = books.stream()
                .filter(book -> book.acceptsLanguage(language))
                .toList();
        var pageBooks = booksWithLanguage.stream()
                .skip(request.getOffset())
                .limit(request.getPageSize())
                .toList();
        return new PageImpl<>(pageBooks, request, booksWithLanguage.size());
    }

    @Override
    public Optional<Book> findById(UUID id) {
        return books.stream()
                .filter(book -> book.id().equals(id))
                .findFirst();
    }

    @Override
    public Collection<Book> findNotableByAuthor(UUID authorId) {
        return books.stream()
                .filter(book -> book.authors().stream().anyMatch(author -> author.id().equals(authorId)))
                .toList();
    }

    public void saveAll(Collection<Book> books) {
        this.books.clear();
        this.books.addAll(books);
    }

}
