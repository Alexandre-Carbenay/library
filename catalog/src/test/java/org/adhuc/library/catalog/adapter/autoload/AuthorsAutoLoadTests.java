package org.adhuc.library.catalog.adapter.autoload;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.adhuc.library.catalog.adapter.autoload.InMemoryAuthorsLoader.AuthorsAutoLoadException;
import org.adhuc.library.catalog.authors.Author;
import org.adhuc.library.catalog.authors.internal.InMemoryAuthorsRepository;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.stream.Stream;

import static org.adhuc.library.catalog.authors.AuthorsMother.Real.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Authors auto-load from JSON file should")
class AuthorsAutoLoadTests {

    private InMemoryAuthorsRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryAuthorsRepository();
    }

    @Test
    @DisplayName("fail if the provided JSON resource does not exist")
    void failNonExistingResource() {
        var authorsLoader = new InMemoryAuthorsLoader(repository, "classpath:unknown.json");
        var exception = assertThrows(AuthorsAutoLoadException.class, authorsLoader::load);
        assertThat(exception).hasCauseInstanceOf(FileNotFoundException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "classpath:auto-load/authors/authors-test-not-json.json",
            "classpath:auto-load/authors/authors-test-not-json.yml",
            "classpath:auto-load/authors/authors-test-malformed-json.json"
    })
    @DisplayName("fail if the content does not correspond to valid JSON")
    void failNotJson(String resourcePath) {
        var authorsLoader = new InMemoryAuthorsLoader(repository, resourcePath);
        var exception = assertThrows(AuthorsAutoLoadException.class, authorsLoader::load);
        assertThat(exception).hasCauseInstanceOf(JsonParseException.class);
    }

    @Test
    @DisplayName("fail if the JSON root element is not an array")
    void failNotArray() {
        var authorsLoader = new InMemoryAuthorsLoader(repository, "classpath:auto-load/authors/authors-test-root-not-array.json");
        var exception = assertThrows(AuthorsAutoLoadException.class, authorsLoader::load);
        assertThat(exception).hasCauseInstanceOf(MismatchedInputException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "classpath:auto-load/authors/authors-test-no-id.json",
            "classpath:auto-load/authors/authors-test-id-not-uuid.json",
            "classpath:auto-load/authors/authors-test-id-wrong-type.json",
            "classpath:auto-load/authors/authors-test-no-name.json",
            "classpath:auto-load/authors/authors-test-no-date-of-birth.json",
            "classpath:auto-load/authors/authors-test-date-of-birth-after-date-of-death.json"
    })
    @DisplayName("fail if the authors data is invalid because a field does not have expected value")
    void failInvalidAuthorsUnexpectedValue(String resourcePath) {
        var authorsLoader = new InMemoryAuthorsLoader(repository, resourcePath);
        var exception = assertThrows(AuthorsAutoLoadException.class, authorsLoader::load);
        assertThat(exception).has(new Condition<>(
                e -> e.getCause() != null
                        && (IllegalArgumentException.class.isAssignableFrom(e.getCause().getClass())
                        || InvalidFormatException.class.isAssignableFrom(e.getCause().getClass())),
                "Cause is invalid data from JSON file"));
    }

    @ParameterizedTest
    @MethodSource("authorsLoadingProvider")
    @DisplayName("contain expected authors as defined in the configured resource")
    void containExpectedAuthors(String resourcePath, List<Author> expected) {
        var authorsLoader = new InMemoryAuthorsLoader(repository, resourcePath);
        authorsLoader.load();

        var actual = repository.findAll();
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    static Stream<Arguments> authorsLoadingProvider() {
        return Stream.of(
                Arguments.of("classpath:auto-load/authors/authors-test-empty.json", List.of()),
                Arguments.of("classpath:auto-load/authors/authors-test-valid.json", List.of(
                        ALBERT_CAMUS,
                        GUSTAVE_FLAUBERT,
                        LEON_TOLSTOI,
                        VIRGINIE_DESPENTES
                ))
        );
    }

}
