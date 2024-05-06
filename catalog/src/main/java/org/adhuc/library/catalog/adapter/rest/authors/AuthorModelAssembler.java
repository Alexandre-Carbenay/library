package org.adhuc.library.catalog.adapter.rest.authors;

import org.adhuc.library.catalog.books.Author;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AuthorModelAssembler extends RepresentationModelAssemblerSupport<Author, AuthorModel> {
    public AuthorModelAssembler() {
        super(AuthorsController.class, AuthorModel.class);
    }

    @NonNull
    @Override
    public AuthorModel toModel(@NonNull Author author) {
        var model = instantiateModel(author);
        model.add(linkTo(methodOn(AuthorsController.class).getAuthor(author.id())).withSelfRel());
        return model;
    }

    @NonNull
    @Override
    protected AuthorModel instantiateModel(@NonNull Author author) {
        return new AuthorModel(author);
    }
}
