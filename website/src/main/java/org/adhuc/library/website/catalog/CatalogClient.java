package org.adhuc.library.website.catalog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CatalogClient {

    Page<Book> listBooks();

    Page<Book> listBooks(Pageable pageable);

}
