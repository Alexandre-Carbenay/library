package org.adhuc.library.catalog.adapter.autoload;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.adhuc.library.catalog.adapter.autoload.InMemoryBooksLoader.BooksAutoLoadException;
import org.adhuc.library.catalog.authors.internal.InMemoryAuthorsRepository;
import org.adhuc.library.catalog.books.Book;
import org.adhuc.library.catalog.books.internal.InMemoryBooksRepository;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.FileNotFoundException;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Stream;

import static org.adhuc.library.catalog.authors.AuthorsMother.Real.*;
import static org.adhuc.library.catalog.books.BooksMother.Real.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Books auto-load from JSON file should")
class BooksAutoLoadTests {

    private InMemoryBooksRepository repository;
    private InMemoryAuthorsRepository authorsRepository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryBooksRepository();
        authorsRepository = new InMemoryAuthorsRepository();
        authorsRepository.saveAll(List.of(
                ALBERT_CAMUS,
                GUSTAVE_FLAUBERT,
                LEON_TOLSTOI,
                VIRGINIE_DESPENTES
        ));
    }

    @Test
    @DisplayName("fail if the provided JSON resource does not exist")
    void failNonExistingResource() {
        var booksLoader = new InMemoryBooksLoader(repository, authorsRepository, "classpath:unknown.json");
        var exception = assertThrows(BooksAutoLoadException.class, booksLoader::load);
        assertThat(exception).hasCauseInstanceOf(FileNotFoundException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "classpath:auto-load/books/books-test-not-json.json",
            "classpath:auto-load/books/books-test-not-json.yml",
            "classpath:auto-load/books/books-test-malformed-json.json"
    })
    @DisplayName("fail if the content does not correspond to valid JSON")
    void failNotJson(String resourcePath) {
        var booksLoader = new InMemoryBooksLoader(repository, authorsRepository, resourcePath);
        var exception = assertThrows(BooksAutoLoadException.class, booksLoader::load);
        assertThat(exception).hasCauseInstanceOf(JsonParseException.class);
    }

    @Test
    @DisplayName("fail if the JSON root element is not an array")
    void failNotArray() {
        var booksLoader = new InMemoryBooksLoader(repository, authorsRepository, "classpath:auto-load/books/books-test-root-not-array.json");
        var exception = assertThrows(BooksAutoLoadException.class, booksLoader::load);
        assertThat(exception).hasCauseInstanceOf(MismatchedInputException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "classpath:auto-load/books/books-test-no-id.json",
            "classpath:auto-load/books/books-test-id-not-valid.json",
            "classpath:auto-load/books/books-test-no-authors.json",
            "classpath:auto-load/books/books-test-empty-authors.json",
            "classpath:auto-load/books/books-test-invalid-author.json",
            "classpath:auto-load/books/books-test-unknown-author.json",
            "classpath:auto-load/books/books-test-unknown-author-of-many.json",
            "classpath:auto-load/books/books-test-empty-localized-details.json",
            "classpath:auto-load/books/books-test-no-original-language.json",
            "classpath:auto-load/books/books-test-unknown-original-language.json",
            "classpath:auto-load/books/books-test-no-language.json",
            "classpath:auto-load/books/books-test-unknown-language.json",
            "classpath:auto-load/books/books-test-no-title.json",
            "classpath:auto-load/books/books-test-no-description.json",
            "classpath:auto-load/books/books-test-no-external-link-source.json",
            "classpath:auto-load/books/books-test-no-external-link-value.json"
    })
    @DisplayName("fail if the editions data is invalid because a field does not have expected value")
    void failInvalidUnexpectedValue(String resourcePath) {
        var booksLoader = new InMemoryBooksLoader(repository, authorsRepository, resourcePath);
        var exception = assertThrows(BooksAutoLoadException.class, booksLoader::load);
        assertThat(exception.getCause()).isNotNull().has(new Condition<>(
                cause -> IllegalArgumentException.class.isAssignableFrom(cause.getClass())
                        || InvalidFormatException.class.isAssignableFrom(cause.getClass())
                        || DateTimeParseException.class.isAssignableFrom(cause.getClass()),
                "Cause is invalid data from JSON file"));
    }

    @ParameterizedTest
    @MethodSource("booksLoadingProvider")
    @DisplayName("contain expected books as defined in the configured resource")
    void containExpectedBooks(String resourcePath, List<Book> expected) {
        var booksLoader = new InMemoryBooksLoader(repository, authorsRepository, resourcePath);
        booksLoader.load();

        var actual = repository.findAll();
        assertThat(actual).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrderElementsOf(expected);
    }

    static Stream<Arguments> booksLoadingProvider() {
        return Stream.of(
                Arguments.of("classpath:auto-load/books/books-test-empty.json", List.of()),
                Arguments.of("classpath:auto-load/books/books-test-valid.json", List.of(
                        L_ETRANGER,
                        LA_PESTE,
                        LA_CHUTE,
                        MADAME_BOVARY,
                        SALAMMBO,
                        ANNA_KARENINE,
                        GUERRE_ET_PAIX,
                        LES_COSAQUES,
                        CHER_CONNARD,
                        BAISE_MOI,
                        APOCALYPSE_BEBE
                ))
        );
    }

}
