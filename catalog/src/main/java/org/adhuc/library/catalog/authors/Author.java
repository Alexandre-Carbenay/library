package org.adhuc.library.catalog.authors;

import org.adhuc.library.catalog.books.Book;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record Author(UUID id, String name, LocalDate dateOfBirth, LocalDate dateOfDeath, List<Book> notableBooks) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Author author = (Author) o;
        return Objects.equals(id, author.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

}
