package org.adhuc.library.catalog.authors.internal;

import org.adhuc.library.catalog.authors.Author;
import org.adhuc.library.catalog.authors.AuthorsRepository;
import org.jmolecules.architecture.onion.classical.InfrastructureRing;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@InfrastructureRing
public class InMemoryAuthorsRepository implements AuthorsRepository {

    private final List<Author> authors = new ArrayList<>();

    public Collection<Author> findAll() {
        return List.copyOf(authors);
    }

    @Override
    public Optional<Author> findById(UUID id) {
        return authors.stream().filter(author -> author.id().equals(id)).findFirst();
    }

    public void saveAll(Collection<Author> authors) {
        this.authors.addAll(authors);
    }

}
