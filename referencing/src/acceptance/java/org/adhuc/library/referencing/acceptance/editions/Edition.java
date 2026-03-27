package org.adhuc.library.referencing.acceptance.editions;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Edition(String isbn, @JsonProperty("book") String bookId, String title, String summary) {
    public boolean hasIsbn(String isbn) {
        return this.isbn.equals(isbn);
    }
}
