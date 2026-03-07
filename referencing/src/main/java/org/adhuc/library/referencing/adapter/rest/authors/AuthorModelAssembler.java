package org.adhuc.library.referencing.adapter.rest.authors;

import org.adhuc.library.referencing.authors.Author;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AuthorModelAssembler extends RepresentationModelAssemblerSupport<Author, AuthorModel> {
    public AuthorModelAssembler() {
        super(AuthorsController.class, AuthorModel.class);
    }

    @Override
    public AuthorModel toModel(Author author) {
        var model = instantiateModel(author);
        model.add(linkTo(methodOn(AuthorsController.class).getAuthor(author.id())).withSelfRel());
        return model;
    }

    @Override
    protected AuthorModel instantiateModel(Author author) {
        return new AuthorModel(author);
    }
}
