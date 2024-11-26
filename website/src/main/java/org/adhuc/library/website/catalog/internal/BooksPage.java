package org.adhuc.library.website.catalog.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.adhuc.library.website.catalog.Book;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@JsonDeserialize
public class BooksPage extends PageImpl<Book> {

    public BooksPage(@JsonProperty("page") PageAttributes page, @JsonProperty("_embedded") EmbeddedValues embedded) {
        super(embedded.convertedBooks(), PageRequest.of(page.number(), page.size()), page.totalElements());
    }

    public record PageAttributes(int size,
                                 @JsonProperty("total_elements") int totalElements,
                                 @JsonProperty("total_pages") int totalPages,
                                 int number) {
    }

    public record EmbeddedValues(List<BookDto> books, List<AuthorDto> authors) {
        List<Book> convertedBooks() {
            return books.stream()
                    .map(book -> book.withAuthorsFrom(authors))
                    .toList();
        }
    }

}
