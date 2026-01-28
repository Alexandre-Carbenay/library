package org.adhuc.library.referencing.acceptance.books.actions;

import org.adhuc.library.referencing.acceptance.authors.Author;

import java.util.List;

record ReferenceRequest(List<String> authors, String originalLanguage, List<ReferenceDetail> details) {

    public ReferenceRequest(List<Author> authors, String originalLanguage, String title, String description) {
        this(authors.stream().map(Author::id).toList(), originalLanguage, List.of(new ReferenceDetail(originalLanguage, title, description)));
    }

    record ReferenceDetail(String language, String title, String description) {
    }

}
