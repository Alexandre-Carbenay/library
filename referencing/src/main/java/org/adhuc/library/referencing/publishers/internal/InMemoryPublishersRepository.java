package org.adhuc.library.referencing.publishers.internal;

import org.adhuc.library.referencing.publishers.Publisher;
import org.adhuc.library.referencing.publishers.PublishersRepository;
import org.jmolecules.architecture.onion.classical.InfrastructureRing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@InfrastructureRing
public class InMemoryPublishersRepository implements PublishersRepository {

    private final Map<UUID, Publisher> publishers = new HashMap<>();

    @Override
    public Page<Publisher> findPage(Pageable request) {
        var pagePublishers = publishers.values().stream()
                .skip(request.getOffset())
                .limit(request.getPageSize())
                .toList();
        return new PageImpl<>(pagePublishers, request, publishers.size());
    }

    @Override
    public Optional<Publisher> findById(UUID id) {
        return Optional.ofNullable(publishers.get(id));
    }

    @Override
    public Optional<Publisher> findByName(String name) {
        return publishers.values().stream().filter(publisher -> publisher.name().equals(name)).findFirst();
    }

    @Override
    public void save(Publisher publisher) {
        publishers.put(publisher.id(), publisher);
    }

    public void saveAll(List<Publisher> publishers) {
        publishers.forEach(this::save);
    }

}
