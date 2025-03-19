package org.adhuc.library.catalog.books;

public record ExternalLink(String source, String value) {
    public static final String WIKIPEDIA_LINK = "wikipedia";

    public boolean isWikipediaLink() {
        return WIKIPEDIA_LINK.equals(source);
    }
}
