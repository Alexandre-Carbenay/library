package org.adhuc.library.referencing.adapter.rest.books;

import org.adhuc.library.referencing.books.Book;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class BookModelAssembler extends RepresentationModelAssemblerSupport<Book, BookModel> {
    public BookModelAssembler() {
        super(BooksController.class, BookModel.class);
    }

    @Override
    public BookModel toModel(Book book) {
        var model = instantiateModel(book);
        model.add(linkTo(methodOn(BooksController.class).getBook(model.id())).withSelfRel());
        return model;
    }

    @Override
    protected BookModel instantiateModel(Book book) {
        return new BookModel(book);
    }
}
