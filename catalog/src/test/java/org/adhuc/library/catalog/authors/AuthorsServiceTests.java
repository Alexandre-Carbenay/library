package org.adhuc.library.catalog.authors;

import org.adhuc.library.catalog.authors.AuthorsMother.Authors;
import org.adhuc.library.catalog.authors.internal.InMemoryAuthorsRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.adhuc.library.catalog.authors.AuthorsMother.authors;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
@Tag("useCase")
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
    @DisplayName("not find any author with unknown ID")
    void unknownId() {
        var id = Authors.id();
        assertThat(service.getAuthor(id)).isEmpty();
    }

    @Nested
    @DisplayName("when some authors have editions in the catalog")
    class WithAuthorsTests {

        private static final List<Author> AUTHORS = List.copyOf(authors(10));

        @BeforeEach
        void setUp() {
            authorsRepository.saveAll(AUTHORS);
        }

        @Test
        @DisplayName("not find any author with unknown ID")
        void unknownId() {
            var id = Authors.id();
            assertThat(service.getAuthor(id)).isEmpty();
        }

        @ParameterizedTest
        @MethodSource("knownIdsProvider")
        @DisplayName("find author with known ID")
        void knownId(Author expected) {
            assertThat(service.getAuthor(expected.id())).isPresent().contains(expected);
        }

        private static Stream<Arguments> knownIdsProvider() {
            return AUTHORS.stream().map(Arguments::of);
        }

    }

}
