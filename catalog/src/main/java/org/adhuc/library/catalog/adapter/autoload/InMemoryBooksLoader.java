package org.adhuc.library.catalog.adapter.autoload;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.adhuc.library.catalog.authors.AuthorsRepository;
import org.adhuc.library.catalog.authors.internal.InMemoryAuthorsRepository;
import org.adhuc.library.catalog.books.Book;
import org.adhuc.library.catalog.books.PublicationDate;
import org.adhuc.library.catalog.books.internal.InMemoryBooksRepository;
import org.apache.commons.validator.routines.ISBNValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.util.Assert;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static java.util.stream.Collectors.toSet;

@SuppressWarnings("preview")
public class InMemoryBooksLoader {

    private final Logger logger = LoggerFactory.getLogger(InMemoryBooksLoader.class);
    private static final ISBNValidator ISBN_VALIDATOR = new ISBNValidator();
    private static final List<String> LANGUAGES = List.of(Locale.getISOLanguages());

    private final InMemoryBooksRepository repository;
    private final InMemoryAuthorsRepository authorsRepository;
    private final String booksResourcePath;

    public InMemoryBooksLoader(InMemoryBooksRepository repository, InMemoryAuthorsRepository authorsRepository, String booksResourcePath) {
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

    private record BookDto(String isbn, String title, String publicationDate, List<UUID> authors, String language,
                           String summary) {
        Book convert(AuthorsRepository authorsRepository) {
            Assert.notNull(ISBN_VALIDATOR.validateISBN13(isbn), STR."ISBN \{isbn} is not valid");
            Assert.hasText(title, () -> STR."Book \{isbn} title must be filled");
            Assert.notNull(publicationDate, () -> STR."Book \{isbn} publication date must be filled");
            var publicationDate = PublicationDate.of(LocalDate.parse(this.publicationDate));
            Assert.notEmpty(this.authors, () -> STR."Book \{isbn} authors cannot be empty");
            var authors = this.authors.stream()
                    .map(authorsRepository::findById)
                    .flatMap(Optional::stream)
                    .collect(toSet());
            Assert.isTrue(this.authors.size() == authors.size(), STR."At least one of the authors in \{this.authors} is unknown for book \{isbn}");
            Assert.notNull(language, () -> STR."Book \{isbn} language must be filled");
            Assert.isTrue(LANGUAGES.contains(language), () -> STR."Book \{isbn} language \{language} does not correspond to known language");
            Assert.hasText(summary, () -> STR."Book \{isbn} summary must be filled");
            return new Book(isbn, title, publicationDate, authors, language, summary);
        }
    }

}
