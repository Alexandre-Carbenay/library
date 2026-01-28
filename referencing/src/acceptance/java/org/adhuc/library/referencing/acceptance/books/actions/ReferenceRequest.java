package org.adhuc.library.referencing.acceptance.books.actions;

import org.adhuc.library.referencing.acceptance.authors.Author;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public record ReferenceRequest(@Nullable List<String> authors,
                               @Nullable String originalLanguage,
                               @Nullable List<ReferenceDetail> details) {

    public ReferenceRequest(@Nullable List<Author> authors,
                            @Nullable String originalLanguage,
                            @Nullable String detailLanguage,
                            @Nullable String title,
                            @Nullable String description) {
        this(
                authors != null ? authors.stream().map(author -> Optional.ofNullable(author.id()).orElseThrow()).toList() : null,
                originalLanguage,
                List.of(new ReferenceDetail(detailLanguage, title, description))
        );
    }

    public record ReferenceDetail(@Nullable String language, @Nullable String title, @Nullable String description) {
    }

}
