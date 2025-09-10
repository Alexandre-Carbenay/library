package org.adhuc.library.catalog.books;

import org.adhuc.library.catalog.authors.AuthorsMother.Authors;
import org.adhuc.library.catalog.books.BooksMother.Books;
import org.adhuc.library.catalog.books.internal.InMemoryBooksRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.adhuc.library.catalog.authors.AuthorsMother.Real.*;
import static org.adhuc.library.catalog.books.BooksMother.Real.*;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
@Tag("useCase")
@DisplayName("Books service should")
class BooksServiceTests {

    private BooksService service;
    private InMemoryBooksRepository booksRepository;

    @BeforeEach
    void setUp() {
        booksRepository = new InMemoryBooksRepository();
        service = new BooksService(booksRepository);
    }

    @Nested
    @DisplayName("when some books are in the catalog")
    class BooksInCatalogTests {

        private static final List<Book> BOOKS = List.of(
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

        @BeforeEach
        void setUp() {
            booksRepository.saveAll(BOOKS);
        }

        @Test
        @DisplayName("not find any book with unknown ID")
        void unknownId() {
            var id = Books.id();
            assertThat(service.getBook(id)).isEmpty();
        }

        @ParameterizedTest
        @MethodSource("knownIdProvider")
        @DisplayName("find book with known ID")
        void knownId(Book expected) {
            assertThat(service.getBook(expected.id())).isPresent().contains(expected);
        }

        private static Stream<Arguments> knownIdProvider() {
            return BOOKS.stream().map(Arguments::of);
        }

        @Test
        @DisplayName("not find any notable book for unknown author")
        void unknownAuthorNotableBooks() {
            var authorId = Authors.id();
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
                    Arguments.of(LEON_TOLSTOI.id(), List.of(ANNA_KARENINE, GUERRE_ET_PAIX, LES_COSAQUES)),
                    Arguments.of(RENE_GOSCINNY.id(), List.of(ASTERIX_LE_GAULOIS, LA_SERPE_D_OR, ASTERIX_ET_CLEOPATRE, LE_PETIT_NICOLAS)),
                    Arguments.of(ALBERT_UDERZO.id(), List.of(ASTERIX_LE_GAULOIS, LA_SERPE_D_OR, ASTERIX_ET_CLEOPATRE))
            );
        }

    }

}
