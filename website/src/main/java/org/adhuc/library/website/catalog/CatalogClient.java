package org.adhuc.library.website.catalog;

import org.adhuc.library.website.support.pagination.NavigablePage;

public interface CatalogClient {

    NavigablePage<Book> listBooks(String acceptLanguages);

    NavigablePage<Book> listBooks(int pageNumber, String acceptLanguages);

    NavigablePage<Book> listBooks(NavigablePage<Book> current, String linkName, String acceptLanguages);

    Book getBook(String id, String acceptLanguages);

}
