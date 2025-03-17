package org.adhuc.library.catalog.adapter.autoload;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.adhuc.library.catalog.adapter.autoload.InMemoryEditionsLoader.EditionsAutoLoadException;
import org.adhuc.library.catalog.authors.internal.InMemoryAuthorsRepository;
import org.adhuc.library.catalog.editions.Edition;
import org.adhuc.library.catalog.editions.internal.InMemoryEditionsRepository;
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
import static org.adhuc.library.catalog.editions.EditionsMother.Real.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Editions auto-load from JSON file should")
class EditionsAutoLoadTests {

    private InMemoryEditionsRepository repository;
    private InMemoryAuthorsRepository authorsRepository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryEditionsRepository();
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
        var editionsLoader = new InMemoryEditionsLoader(repository, authorsRepository, "classpath:unknown.json");
        var exception = assertThrows(EditionsAutoLoadException.class, editionsLoader::load);
        assertThat(exception).hasCauseInstanceOf(FileNotFoundException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "classpath:auto-load/editions/editions-test-not-json.json",
            "classpath:auto-load/editions/editions-test-not-json.yml",
            "classpath:auto-load/editions/editions-test-malformed-json.json"
    })
    @DisplayName("fail if the content does not correspond to valid JSON")
    void failNotJson(String resourcePath) {
        var editionsLoader = new InMemoryEditionsLoader(repository, authorsRepository, resourcePath);
        var exception = assertThrows(EditionsAutoLoadException.class, editionsLoader::load);
        assertThat(exception).hasCauseInstanceOf(JsonParseException.class);
    }

    @Test
    @DisplayName("fail if the JSON root element is not an array")
    void failNotArray() {
        var editionsLoader = new InMemoryEditionsLoader(repository, authorsRepository, "classpath:auto-load/editions/editions-test-root-not-array.json");
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
            "classpath:auto-load/editions/editions-test-no-authors.json",
            "classpath:auto-load/editions/editions-test-empty-authors.json",
            "classpath:auto-load/editions/editions-test-invalid-author.json",
            "classpath:auto-load/editions/editions-test-unknown-author.json",
            "classpath:auto-load/editions/editions-test-unknown-author-of-many.json",
            "classpath:auto-load/editions/editions-test-no-language.json",
            "classpath:auto-load/editions/editions-test-unknown-language.json",
            "classpath:auto-load/editions/editions-test-no-summary.json"
    })
    @DisplayName("fail if the editions data is invalid because a field does not have expected value")
    void failInvalidAuthorsUnexpectedValue(String resourcePath) {
        var editionsLoader = new InMemoryEditionsLoader(repository, authorsRepository, resourcePath);
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
    void containExpectedAuthors(String resourcePath, List<Edition> expected) {
        var editionsLoader = new InMemoryEditionsLoader(repository, authorsRepository, resourcePath);
        editionsLoader.load();

        var actual = repository.findAll();
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    static Stream<Arguments> editionsLoadingProvider() {
        return Stream.of(
                Arguments.of("classpath:auto-load/editions/editions-test-empty.json", List.of()),
                Arguments.of("classpath:auto-load/editions/editions-test-valid.json", List.of(
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
