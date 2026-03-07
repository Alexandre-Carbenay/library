package org.adhuc.library.referencing.acceptance.books;

import org.adhuc.library.referencing.acceptance.authors.Author;

import java.util.List;

import static org.adhuc.library.referencing.acceptance.books.actions.BooksListing.listBooks;

public class BookRetriever {

    public static List<Book> findBooksByTitle(String bookTitle) {
        var books = listBooks();
        return books.stream().filter(book -> book.hasTitle(bookTitle)).toList();
    }

    public static List<Book> findBooksByTitleAndAuthor(String bookTitle, Author author) {
        var books = listBooks();
        return books.stream().filter(book -> book.hasTitle(bookTitle) && book.hasAuthor(author)).toList();
    }

}
