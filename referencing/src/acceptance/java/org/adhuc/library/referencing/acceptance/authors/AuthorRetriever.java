package org.adhuc.library.referencing.acceptance.authors;

import java.util.Optional;

import static org.adhuc.library.referencing.acceptance.authors.actions.AuthorsListing.listAuthors;

public class AuthorRetriever {

    public static Optional<Author> findAuthorByName(String authorName) {
        var authors = listAuthors();
        return authors != null
                ? authors.stream().filter(author -> author.hasName(authorName)).findFirst()
                : Optional.empty();
    }

}
