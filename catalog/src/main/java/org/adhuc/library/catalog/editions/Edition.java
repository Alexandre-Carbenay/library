package org.adhuc.library.catalog.editions;

import org.adhuc.library.catalog.books.Book;

import java.util.Objects;
import java.util.Set;

public record Edition(String isbn,
                      String title,
                      PublicationDate publicationDate,
                      Book book,
                      String language,
                      String summary) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edition edition = (Edition) o;
        return Objects.equals(isbn, edition.isbn);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(isbn);
    }

}
