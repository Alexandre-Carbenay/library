package org.adhuc.library.catalog.books;

import net.jqwik.api.Arbitraries;
import org.adhuc.library.catalog.books.internal.InMemoryBooksRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.adhuc.library.catalog.books.BooksMother.Real.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Books service should")
class BooksServiceTests {

    private BooksService service;
    private InMemoryBooksRepository booksRepository;

    @BeforeEach
    void setUp() {
        booksRepository = new InMemoryBooksRepository();
        service = new BooksService(booksRepository);
    }

    @Test
    @DisplayName("refuse getting book with null ID")
    void errorGetBookNullId() {
        assertThrows(IllegalArgumentException.class, () -> service.getBook(null));
    }

    @Nested
    @DisplayName("when some books are in the catalog")
    class BooksInCatalogTests {

        private static List<Book> BOOKS;

        @BeforeAll
        static void initCatalog() {
            BOOKS = List.of(
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
                    APOCALYPSE_BEBE,
                    ASTERIX_LE_GAULOIS,
                    LA_SERPE_D_OR,
                    ASTERIX_ET_CLEOPATRE,
                    LE_PETIT_NICOLAS
            );
        }

        @BeforeEach
        void setUp() {
            booksRepository.saveAll(BOOKS);
        }

        @ParameterizedTest
        @MethodSource("unknownIdProvider")
        @DisplayName("not find any book with unknown ID")
        void unknownIsbn(UUID id) {
            assertThat(service.getBook(id)).isEmpty();
        }

        private static Stream<Arguments> unknownIdProvider() {
            return Arbitraries.create(UUID::randomUUID).map(Arguments::of)
                    .sampleStream().limit(3);
        }

        @ParameterizedTest
        @MethodSource("knownIdProvider")
        @DisplayName("find book with known ID")
        void knownId(Book expected) {
            assertThat(service.getBook(expected.id())).isPresent().contains(expected);
        }

        private static Stream<Arguments> knownIdProvider() {
            return Arbitraries.of(BOOKS).map(Arguments::of).sampleStream().limit(3);
        }

    }

}
