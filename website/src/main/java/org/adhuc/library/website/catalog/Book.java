package org.adhuc.library.website.catalog;

import java.util.List;

public record Book(String id, String title, List<Author> authors, String description, List<Edition> editions) {

    public Book(String id, String title, List<Author> authors, String description) {
        this(id, title, authors, description, null);
    }

}
