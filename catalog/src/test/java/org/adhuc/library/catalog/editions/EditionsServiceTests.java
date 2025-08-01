package org.adhuc.library.catalog.editions;

import net.jqwik.api.Arbitraries;
import org.adhuc.library.catalog.books.BooksMother;
import org.adhuc.library.catalog.books.BooksMother.Books;
import org.adhuc.library.catalog.editions.internal.InMemoryEditionsRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.adhuc.library.catalog.editions.EditionsMother.Real.*;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Editions service should")
class EditionsServiceTests {

    private EditionsService service;
    private InMemoryEditionsRepository editionsRepository;

    @BeforeEach
    void setUp() {
        editionsRepository = new InMemoryEditionsRepository();
        service = new EditionsService(editionsRepository);
    }

    @Nested
    @DisplayName("when some editions are in the catalog")
    class EditionsInCatalogTests {

        private static List<Edition> EDITIONS;

        @BeforeAll
        static void initCatalog() {
            EDITIONS = List.of(
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
        }

        @BeforeEach
        void setUp() {
            editionsRepository.saveAll(EDITIONS);
        }

        @ParameterizedTest
        @CsvSource({
                "9782081275232",
                "9782267046892",
                "9782072730672"
        })
        @DisplayName("not find any edition with unknown ISBN")
        void unknownIsbn(String isbn) {
            assertThat(service.getEdition(isbn)).isEmpty();
        }

        @ParameterizedTest
        @MethodSource("knownIsbnsProvider")
        @DisplayName("find edition with known ISBN")
        void knownIsbn(Edition expected) {
            assertThat(service.getEdition(expected.isbn())).isPresent().contains(expected);
        }

        private static Stream<Arguments> knownIsbnsProvider() {
            return Arbitraries.of(EDITIONS).map(Arguments::of).sampleStream().limit(3);
        }

        @ParameterizedTest
        @MethodSource("unknownBookEditionsProvider")
        @DisplayName("not find any editions for unknown book")
        void unknownBookEditions(UUID bookId) {
            assertThat(service.getBookEditions(bookId)).isEmpty();
        }

        private static Stream<Arguments> unknownBookEditionsProvider() {
            return Books.ids()
                    .map(Arguments::of)
                    .sampleStream()
                    .limit(3);
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
