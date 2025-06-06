package org.adhuc.library.catalog.adapter.rest.editions;

import org.adhuc.library.catalog.adapter.rest.books.BooksController;
import org.adhuc.library.catalog.editions.Edition;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class EditionModelAssembler extends RepresentationModelAssemblerSupport<Edition, EditionModel> {
    public EditionModelAssembler() {
        super(EditionsController.class, EditionModel.class);
    }

    @NonNull
    @Override
    public EditionModel toModel(@NonNull Edition edition) {
        var model = instantiateModel(edition);
        model.add(linkTo(methodOn(EditionsController.class).getEdition(edition.isbn())).withSelfRel());
        model.add(linkTo(methodOn(BooksController.class).getBook(edition.book().id(), null)).withRel("book"));
        return model;
    }

    @NonNull
    @Override
    protected EditionModel instantiateModel(@NonNull Edition edition) {
        return new EditionModel(edition);
    }
}
