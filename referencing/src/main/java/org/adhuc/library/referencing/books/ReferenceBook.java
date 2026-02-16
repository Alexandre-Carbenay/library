package org.adhuc.library.referencing.books;

import java.util.List;
import java.util.UUID;

public record ReferenceBook(List<UUID> authors, String originalLanguage, List<Book.LocalizedDetail> details) {
}
