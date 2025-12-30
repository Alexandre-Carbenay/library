package org.adhuc.library.catalog.adapter.rest.books;

import org.adhuc.library.catalog.books.Book;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class BookModelAssembler extends RepresentationModelAssemblerSupport<Book, BookModel> {
    public BookModelAssembler() {
        super(BooksController.class, BookModel.class);
    }

    @Override
    public BookModel toModel(Book book) {
        return toModel(book, book.originalLanguage());
    }

    public BookModel toModel(Book book, String language) {
        var model = instantiateModel(book, language);
        model.add(linkTo(methodOn(BooksController.class).getBook(book.id(), null)).withSelfRel());
        book.wikipediaLinkIn(language).ifPresent(link -> model.add(Link.of(link.value()).withRel(link.source())));
        return model;
    }

    @Override
    protected BookModel instantiateModel(Book book) {
        return new BookModel(book);
    }

    protected BookModel instantiateModel(Book book, String language) {
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
