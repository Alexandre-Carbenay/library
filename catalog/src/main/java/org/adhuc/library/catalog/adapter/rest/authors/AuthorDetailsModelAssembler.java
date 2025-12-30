package org.adhuc.library.catalog.adapter.rest.authors;

import org.adhuc.library.catalog.authors.Author;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AuthorDetailsModelAssembler extends RepresentationModelAssemblerSupport<Author, AuthorDetailsModel> {
    public AuthorDetailsModelAssembler() {
        super(AuthorsController.class, AuthorDetailsModel.class);
    }

    @Override
    public AuthorDetailsModel toModel(Author author) {
        var model = instantiateModel(author);
        model.add(linkTo(methodOn(AuthorsController.class).getAuthor(author.id(), null)).withSelfRel());
        return model;
    }

    @Override
    protected AuthorDetailsModel instantiateModel(Author author) {
        return new AuthorDetailsModel(author);
    }
}
