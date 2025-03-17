package org.adhuc.library.catalog.adapter.rest.editions;

import org.adhuc.library.catalog.editions.Edition;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class EditionDetailsModelAssembler extends RepresentationModelAssemblerSupport<Edition, EditionDetailsModel> {
    public EditionDetailsModelAssembler() {
        super(EditionsController.class, EditionDetailsModel.class);
    }

    @NonNull
    @Override
    public EditionDetailsModel toModel(@NonNull Edition edition) {
        var model = instantiateModel(edition);
        model.add(linkTo(methodOn(EditionsController.class).getEdition(edition.isbn())).withSelfRel());
        return model;
    }

    @NonNull
    @Override
    protected EditionDetailsModel instantiateModel(@NonNull Edition edition) {
        return new EditionDetailsModel(edition);
    }
}
