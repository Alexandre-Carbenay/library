package org.adhuc.library.website.catalog.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.adhuc.library.website.catalog.Book;
import org.adhuc.library.website.support.pagination.NavigablePageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@JsonDeserialize
public class BooksPage extends NavigablePageImpl<Book> {

    public BooksPage(@JsonProperty("page") PageAttributes page,
                     @JsonProperty("_links") Links links,
                     @JsonProperty("_embedded") EmbeddedValues embedded) {
        super(embedded.convertedBooks(), PageRequest.of(page.number(), page.size()), page.totalElements(), links.converted());
    }

    public record PageAttributes(int size,
                                 @JsonProperty("total_elements") int totalElements,
                                 @JsonProperty("total_pages") int totalPages,
                                 int number) {
    }

    public record Links(LinkValue first, @JsonProperty("prev") LinkValue previous, LinkValue next, LinkValue last) {
        List<Link> converted() {
            return Stream.of(
                            converted("first", first),
                            converted("prev", previous),
                            converted("next", next),
                            converted("last", last)
                    )
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();
        }

        private Optional<Link> converted(String name, LinkValue linkValue) {
            return Optional.ofNullable(linkValue)
                    .map(LinkValue::href)
                    .map(href -> new Link(name, href));
        }
    }

    public record LinkValue(String href) {
    }

    public record EmbeddedValues(List<BookDto> books, List<AuthorDto> authors) {
        List<Book> convertedBooks() {
            return books.stream()
                    .map(book -> book.withAuthorsFrom(authors))
                    .toList();
        }
    }

}
