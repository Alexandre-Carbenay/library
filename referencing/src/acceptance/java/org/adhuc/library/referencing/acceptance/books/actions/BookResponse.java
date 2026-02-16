package org.adhuc.library.referencing.acceptance.books.actions;

import org.adhuc.library.referencing.acceptance.authors.Author;
import org.adhuc.library.referencing.acceptance.books.Book;

import java.util.List;

record BookResponse(String id, String originalLanguage, List<String> authors, List<Book.BookDetail> details) {
    Book toBook(List<Author> existingAuthors) {
        var bookAuthors = existingAuthors.stream()
                .filter(author -> this.authors.contains(author.id()))
                .toList();
        return new Book(id, originalLanguage, bookAuthors, details);
    }
}
