package org.adhuc.library.catalog.adapter.autoload;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.adhuc.library.catalog.authors.Author;
import org.adhuc.library.catalog.authors.internal.InMemoryAuthorsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.util.Assert;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("preview")
public class InMemoryAuthorsLoader {

    private final Logger logger = LoggerFactory.getLogger(InMemoryAuthorsLoader.class);
    private final InMemoryAuthorsRepository repository;
    private final String authorsResourcePath;

    public InMemoryAuthorsLoader(InMemoryAuthorsRepository repository, String authorsResourcePath) {
        this.repository = repository;
        this.authorsResourcePath = authorsResourcePath;
    }

    public void load() {
        var resourceLoader = new DefaultResourceLoader();
        var authorsResource = resourceLoader.getResource(authorsResourcePath);
        var mapper = new ObjectMapper();
        try {
            var authors = mapper.readValue(authorsResource.getInputStream(), new TypeReference<List<AuthorDto>>() {
                    })
                    .stream().map(AuthorDto::convert)
                    .toList();
            repository.saveAll(authors);
            logger.info("Loaded {} authors", authors.size());
            logger.debug("Authors: {}", authors);
        } catch (IOException | RuntimeException e) {
            throw new AuthorsAutoLoadException(authorsResourcePath, e);
        }
    }

    public static class AuthorsAutoLoadException extends RuntimeException {
        AuthorsAutoLoadException(String authorsResourcePath, Throwable cause) {
            super(STR."Unable to load authors from \{authorsResourcePath}", cause);
        }
    }

    private record AuthorDto(UUID id, String name, String dateOfBirth, String dateOfDeath) {
        Author convert() {
            Assert.notNull(id, "Author ID cannot be null");
            Assert.hasText(name, () -> STR."Author \{id} name cannot be null or empty");
            Assert.notNull(this.dateOfBirth, () -> STR."Author \{id} date of birth cannot be null");
            var dateOfBirth = LocalDate.parse(this.dateOfBirth);
            var dateOfDeath = this.dateOfDeath != null ? LocalDate.parse(this.dateOfDeath) : null;
            if (dateOfDeath != null && dateOfDeath.isBefore(dateOfBirth)) {
                throw new IllegalArgumentException(STR."Author \{id} cannot be dead before being born");
            }
            return new Author(id, name, dateOfBirth, dateOfDeath);
        }
    }

}
