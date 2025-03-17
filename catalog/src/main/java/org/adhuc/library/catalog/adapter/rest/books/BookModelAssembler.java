package org.adhuc.library.catalog.adapter.rest.books;

import org.adhuc.library.catalog.books.Book;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

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
        var model = instantiateModel(book);
        model.add(linkTo(methodOn(BooksController.class).getBook(book.id(), null)).withSelfRel());
        return model;
    }

    @NonNull
    @Override
    protected BookModel instantiateModel(@NonNull Book book) {
        return new BookModel(book);
    }
}
