package org.adhuc.library.catalog.adapter.autoload;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.adhuc.library.catalog.authors.AuthorsRepository;
import org.adhuc.library.catalog.books.Book;
import org.adhuc.library.catalog.books.ExternalLink;
import org.adhuc.library.catalog.books.LocalizedDetails;
import org.adhuc.library.catalog.books.internal.InMemoryBooksRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static java.util.stream.Collectors.toSet;

public class InMemoryBooksLoader {
    private static final List<String> LANGUAGES = List.of(Locale.getISOLanguages());

    private final Logger logger = LoggerFactory.getLogger(InMemoryEditionsLoader.class);
    private final InMemoryBooksRepository repository;
    private final AuthorsRepository authorsRepository;
    private final String booksResourcePath;

    public InMemoryBooksLoader(InMemoryBooksRepository repository, AuthorsRepository authorsRepository, String booksResourcePath) {
        this.repository = repository;
        this.authorsRepository = authorsRepository;
        this.booksResourcePath = booksResourcePath;
    }

    public void load() {
        var resourceLoader = new DefaultResourceLoader();
        var booksResource = resourceLoader.getResource(booksResourcePath);
        var mapper = new ObjectMapper();
        try {
            var books = mapper.readValue(booksResource.getInputStream(), new TypeReference<List<BookDto>>() {
                    })
                    .stream().map(dto -> dto.convert(authorsRepository))
                    .toList();
            repository.saveAll(books);
            logger.info("Loaded {} books", books.size());
            logger.debug("Books: {}", books);
        } catch (IOException | RuntimeException e) {
            throw new BooksAutoLoadException(booksResourcePath, e);
        }
    }

    public static class BooksAutoLoadException extends RuntimeException {
        BooksAutoLoadException(String booksResourcePath, Throwable cause) {
            super(STR."Unable to load books from \{booksResourcePath}", cause);
        }
    }

    private record BookDto(UUID id, List<UUID> authors, String originalLanguage, List<LocalizedDetailsDto> details) {
        Book convert(AuthorsRepository authorsRepository) {
            Assert.notNull(id, () -> STR."Book ID must be filled");
            Assert.notEmpty(this.authors, () -> STR."Book \{id} authors cannot be empty");
            var authors = this.authors.stream()
                    .map(authorsRepository::findById)
                    .flatMap(Optional::stream)
                    .collect(toSet());
            Assert.isTrue(this.authors.size() == authors.size(), STR."At least one of the authors in \{this.authors} is unknown for book \{id}");
            Assert.notNull(originalLanguage, () -> STR."Book \{id} original language must be filled");
            Assert.isTrue(LANGUAGES.contains(originalLanguage), () -> STR."Book \{id} original language \{originalLanguage} does not correspond to known language");
            Assert.notEmpty(details, () -> STR."Book \{id} localized details cannot be empty");
            var localizedDetails = details.stream()
                    .map(dto -> dto.convert(id))
                    .collect(toSet());
            return new Book(id, authors, originalLanguage, localizedDetails);
        }
    }

    private record LocalizedDetailsDto(String language, String title, String description,
                                       List<ExternalLinkDto> links) {
        LocalizedDetails convert(UUID id) {
            Assert.notNull(language, () -> STR."Book \{id} localized details language must be filled");
            Assert.isTrue(LANGUAGES.contains(language), () -> STR."Book \{id} localized detail language \{language} does not correspond to known language");
            Assert.hasText(title, () -> STR."Book \{id} title for language \{language} must be filled");
            Assert.hasText(description, () -> STR."Book \{id} description for language \{language} must be filled");
            var externalLinks = this.links.stream()
                    .map(dto -> dto.convert(id, language))
                    .collect(toSet());
            return null;
        }
    }

    private record ExternalLinkDto(String source, String value) {
        ExternalLink convert(UUID id, String language) {
            Assert.hasText(source, () -> STR."Book \{id} external link source for language \{language} must be filled");
            Assert.hasText(value, () -> STR."Book \{id} external link value for source \{source} for language \{language} must be filled");
            return null;
        }
    }

}
