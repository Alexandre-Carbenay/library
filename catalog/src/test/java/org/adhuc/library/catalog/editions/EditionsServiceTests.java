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

import static org.adhuc.library.catalog.authors.AuthorsMother.Real.*;
import static org.adhuc.library.catalog.editions.EditionsMother.Real.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    @DisplayName("refuse getting edition with null ISBN")
    void errorGetEditionNullIsbn() {
        assertThrows(IllegalArgumentException.class, () -> service.getEdition(null));
    }

    @Test
    @DisplayName("refuse getting editions for a null list of books")
    void errorGetEditionsNullBookIds() {
        assertThrows(IllegalArgumentException.class, () -> service.getBooksEditions(null));
    }

    @Test
    @DisplayName("refuse getting notable editions for a null author")
    void errorGetNotableEditionsNullAuthorId() {
        assertThrows(IllegalArgumentException.class, () -> service.getNotableEditions(null));
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
        @DisplayName("not find any editions for unknown books")
        void unknownBooksEditions(List<UUID> bookIds) {
            assertThat(service.getBooksEditions(bookIds)).isEmpty();
        }

        private static Stream<Arguments> unknownBookEditionsProvider() {
            return Books.ids().list().ofMinSize(0).ofMaxSize(10)
                    .map(Arguments::of)
                    .sampleStream()
                    .limit(3);
        }

        @ParameterizedTest
        @MethodSource("knownBookEditionsProvider")
        @DisplayName("find editions for known books")
        void knownBooksEditions(List<UUID> bookIds, List<Edition> expected) {
            assertThat(service.getBooksEditions(bookIds)).containsExactlyInAnyOrderElementsOf(expected);
        }

        private static Stream<Arguments> knownBookEditionsProvider() {
            return Stream.of(
                    Arguments.of(
                            List.of(BooksMother.Real.L_ETRANGER.id()),
                            List.of(L_ETRANGER)
                    ),
                    Arguments.of(
                            List.of(
                                    BooksMother.Real.L_ETRANGER.id(),
                                    BooksMother.Real.ANNA_KARENINE.id()
                            ),
                            List.of(
                                    L_ETRANGER,
                                    ANNA_KARENINE
                            )
                    ),
                    Arguments.of(
                            List.of(BooksMother.Real.GUERRE_ET_PAIX.id()),
                            List.of(LA_GUERRE_ET_LA_PAIX_1, LA_GUERRE_ET_LA_PAIX_2)
                    ),
                    Arguments.of(
                            List.of(
                                    BooksMother.Real.GUERRE_ET_PAIX.id(),
                                    BooksMother.Real.ANNA_KARENINE.id()
                            ),
                            List.of(
                                    LA_GUERRE_ET_LA_PAIX_1,
                                    LA_GUERRE_ET_LA_PAIX_2,
                                    ANNA_KARENINE
                            )
                    )

            );
        }

        @ParameterizedTest
        @CsvSource({
                "917fb110-7991-464e-a623-47c285b6cc3d",
                "a3f3928f-929b-4b08-859c-f2567c295f21",
                "7f91c0f6-dc86-4772-bbff-3b3b6eeedcd6"
        })
        @DisplayName("not find any notable edition for unknown authors")
        void unknownAuthorNotableEditions(UUID authorId) {
            assertThat(service.getNotableEditions(authorId)).isEmpty();
        }

        @ParameterizedTest
        @MethodSource("knownAuthorNotableEditionsProvider")
        @DisplayName("find notable editions for known authors")
        void knownAuthorNotableEditions(UUID authorId, List<Edition> expected) {
            assertThat(service.getNotableEditions(authorId)).containsExactlyInAnyOrderElementsOf(expected);
        }

        private static Stream<Arguments> knownAuthorNotableEditionsProvider() {
            return Stream.of(
                    Arguments.of(ALBERT_CAMUS.id(), List.of(L_ETRANGER, LA_PESTE, LA_CHUTE)),
                    Arguments.of(LEON_TOLSTOI.id(), List.of(ANNA_KARENINE, LA_GUERRE_ET_LA_PAIX_1, LA_GUERRE_ET_LA_PAIX_2, LES_COSAQUES)),
                    Arguments.of(RENE_GOSCINNY.id(), List.of(ASTERIX_LE_GAULOIS, LA_SERPE_D_OR, ASTERIX_ET_CLEOPATRE, LE_PETIT_NICOLAS)),
                    Arguments.of(ALBERT_UDERZO.id(), List.of(ASTERIX_LE_GAULOIS, LA_SERPE_D_OR, ASTERIX_ET_CLEOPATRE))
            );
        }

    }

}
