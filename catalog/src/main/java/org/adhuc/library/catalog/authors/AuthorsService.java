package org.adhuc.library.catalog.authors;

import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthorsService {

    public Optional<Author> getAuthor(UUID id) {
        return Optional.empty();
    }

}
