package org.adhuc.library.catalog.books;

import org.adhuc.library.catalog.authors.Author;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record Book(UUID id,
                   String isbn,
                   String title,
                   PublicationDate publicationDate,
                   Set<Author> authors,
                   String language,
                   String summary) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(id, book.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

}
