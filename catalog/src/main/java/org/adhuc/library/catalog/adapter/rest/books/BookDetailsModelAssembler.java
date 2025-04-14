package org.adhuc.library.catalog.adapter.rest.books;

import org.adhuc.library.catalog.books.Book;
import org.springframework.hateoas.Link;
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
        return toModel(book, book.originalLanguage());
    }

    @NonNull
    public BookDetailsModel toModel(@NonNull Book book, @NonNull String language) {
        var model = instantiateModel(book, language);
        model.add(linkTo(methodOn(BooksController.class).getBook(book.id(), null)).withSelfRel());
        book.wikipediaLinkIn(language).ifPresent(link -> model.add(Link.of(link.value()).withRel(link.source())));
        return model;
    }

    @NonNull
    @Override
    protected BookDetailsModel instantiateModel(@NonNull Book book) {
        return new BookDetailsModel(book);
    }

    @NonNull
    protected BookDetailsModel instantiateModel(@NonNull Book book, @NonNull String language) {
        return new BookDetailsModel(book, language);
    }
}
