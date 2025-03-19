package org.adhuc.library.catalog.adapter.autoload;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.adhuc.library.catalog.books.BooksRepository;
import org.adhuc.library.catalog.editions.Edition;
import org.adhuc.library.catalog.editions.PublicationDate;
import org.adhuc.library.catalog.editions.internal.InMemoryEditionsRepository;
import org.apache.commons.validator.routines.ISBNValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.util.Assert;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@SuppressWarnings("preview")
public class InMemoryEditionsLoader {

    private static final ISBNValidator ISBN_VALIDATOR = new ISBNValidator();
    private static final List<String> LANGUAGES = List.of(Locale.getISOLanguages());

    private final Logger logger = LoggerFactory.getLogger(InMemoryEditionsLoader.class);
    private final InMemoryEditionsRepository repository;
    private final BooksRepository booksRepository;
    private final String editionsResourcePath;

    public InMemoryEditionsLoader(InMemoryEditionsRepository repository, BooksRepository booksRepository, String editionsResourcePath) {
        this.repository = repository;
        this.booksRepository = booksRepository;
        this.editionsResourcePath = editionsResourcePath;
    }

    public void load() {
        var resourceLoader = new DefaultResourceLoader();
        var editionsResource = resourceLoader.getResource(editionsResourcePath);
        var mapper = new ObjectMapper();
        try {
            var editions = mapper.readValue(editionsResource.getInputStream(), new TypeReference<List<EditionDto>>() {
                    })
                    .stream().map(dto -> dto.convert(booksRepository))
                    .toList();
            repository.saveAll(editions);
            logger.info("Loaded {} editions", editions.size());
            logger.debug("Editions: {}", editions);
        } catch (IOException | RuntimeException e) {
            throw new EditionsAutoLoadException(editionsResourcePath, e);
        }
    }

    public static class EditionsAutoLoadException extends RuntimeException {
        EditionsAutoLoadException(String editionsResourcePath, Throwable cause) {
            super(STR."Unable to load editions from \{editionsResourcePath}", cause);
        }
    }

    private record EditionDto(String isbn, String title, String publicationDate, UUID book, String language,
                              String summary) {
        Edition convert(BooksRepository booksRepository) {
            Assert.notNull(ISBN_VALIDATOR.validateISBN13(isbn), STR."ISBN \{isbn} is not valid");
            Assert.hasText(title, () -> STR."Edition \{isbn} title must be filled");
            Assert.notNull(publicationDate, () -> STR."Edition \{isbn} publication date must be filled");
            var publicationDate = PublicationDate.of(LocalDate.parse(this.publicationDate));
            Assert.notNull(this.book, () -> STR."Edition \{isbn} book must be filled");
            var book = booksRepository.findById(this.book);
            Assert.isTrue(book.isPresent(), STR."Book \{book} is unknown for edition \{isbn}");
            Assert.notNull(language, () -> STR."Edition \{isbn} language must be filled");
            Assert.isTrue(LANGUAGES.contains(language), () -> STR."Edition \{isbn} language \{language} does not correspond to known language");
            Assert.hasText(summary, () -> STR."Edition \{isbn} summary must be filled");
            return new Edition(isbn, title, publicationDate, book.get(), language, summary);
        }
    }

}
