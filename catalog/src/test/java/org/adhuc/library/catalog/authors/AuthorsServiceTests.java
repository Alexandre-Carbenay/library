package org.adhuc.library.catalog.authors;

import net.jqwik.api.Arbitraries;
import org.adhuc.library.catalog.authors.AuthorsMother.Authors;
import org.adhuc.library.catalog.authors.internal.InMemoryAuthorsRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.adhuc.library.catalog.authors.AuthorsMother.authors;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Authors service should")
class AuthorsServiceTests {

    private AuthorsService service;
    private InMemoryAuthorsRepository authorsRepository;

    @BeforeEach
    void setUp() {
        authorsRepository = new InMemoryAuthorsRepository();
        service = new AuthorsService(authorsRepository);
    }

    @Test
    @DisplayName("refuse getting author with null id")
    void errorGetAuthorNullId() {
        assertThrows(IllegalArgumentException.class, () -> service.getAuthor(null));
    }

    @ParameterizedTest
    @CsvSource({
            "917fb110-7991-464e-a623-47c285b6cc3d",
            "a3f3928f-929b-4b08-859c-f2567c295f21",
            "7f91c0f6-dc86-4772-bbff-3b3b6eeedcd6"
    })
    @DisplayName("not find any author with unknown ID")
    void unknownId(UUID id) {
        assertThat(service.getAuthor(id)).isEmpty();
    }

    @Nested
    @DisplayName("when some authors have books in the catalog")
    class WithAuthorsTests {

        private static List<Author> AUTHORS;

        @BeforeAll
        static void initAuthors() {
            AUTHORS = authors().list().ofSize(10).sample();
        }

        @BeforeEach
        void setUp() {
            authorsRepository.saveAll(AUTHORS);
        }

        @ParameterizedTest
        @MethodSource("unknownIdsProvider")
        @DisplayName("not find any author with unknown ID")
        void unknownId(UUID id) {
            assertThat(service.getAuthor(id)).isEmpty();
        }

        private static Stream<Arguments> unknownIdsProvider() {
            var knownIds = AUTHORS.stream().map(Author::id).toList();
            return Authors.ids().filter(id -> !knownIds.contains(id)).map(Arguments::of).sampleStream().limit(3);
        }

        @ParameterizedTest
        @MethodSource("knownIdsProvider")
        @DisplayName("find author with known ID")
        void knownId(Author expected) {
            assertThat(service.getAuthor(expected.id())).isPresent().contains(expected);
        }

        private static Stream<Arguments> knownIdsProvider() {
            return Arbitraries.of(AUTHORS).map(Arguments::of).sampleStream().limit(3);
        }

    }

}
