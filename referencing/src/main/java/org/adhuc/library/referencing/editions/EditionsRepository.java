package org.adhuc.library.referencing.editions;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface EditionsRepository {

    Page<Edition> findPage(Pageable request);

    Optional<Edition> findByIsbn(String isbn);

    void save(Edition edition);

}
