package org.adhuc.library.referencing.authors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuthorsRepository {

    Page<Author> findPage(Pageable request);

    void save(Author author);

}
