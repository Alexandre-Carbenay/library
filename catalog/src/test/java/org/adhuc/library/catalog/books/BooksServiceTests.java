package org.adhuc.library.catalog.books;

import net.jqwik.api.Arbitraries;
import org.adhuc.library.catalog.books.internal.InMemoryBooksRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.adhuc.library.catalog.authors.AuthorsMother.Real.*;
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
    @DisplayName("refuse getting book with null ISBN")
    void errorGetBookNullIsbn() {
        assertThrows(IllegalArgumentException.class, () -> service.getBook(null));
    }

    @Test
    @DisplayName("refuse getting notable books for a null author")
    void errorGetNotableBooksNullAuthorId() {
        assertThrows(IllegalArgumentException.class, () -> service.getNotableBooks(null));
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
            booksRepository.saveAll(BOOKS);
        }

        @ParameterizedTest
        @CsvSource({
                "9782081275232",
                "9782267046892",
                "9782072730672"
        })
        @DisplayName("not find any book with unknown ISBN")
        void unknownIsbn(String isbn) {
            assertThat(service.getBook(isbn)).isEmpty();
        }

        @ParameterizedTest
        @MethodSource("knownIsbnsProvider")
        @DisplayName("find book with known ISBN")
        void knownIsbn(Book expected) {
            assertThat(service.getBook(expected.isbn())).isPresent().contains(expected);
        }

        private static Stream<Arguments> knownIsbnsProvider() {
            return Arbitraries.of(BOOKS).map(Arguments::of).sampleStream().limit(3);
        }

        @ParameterizedTest
        @CsvSource({
                "917fb110-7991-464e-a623-47c285b6cc3d",
                "a3f3928f-929b-4b08-859c-f2567c295f21",
                "7f91c0f6-dc86-4772-bbff-3b3b6eeedcd6"
        })
        @DisplayName("not find any notable book for unknown authors")
        void unknownAuthorNotableBooks(UUID authorId) {
            assertThat(service.getNotableBooks(authorId)).isEmpty();
        }

        @ParameterizedTest
        @MethodSource("knownAuthorNotableBooksProvider")
        @DisplayName("find notable books for known authors")
        void knownAuthorNotableBooks(UUID authorId, List<Book> expected) {
            assertThat(service.getNotableBooks(authorId)).containsExactlyInAnyOrderElementsOf(expected);
        }

        private static Stream<Arguments> knownAuthorNotableBooksProvider() {
            return Stream.of(
                    Arguments.of(ALBERT_CAMUS.id(), List.of(L_ETRANGER, LA_PESTE, LA_CHUTE)),
                    Arguments.of(LEON_TOLSTOI.id(), List.of(ANNA_KARENINE, LA_GUERRE_ET_LA_PAIX_1, LA_GUERRE_ET_LA_PAIX_2, LES_COSAQUES)),
                    Arguments.of(RENE_GOSCINNY.id(), List.of(ASTERIX_LE_GAULOIS, LA_SERPE_D_OR, ASTERIX_ET_CLEOPATRE, LE_PETIT_NICOLAS)),
                    Arguments.of(ALBERT_UDERZO.id(), List.of(ASTERIX_LE_GAULOIS, LA_SERPE_D_OR, ASTERIX_ET_CLEOPATRE))
            );
        }

    }

}
