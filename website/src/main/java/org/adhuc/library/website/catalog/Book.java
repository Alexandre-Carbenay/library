package org.adhuc.library.website.catalog;

import java.util.List;

public record Book(String id, String title, List<Author> authors, String description) {

}
