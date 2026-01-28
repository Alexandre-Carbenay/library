package org.adhuc.library.referencing.acceptance.books;

import java.util.Optional;

import static org.adhuc.library.referencing.acceptance.books.actions.BooksListing.listBooks;

public class BookRetriever {

    public static Optional<Book> findBookByTitle(String bookTitle) {
        var books = listBooks();
        return books != null
                ? books.stream().filter(author -> author.hasTitle(bookTitle)).findFirst()
                : Optional.empty();
    }

}
