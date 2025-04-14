package org.adhuc.library.catalog.editions.internal;

import org.adhuc.library.catalog.editions.Edition;
import org.adhuc.library.catalog.editions.EditionsRepository;
import org.jmolecules.architecture.onion.classical.InfrastructureRing;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@InfrastructureRing
public class InMemoryEditionsRepository implements EditionsRepository {

    private final List<Edition> editions = new ArrayList<>();

    public Collection<Edition> findAll() {
        return List.copyOf(editions);
    }

    @Override
    public Optional<Edition> findByIsbn(String isbn) {
        return editions.stream()
                .filter(edition -> edition.isbn().equals(isbn))
                .findFirst();
    }

    @Override
    public Collection<Edition> findByBookId(UUID bookId) {
        return editions.stream()
                .filter(edition -> bookId.equals(edition.book().id()))
                .toList();
    }

    @Override
    public Collection<Edition> findNotableByAuthor(UUID authorId) {
        return editions.stream()
                .filter(edition -> edition.book().authors().stream().anyMatch(author -> author.id().equals(authorId)))
                .toList();
    }

    public void saveAll(Collection<Edition> editions) {
        this.editions.addAll(editions);
    }

}
