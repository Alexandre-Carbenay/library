package org.adhuc.library.referencing.adapter.rest.publishers;

import org.adhuc.library.referencing.publishers.Publisher;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
class PublisherModelAssembler extends RepresentationModelAssemblerSupport<Publisher, PublisherModel> {
    public PublisherModelAssembler() {
        super(PublishersController.class, PublisherModel.class);
    }

    @Override
    public PublisherModel toModel(Publisher publisher) {
        var model = instantiateModel(publisher);
        model.add(linkTo(methodOn(PublishersController.class).getPublisher(model.id())).withSelfRel());
        return model;
    }

    @Override
    protected PublisherModel instantiateModel(Publisher publisher) {
        return new PublisherModel(publisher);
    }
}
