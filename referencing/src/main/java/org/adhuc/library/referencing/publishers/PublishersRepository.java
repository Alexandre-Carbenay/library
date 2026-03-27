package org.adhuc.library.referencing.publishers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface PublishersRepository {

    Page<Publisher> findPage(Pageable request);

    Optional<Publisher> findById(UUID id);

    Optional<Publisher> findByName(String name);

    void save(Publisher publisher);
}
