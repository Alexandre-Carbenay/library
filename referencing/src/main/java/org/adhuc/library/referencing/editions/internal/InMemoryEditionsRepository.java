package org.adhuc.library.referencing.editions.internal;

import org.adhuc.library.referencing.editions.Edition;
import org.adhuc.library.referencing.editions.EditionsRepository;
import org.jmolecules.architecture.onion.classical.InfrastructureRing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@InfrastructureRing
public class InMemoryEditionsRepository implements EditionsRepository {

    private final Map<String, Edition> editions = new HashMap<>();

    @Override
    public Page<Edition> findPage(Pageable request) {
        var pageEditions = editions.values().stream()
                .skip(request.getOffset())
                .limit(request.getPageSize())
                .toList();
        return new PageImpl<>(pageEditions, request, editions.size());
    }

    @Override
    public Optional<Edition> findByIsbn(String isbn) {
        return Optional.ofNullable(editions.get(isbn));
    }

    @Override
    public void save(Edition edition) {
        editions.put(edition.isbn(), edition);
    }

    public void saveAll(List<Edition> editions) {
        editions.forEach(this::save);
    }

}
