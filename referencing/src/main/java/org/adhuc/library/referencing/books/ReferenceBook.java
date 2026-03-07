package org.adhuc.library.referencing.books;

import java.util.*;
import java.util.stream.IntStream;

public record ReferenceBook(Set<UUID> authors, String originalLanguage, List<Book.LocalizedDetail> details) {

    public ReferenceBook(Set<UUID> authors, String originalLanguage, List<Book.LocalizedDetail> details) {
        this.authors = Set.copyOf(authors);
        this.originalLanguage = originalLanguage.trim();
        this.details = List.copyOf(details);
        if (this.authors.isEmpty()) {
            throw new IllegalArgumentException("A book cannot have no author");
        }
        if (this.originalLanguage.isEmpty()) {
            throw new IllegalArgumentException("A book cannot have empty original language");
        }
        if (this.details.isEmpty()) {
            throw new IllegalArgumentException("A book cannot have no details");
        }
        assertUniqueLanguagesInDetails();
    }

    public Collection<String> titles() {
        return details.stream().map(Book.LocalizedDetail::title).toList();
    }

    Optional<DuplicateBookException> prepareDuplication(Book book) {
        var bookTitles = book.titles();
        return IntStream.range(0, details.size())
                .filter(index -> bookTitles.contains(details.get(index).title()))
                .mapToObj(index -> new DuplicateBookException(index, details.get(index).title()))
                .findFirst();
    }

    private void assertUniqueLanguagesInDetails() {
        var numberOfLanguages = details.stream().map(Book.LocalizedDetail::language).distinct().count();
        if (numberOfLanguages != details.size()) {
            throw new IllegalArgumentException("A book cannot have details with duplicated languages");
        }
    }

    Book buildBook() {
        return new Book(UUID.randomUUID(), authors, originalLanguage, Set.copyOf(details));
    }

}
