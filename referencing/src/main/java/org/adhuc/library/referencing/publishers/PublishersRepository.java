package org.adhuc.library.referencing.publishers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PublishersRepository {

    Page<Publisher> findPage(Pageable request);

    Optional<Publisher> findByName(String name);

    void save(Publisher publisher);

}
