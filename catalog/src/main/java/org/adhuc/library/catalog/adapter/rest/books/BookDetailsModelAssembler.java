package org.adhuc.library.catalog.adapter.rest.books;

import org.adhuc.library.catalog.books.Book;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class BookDetailsModelAssembler extends RepresentationModelAssemblerSupport<Book, BookDetailsModel> {
    public BookDetailsModelAssembler() {
        super(BooksController.class, BookDetailsModel.class);
    }

    @NonNull
    @Override
    public BookDetailsModel toModel(@NonNull Book book) {
        var model = instantiateModel(book);
        model.add(linkTo(methodOn(BooksController.class).getBook(book.isbn())).withSelfRel());
        return model;
    }

    @NonNull
    @Override
    protected BookDetailsModel instantiateModel(@NonNull Book book) {
        return new BookDetailsModel(book);
    }
}
