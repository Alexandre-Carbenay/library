package org.adhuc.library.website.catalog;

import org.adhuc.library.website.support.pagination.NavigablePage;
import org.springframework.data.domain.Pageable;

public interface CatalogClient {

    NavigablePage<Book> listBooks();

    NavigablePage<Book> listBooks(NavigablePage<Book> current, String linkName);

}
