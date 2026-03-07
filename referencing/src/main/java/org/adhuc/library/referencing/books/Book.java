package org.adhuc.library.referencing.books;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.toSet;

public class Book {
    private final UUID id;
    private final Set<UUID> authors;
    private final String originalLanguage;
    private final Set<LocalizedDetail> details;

    Book(UUID id, Set<UUID> authors, String originalLanguage, Set<LocalizedDetail> details) {
        this.id = id;
        this.authors = Set.copyOf(authors);
        this.originalLanguage = originalLanguage;
        this.details = Set.copyOf(details);
    }

    public UUID id() {
        return id;
    }

    public boolean hasSimilarAuthors(Collection<UUID> authors) {
        return this.authors.containsAll(authors) || authors.containsAll(this.authors);
    }

    public Set<UUID> authors() {
        return Set.copyOf(authors);
    }

    public String originalLanguage() {
        return originalLanguage;
    }

    public Set<LocalizedDetail> details() {
        return Set.copyOf(details);
    }

    public Optional<LocalizedDetail> detailsIn(String language) {
        return details.stream().filter(detail -> detail.language.equals(language)).findFirst();
    }

    public boolean hasSimilarTitles(Collection<String> titles) {
        return details.stream().anyMatch(detail -> titles.contains(detail.title()));
    }

    public Set<String> titles() {
        return details.stream().map(LocalizedDetail::title).collect(toSet());
    }

    public record LocalizedDetail(String language, String title, String description) {
        public LocalizedDetail(String language, String title, String description) {
            this.language = language;
            this.title = title.trim();
            this.description = description.trim();
            if (this.language.isEmpty()) {
                throw new IllegalArgumentException("A book detail cannot have empty language");
            }
            if (this.title.isEmpty()) {
                throw new IllegalArgumentException("A book detail cannot have empty title");
            }
            if (this.description.isEmpty()) {
                throw new IllegalArgumentException("A book detail cannot have empty description");
            }
        }
    }
}
