package org.adhuc.library.referencing.editions;

import org.adhuc.library.referencing.books.Book;
import org.apache.commons.validator.routines.ISBNValidator;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public class ReferenceEdition {
    private static final ISBNValidator ISBN_VALIDATOR = new ISBNValidator();

    private final String isbn;
    private final String language;
    private final UUID book;
    private final UUID publisher;
    private final LocalDate publicationDate;
    @Nullable
    private final String title;
    @Nullable
    private final String summary;

    public ReferenceEdition(String isbn, String language, UUID book, UUID publisher, LocalDate publicationDate) {
        this(isbn, language, book, publisher, publicationDate, null, null);
    }

    public ReferenceEdition(String isbn, String language, UUID book, UUID publisher, LocalDate publicationDate, @Nullable String title, @Nullable String summary) {
        this.isbn = isbn.trim();
        this.language = language.trim();
        this.book = book;
        this.publisher = publisher;
        this.publicationDate = publicationDate;
        this.title = title;
        this.summary = summary;
        if (!ISBN_VALIDATOR.isValidISBN13(this.isbn)) {
            throw new IllegalArgumentException("An edition cannot have invalid ISBN");
        }
        if (this.language.isEmpty()) {
            throw new IllegalArgumentException("An edition cannot have empty language");
        }
    }

    public String isbn() {
        return isbn;
    }

    public String language() {
        return language;
    }

    public UUID book() {
        return book;
    }

    public UUID publisher() {
        return publisher;
    }

    public LocalDate publicationDate() {
        return publicationDate;
    }

    public Optional<String> title() {
        return Optional.ofNullable(title);
    }

    public Optional<String> summary() {
        return Optional.ofNullable(summary);
    }

    public Edition buildEdition(Book book) {
        return new Edition(isbn, language, book.id(), publisher, publicationDate, editionTitle(book), editionSummary(book));
    }

    private String editionTitle(Book book) {
        return title().orElseGet(() -> book.titleIn(language)
                .orElseGet(() -> book.titleIn(book.originalLanguage())
                        .orElseThrow(() -> new IllegalStateException("Cannot deduce edition title from book " + book))));
    }

    private String editionSummary(Book book) {
        return summary().orElseGet(() -> book.descriptionIn(language)
                .orElseGet(() -> book.descriptionIn(book.originalLanguage())
                        .orElseThrow(() -> new IllegalStateException("Cannot deduce edition summary from book " + book))));
    }

}
