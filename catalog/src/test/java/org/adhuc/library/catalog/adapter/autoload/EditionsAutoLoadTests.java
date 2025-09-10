package org.adhuc.library.catalog.adapter.autoload;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.adhuc.library.catalog.adapter.autoload.InMemoryEditionsLoader.EditionsAutoLoadException;
import org.adhuc.library.catalog.books.BooksMother;
import org.adhuc.library.catalog.books.internal.InMemoryBooksRepository;
import org.adhuc.library.catalog.editions.Edition;
import org.adhuc.library.catalog.editions.internal.InMemoryEditionsRepository;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.FileNotFoundException;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Stream;

import static org.adhuc.library.catalog.editions.EditionsMother.Real.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("integration")
@Tag("dataLoading")
@DisplayName("Editions auto-load from JSON file should")
class EditionsAutoLoadTests {

    private InMemoryEditionsRepository repository;
    private InMemoryBooksRepository booksRepository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryEditionsRepository();
        booksRepository = new InMemoryBooksRepository();
        booksRepository.saveAll(List.of(
                BooksMother.Real.L_ETRANGER,
                BooksMother.Real.LA_PESTE,
                BooksMother.Real.LA_CHUTE,
                BooksMother.Real.MADAME_BOVARY,
                BooksMother.Real.SALAMMBO,
                BooksMother.Real.ANNA_KARENINE,
                BooksMother.Real.GUERRE_ET_PAIX,
                BooksMother.Real.LES_COSAQUES,
                BooksMother.Real.CHER_CONNARD,
                BooksMother.Real.BAISE_MOI,
                BooksMother.Real.APOCALYPSE_BEBE,
                BooksMother.Real.ASTERIX_LE_GAULOIS,
                BooksMother.Real.LA_SERPE_D_OR,
                BooksMother.Real.ASTERIX_ET_CLEOPATRE,
                BooksMother.Real.LE_PETIT_NICOLAS
        ));
    }

    @Test
    @DisplayName("fail if the provided JSON edition resource does not exist")
    void failNonExistingEditionsResource() {
        var editionsLoader = new InMemoryEditionsLoader(repository, booksRepository,
                "classpath:unknown.json",
                "classpath:auto-load/publishers/publishers-test-valid.json");
        var exception = assertThrows(EditionsAutoLoadException.class, editionsLoader::load);
        assertThat(exception).hasCauseInstanceOf(FileNotFoundException.class);
    }

    @Test
    @DisplayName("fail if the provided JSON edition resource does not exist")
    void failNonExistingPublishersResource() {
        var editionsLoader = new InMemoryEditionsLoader(repository, booksRepository,
                "classpath:auto-load/editions/editions-test-valid.json",
                "classpath:unknown.json");
        var exception = assertThrows(EditionsAutoLoadException.class, editionsLoader::load);
        assertThat(exception).hasCauseInstanceOf(FileNotFoundException.class);
    }

    @ParameterizedTest
    @CsvSource({
            "classpath:auto-load/editions/editions-test-not-json.json,classpath:auto-load/publishers/publishers-test-valid.json",
            "classpath:auto-load/editions/editions-test-not-json.yml,classpath:auto-load/publishers/publishers-test-valid.json",
            "classpath:auto-load/editions/editions-test-malformed-json.json,classpath:auto-load/publishers/publishers-test-valid.json",
            "classpath:auto-load/editions/editions-test-valid.json,classpath:auto-load/publishers/publishers-test-not-json.json",
            "classpath:auto-load/editions/editions-test-valid.json,classpath:auto-load/publishers/publishers-test-not-json.yml",
            "classpath:auto-load/editions/editions-test-valid.json,classpath:auto-load/publishers/publishers-test-malformed-json.json"
    })
    @DisplayName("fail if the content does not correspond to valid JSON")
    void failNotJson(String editionsResourcePath, String publishersResourcePath) {
        var editionsLoader = new InMemoryEditionsLoader(repository, booksRepository, editionsResourcePath, publishersResourcePath);
        var exception = assertThrows(EditionsAutoLoadException.class, editionsLoader::load);
        assertThat(exception).hasCauseInstanceOf(JsonParseException.class);
    }

    @Test
    @DisplayName("fail if the JSON editions root element is not an array")
    void failEditionsNotArray() {
        var editionsLoader = new InMemoryEditionsLoader(repository, booksRepository,
                "classpath:auto-load/editions/editions-test-root-not-array.json",
                "classpath:auto-load/publishers/publishers-test-valid.json");
        var exception = assertThrows(EditionsAutoLoadException.class, editionsLoader::load);
        assertThat(exception).hasCauseInstanceOf(MismatchedInputException.class);
    }

    @Test
    @DisplayName("fail if the JSON publishers root element is not an array")
    void failPublishersNotArray() {
        var editionsLoader = new InMemoryEditionsLoader(repository, booksRepository,
                "classpath:auto-load/editions/editions-test-valid.json",
                "classpath:auto-load/publishers/publishers-test-root-not-array.json");
        var exception = assertThrows(EditionsAutoLoadException.class, editionsLoader::load);
        assertThat(exception).hasCauseInstanceOf(MismatchedInputException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "classpath:auto-load/editions/editions-test-no-isbn.json",
            "classpath:auto-load/editions/editions-test-isbn-not-valid.json",
            "classpath:auto-load/editions/editions-test-no-title.json",
            "classpath:auto-load/editions/editions-test-no-publication-date.json",
            "classpath:auto-load/editions/editions-test-publication-date-not-valid.json",
            "classpath:auto-load/editions/editions-test-no-book.json",
            "classpath:auto-load/editions/editions-test-invalid-book.json",
            "classpath:auto-load/editions/editions-test-unknown-book.json",
            "classpath:auto-load/editions/editions-test-no-language.json",
            "classpath:auto-load/editions/editions-test-unknown-language.json",
            "classpath:auto-load/editions/editions-test-no-summary.json"
    })
    @DisplayName("fail if the editions data is invalid because a field does not have expected value")
    void failInvalidUnexpectedValue(String resourcePath) {
        var editionsLoader = new InMemoryEditionsLoader(repository, booksRepository, resourcePath,
                "classpath:auto-load/publishers/publishers-test-valid.json");
        var exception = assertThrows(EditionsAutoLoadException.class, editionsLoader::load);
        assertThat(exception.getCause()).isNotNull().has(new Condition<>(
                cause -> IllegalArgumentException.class.isAssignableFrom(cause.getClass())
                        || InvalidFormatException.class.isAssignableFrom(cause.getClass())
                        || DateTimeParseException.class.isAssignableFrom(cause.getClass()),
                "Cause is invalid data from JSON file"));
    }

    @ParameterizedTest
    @MethodSource("editionsLoadingProvider")
    @DisplayName("contain expected editions as defined in the configured resource")
    void containExpectedEditions(String editionsResourcePath, String publishersResourcePath, List<Edition> expected) {
        var editionsLoader = new InMemoryEditionsLoader(repository, booksRepository, editionsResourcePath, publishersResourcePath);
        editionsLoader.load();

        var actual = repository.findAll();
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    static Stream<Arguments> editionsLoadingProvider() {
        return Stream.of(
                Arguments.of("classpath:auto-load/editions/editions-test-empty.json", "classpath:auto-load/publishers/publishers-test-empty.json", List.of()),
                Arguments.of("classpath:auto-load/editions/editions-test-empty.json", "classpath:auto-load/publishers/publishers-test-valid.json", List.of()),
                Arguments.of("classpath:auto-load/editions/editions-test-valid.json", "classpath:auto-load/publishers/publishers-test-valid.json", List.of(
                        L_ETRANGER,
                        LA_PESTE,
                        LA_CHUTE,
                        MADAME_BOVARY,
                        SALAMMBO,
                        ANNA_KARENINE,
                        LA_GUERRE_ET_LA_PAIX_1,
                        LA_GUERRE_ET_LA_PAIX_2,
                        LES_COSAQUES,
                        CHER_CONNARD,
                        BAISE_MOI,
                        APOCALYPSE_BEBE
                ))
        );
    }

}
