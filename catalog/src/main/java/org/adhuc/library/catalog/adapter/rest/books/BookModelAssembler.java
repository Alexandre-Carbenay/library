package org.adhuc.library.catalog.adapter.rest.books;

import org.adhuc.library.catalog.books.Book;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class BookModelAssembler extends RepresentationModelAssemblerSupport<Book, BookModel> {
    public BookModelAssembler() {
        super(BooksController.class, BookModel.class);
    }

    @NonNull
    @Override
    public BookModel toModel(@NonNull Book book) {
        return toModel(book, book.originalLanguage());
    }

    @NonNull
    public BookModel toModel(@NonNull Book book, @NonNull String language) {
        var model = instantiateModel(book, language);
        model.add(linkTo(methodOn(BooksController.class).getBook(book.id(), null)).withSelfRel());
        book.wikipediaLinkIn(language).ifPresent(link -> model.add(Link.of(link.value()).withRel(link.source())));
        return model;
    }

    @NonNull
    @Override
    protected BookModel instantiateModel(@NonNull Book book) {
        return new BookModel(book);
    }

    @NonNull
    protected BookModel instantiateModel(@NonNull Book book, @NonNull String language) {
        return new BookModel(book, language);
    }

    public CollectionModel<BookModel> toCollectionModel(Iterable<Book> books, String language) {
        var resources = new ArrayList<BookModel>();
        for (var book : books) {
            if (book.acceptsLanguage(language)) {
                resources.add(toModel(book, language));
            }
        }
        return CollectionModel.of(resources);
    }
}
