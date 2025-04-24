package org.adhuc.library.catalog.books;

import org.adhuc.library.catalog.authors.Author;

import java.util.*;

public record Book(UUID id,
                   Set<Author> authors,
                   String originalLanguage,
                   Set<LocalizedDetails> details) {

    public boolean acceptsLanguage(Locale language) {
        return details.stream()
                .anyMatch(detail -> detail.language().equals(language.getLanguage()));
    }

    public boolean acceptsLanguage(String language) {
        return acceptsLanguage(Locale.of(language));
    }

    public String titleIn(String language) {
        return detailsIn(language).title();
    }

    public String descriptionIn(String language) {
        return detailsIn(language).description();
    }

    public Optional<ExternalLink> wikipediaLinkIn(String language) {
        return detailsIn(language).links()
                .stream()
                .filter(ExternalLink::isWikipediaLink)
                .findFirst();
    }

    private LocalizedDetails detailsIn(String language) {
        return details.stream()
                .filter(detail -> detail.language().equals(language))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(STR."No detail for language \{language} in book \{id}"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(id, book.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
