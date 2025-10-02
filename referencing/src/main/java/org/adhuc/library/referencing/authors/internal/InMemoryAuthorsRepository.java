package org.adhuc.library.referencing.authors.internal;

import org.adhuc.library.referencing.authors.Author;
import org.adhuc.library.referencing.authors.AuthorsRepository;
import org.jmolecules.architecture.onion.classical.InfrastructureRing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@InfrastructureRing
public class InMemoryAuthorsRepository implements AuthorsRepository {

    private final Map<UUID, Author> authors = new HashMap<>();

    @Override
    public Page<Author> findPage(Pageable request) {
        var pageAuthors = authors.values().stream()
                .skip(request.getOffset())
                .limit(request.getPageSize())
                .toList();
        return new PageImpl<>(pageAuthors, request, authors.size());
    }

    public Optional<Author> findById(UUID id) {
        return Optional.ofNullable(authors.get(id));
    }

    @Override
    public void save(Author author) {
        authors.put(author.id(), author);
    }

    public void saveAll(Collection<Author> authors) {
        authors.forEach(author -> this.authors.put(author.id(), author));
    }

}
