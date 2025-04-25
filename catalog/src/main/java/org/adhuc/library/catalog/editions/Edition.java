package org.adhuc.library.catalog.editions;

import org.adhuc.library.catalog.books.Book;

import java.util.Objects;
import java.util.Optional;

public class Edition {

    private final String isbn;
    private final String title;
    private final PublicationDate publicationDate;
    private final Book book;
    private final Publisher publisher;
    private final String language;
    private final String summary;

    public Edition(String isbn, String title, PublicationDate publicationDate, Book book, Publisher publisher, String language, String summary) {
        this.isbn = isbn;
        this.title = title;
        this.publicationDate = publicationDate;
        this.book = book;
        this.publisher = publisher;
        this.language = language;
        this.summary = summary;
    }

    public String isbn() {
        return this.isbn;
    }

    public String title() {
        return this.title;
    }

    public PublicationDate publicationDate() {
        return this.publicationDate;
    }

    public Book book() {
        return this.book;
    }

    public Optional<Publisher> publisher() {
        return Optional.ofNullable(publisher);
    }

    public String language() {
        return this.language;
    }

    public String summary() {
        return this.summary;
    }

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
