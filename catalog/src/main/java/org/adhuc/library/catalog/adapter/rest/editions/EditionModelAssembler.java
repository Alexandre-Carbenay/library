package org.adhuc.library.catalog.adapter.rest.editions;

import org.adhuc.library.catalog.books.Book;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class EditionModelAssembler extends RepresentationModelAssemblerSupport<Book, EditionModel> {
    public EditionModelAssembler() {
        super(EditionsController.class, EditionModel.class);
    }

    @NonNull
    @Override
    public EditionModel toModel(@NonNull Book book) {
        var model = instantiateModel(book);
        model.add(linkTo(methodOn(EditionsController.class).getBook(book.isbn())).withSelfRel());
        return model;
    }

    @NonNull
    @Override
    protected EditionModel instantiateModel(@NonNull Book book) {
        return new EditionModel(book);
    }
}
