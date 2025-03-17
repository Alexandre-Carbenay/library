package org.adhuc.library.catalog.editions.internal;

import org.adhuc.library.catalog.editions.Edition;
import org.adhuc.library.catalog.editions.EditionsRepository;
import org.jmolecules.architecture.onion.classical.InfrastructureRing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
    public Page<Edition> find(Pageable request) {
        var pageEditions = editions.stream().skip(request.getOffset()).limit(request.getPageSize()).toList();
        return new PageImpl<>(pageEditions, request, editions.size());
    }

    @Override
    public Optional<Edition> findByIsbn(String isbn) {
        return editions.stream()
                .filter(edition -> edition.isbn().equals(isbn))
                .findFirst();
    }

    @Override
    public Collection<Edition> findNotableByAuthor(UUID authorId) {
        return editions.stream()
                .filter(edition -> edition.authors().stream().anyMatch(author -> author.id().equals(authorId)))
                .toList();
    }

    public void saveAll(Collection<Edition> editions) {
        this.editions.addAll(editions);
    }

}
