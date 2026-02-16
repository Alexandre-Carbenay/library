package org.adhuc.library.referencing.books;

import java.util.Set;
import java.util.UUID;

public record Book(UUID id, Set<UUID> authors, String originalLanguage, Set<LocalizedDetail> details) {
    public record LocalizedDetail(String language, String title, String description) {
    }
}
