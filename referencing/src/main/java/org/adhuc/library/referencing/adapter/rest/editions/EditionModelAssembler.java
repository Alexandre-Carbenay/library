package org.adhuc.library.referencing.adapter.rest.editions;

import org.adhuc.library.referencing.editions.Edition;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
class EditionModelAssembler extends RepresentationModelAssemblerSupport<Edition, EditionModel> {
    public EditionModelAssembler() {
        super(EditionsController.class, EditionModel.class);
    }

    @Override
    public EditionModel toModel(Edition edition) {
        var model = instantiateModel(edition);
        model.add(linkTo(methodOn(EditionsController.class).getEdition(model.isbn())).withSelfRel());
        return model;
    }

    @Override
    protected EditionModel instantiateModel(Edition edition) {
        return new EditionModel(edition);
    }
}
