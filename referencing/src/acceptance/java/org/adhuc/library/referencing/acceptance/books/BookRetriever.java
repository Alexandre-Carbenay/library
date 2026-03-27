package org.adhuc.library.referencing.acceptance.books;

import org.adhuc.library.referencing.acceptance.authors.Author;

import java.util.List;
import java.util.Optional;

import static org.adhuc.library.referencing.acceptance.books.actions.BooksListing.listBooks;

public class BookRetriever {

    public static Optional<Book> findBookById(String bookId) {
        var books = listBooks();
        return books.stream().filter(book -> bookId.equals(book.id())).findFirst();
    }

    public static List<Book> findBooksByTitle(String bookTitle) {
        var books = listBooks();
        return books.stream().filter(book -> book.hasTitle(bookTitle)).toList();
    }

    public static List<Book> findBooksByTitleAndAuthor(String bookTitle, Author author) {
        var books = listBooks();
        return books.stream().filter(book -> book.hasTitle(bookTitle) && book.hasAuthor(author)).toList();
    }

}
