package org.adhuc.library.website.catalog;

import org.adhuc.library.website.support.pagination.NavigablePage;

public interface CatalogClient {

    NavigablePage<Book> listBooks();

    NavigablePage<Book> listBooks(NavigablePage<Book> current, String linkName);

}
