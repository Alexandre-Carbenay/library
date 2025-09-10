package org.adhuc.library.catalog.editions;

import org.adhuc.library.catalog.books.BooksMother;
import org.adhuc.library.catalog.books.BooksMother.Books;
import org.adhuc.library.catalog.editions.EditionsMother.Editions;
import org.adhuc.library.catalog.editions.internal.InMemoryEditionsRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.adhuc.library.catalog.editions.EditionsMother.Real.*;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
@Tag("useCase")
@DisplayName("Editions service should")
class EditionsServiceTests {

    private EditionsService service;
    private InMemoryEditionsRepository editionsRepository;

    @BeforeEach
    void setUp() {
        editionsRepository = new InMemoryEditionsRepository();
        service = new EditionsService(editionsRepository);
    }

    @Test
    @DisplayName("not find any edition with unknown ISBN")
    void unknownIsbn() {
        assertThat(service.getEdition(L_ETRANGER.isbn())).isEmpty();
    }

    @Nested
    @DisplayName("when some editions are in the catalog")
    class EditionsInCatalogTests {

        private static final List<Edition> EDITIONS = List.of(
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
                APOCALYPSE_BEBE,
                ASTERIX_LE_GAULOIS,
                LA_SERPE_D_OR,
                ASTERIX_ET_CLEOPATRE,
                LE_PETIT_NICOLAS
        );

        @BeforeEach
        void setUp() {
            editionsRepository.saveAll(EDITIONS);
        }

        @Test
        @DisplayName("not find any edition with unknown ISBN")
        void unknownIsbn() {
            var isbn = Editions.isbn();
            assertThat(service.getEdition(isbn)).isEmpty();
        }

        @ParameterizedTest
        @MethodSource("knownIsbnsProvider")
        @DisplayName("find edition with known ISBN")
        void knownIsbn(Edition expected) {
            assertThat(service.getEdition(expected.isbn())).isPresent().contains(expected);
        }

        private static Stream<Arguments> knownIsbnsProvider() {
            return EDITIONS.stream().map(Arguments::of);
        }

        @Test
        @DisplayName("not find any editions for unknown book")
        void unknownBookEditions() {
            var bookId = Books.id();
            assertThat(service.getBookEditions(bookId)).isEmpty();
        }

        @ParameterizedTest
        @MethodSource("knownBookEditionsProvider")
        @DisplayName("find editions for known book")
        void knownBookEditions(UUID bookId, List<Edition> expected) {
            assertThat(service.getBookEditions(bookId)).containsExactlyInAnyOrderElementsOf(expected);
        }

        private static Stream<Arguments> knownBookEditionsProvider() {
            return Stream.of(
                    Arguments.of(
                            BooksMother.Real.L_ETRANGER.id(),
                            List.of(L_ETRANGER)
                    ),
                    Arguments.of(
                            BooksMother.Real.ANNA_KARENINE.id(),
                            List.of(ANNA_KARENINE)
                    ),
                    Arguments.of(
                            BooksMother.Real.GUERRE_ET_PAIX.id(),
                            List.of(LA_GUERRE_ET_LA_PAIX_1, LA_GUERRE_ET_LA_PAIX_2)
                    )
            );
        }

    }

}
