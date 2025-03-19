package org.adhuc.library.catalog.books;

import java.util.Set;

public record LocalizedDetails(String language,
                               String title,
                               String description,
                               Set<ExternalLink> links) {
}
