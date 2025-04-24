package org.adhuc.library.catalog.adapter.rest.authors;

import org.adhuc.library.catalog.authors.Author;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AuthorDetailsModelAssembler extends RepresentationModelAssemblerSupport<Author, AuthorDetailsModel> {
    public AuthorDetailsModelAssembler() {
        super(AuthorsController.class, AuthorDetailsModel.class);
    }

    @NonNull
    @Override
    public AuthorDetailsModel toModel(@NonNull Author author) {
        var model = instantiateModel(author);
        model.add(linkTo(methodOn(AuthorsController.class).getAuthor(author.id(), null)).withSelfRel());
        return model;
    }

    @NonNull
    @Override
    protected AuthorDetailsModel instantiateModel(@NonNull Author author) {
        return new AuthorDetailsModel(author);
    }
}
