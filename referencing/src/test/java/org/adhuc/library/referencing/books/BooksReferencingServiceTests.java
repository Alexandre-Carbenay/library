package org.adhuc.library.referencing.books;

import org.adhuc.library.referencing.books.BooksMother.Books;
import org.adhuc.library.referencing.books.internal.InMemoryBooksRepository;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.adhuc.library.referencing.authors.AuthorsMother.Real.*;
import static org.adhuc.library.referencing.books.BooksMother.Real.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Books referencing service should")
class BooksReferencingServiceTests {

    private InMemoryBooksRepository booksRepository;
    private BooksReferencingService service;

    @BeforeEach
    void setUp() {
        booksRepository = new InMemoryBooksRepository();
        service = new BooksReferencingService(booksRepository);
    }

    @Test
    @DisplayName("fail referencing book if its authors list is empty")
    void emptyAuthors() {
        assertThrows(IllegalArgumentException.class, () -> new ReferenceBook(
                Set.of(),
                "fr",
                List.of(new Book.LocalizedDetail("fr", "title", "description"))
        ));
    }

    @Test
    @DisplayName("fail referencing book if its original language is empty")
    void emptyOriginalLanguage() {
        assertThrows(IllegalArgumentException.class, () -> new ReferenceBook(
                Set.of(UUID.randomUUID()),
                "",
                List.of(new Book.LocalizedDetail("fr", "title", "description"))
        ));
    }

    @Test
    @DisplayName("fail referencing book if its details list is empty")
    void emptyDetails() {
        assertThrows(IllegalArgumentException.class, () -> new ReferenceBook(
                Set.of(UUID.randomUUID()),
                "fr",
                List.of()
        ));
    }

    @Test
    @DisplayName("fail referencing book if one detail has empty language")
    void emptyDetailLanguage() {
        assertThrows(IllegalArgumentException.class, () -> new Book.LocalizedDetail("", "title", "description"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t   "})
    @DisplayName("fail referencing book if one detail has empty title")
    void emptyDetailTitle(String title) {
        assertThrows(IllegalArgumentException.class, () -> new Book.LocalizedDetail("fr", title, "description"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t   "})
    @DisplayName("fail referencing book if one detail has empty description")
    void emptyDetailDescription(String description) {
        assertThrows(IllegalArgumentException.class, () -> new Book.LocalizedDetail("fr", "title", description));
    }

    @Test
    @DisplayName("fail referencing book if details contain language duplication")
    void duplicateLanguageInDetails() {
        assertThrows(IllegalArgumentException.class, () -> new ReferenceBook(
                Set.of(UUID.randomUUID()),
                "fr",
                List.of(
                        new Book.LocalizedDetail("en", Books.title(), Books.description()),
                        new Book.LocalizedDetail("it", Books.title(), Books.description()),
                        new Book.LocalizedDetail("en", Books.title(), Books.description())
                )
        ));
    }

    @Test
    @DisplayName("reference book with one author and one detail, providing a book with generated ID")
    void referenceBookSuccessOneAuthor() {
        var author = Books.author();
        var originalLanguage = Books.language();
        var detail = Books.details(originalLanguage);
        var command = new ReferenceBook(Set.of(author), originalLanguage, List.of(detail));
        var book = service.referenceBook(command);

        SoftAssertions.assertSoftly(s -> {
            s.assertThat(book.authors()).containsExactly(author);
            s.assertThat(book.originalLanguage()).isEqualTo(originalLanguage);
            s.assertThat(book.details()).containsExactly(detail);
        });
    }

    @Test
    @DisplayName("reference book with multiple authors successfully")
    void referenceBookSuccessMultipleAuthors() {
        var authors = Books.authors(3);
        var originalLanguage = Books.language();
        var detail = Books.details(originalLanguage);
        var command = new ReferenceBook(authors, originalLanguage, List.of(detail));
        var book = service.referenceBook(command);

        SoftAssertions.assertSoftly(s -> {
            s.assertThat(book.authors()).containsExactlyInAnyOrderElementsOf(authors);
            s.assertThat(book.originalLanguage()).isEqualTo(originalLanguage);
            s.assertThat(book.details()).containsExactly(detail);
        });
    }

    @Test
    @DisplayName("reference book with multiple details successfully")
    void referenceBookSuccessMultipleDetails() {
        var author = Books.author();
        var originalLanguage = Books.language();
        var details = Books.detailsLists(originalLanguage);
        var command = new ReferenceBook(Set.of(author), originalLanguage, details);
        var book = service.referenceBook(command);

        SoftAssertions.assertSoftly(s -> {
            s.assertThat(book.authors()).containsExactly(author);
            s.assertThat(book.originalLanguage()).isEqualTo(originalLanguage);
            s.assertThat(book.details()).containsExactlyInAnyOrderElementsOf(details);
        });
    }

    @Test
    @DisplayName("save book when with details in different languages than original one")
    void referenceBookWithDifferentLanguagesThanOriginal() {
        var authors = Books.authors();
        var originalLanguage = "en";
        var details = List.of(
                new Book.LocalizedDetail("fr", Books.title(), Books.description()),
                new Book.LocalizedDetail("es", Books.title(), Books.description()),
                new Book.LocalizedDetail("it", Books.title(), Books.description())
        );
        var command = new ReferenceBook(authors, originalLanguage, details);
        var book = service.referenceBook(command);

        SoftAssertions.assertSoftly(s -> {
            s.assertThat(book.authors()).containsExactlyInAnyOrderElementsOf(authors);
            s.assertThat(book.originalLanguage()).isEqualTo(originalLanguage);
            s.assertThat(book.details()).containsExactlyInAnyOrderElementsOf(details);
        });
    }

    @Test
    @DisplayName("save book when referencing it")
    void referenceBookSaveIt() {
        var authors = Books.authors();
        var originalLanguage = Books.language();
        var details = Books.detailsLists(originalLanguage);
        var command = new ReferenceBook(authors, originalLanguage, details);
        var book = service.referenceBook(command);

        assertThat(booksRepository.findById(book.id())).isPresent().contains(book);
    }

    @Nested
    class WithExistingBooksTests {

        private static final List<Book> BOOKS = new ArrayList<>();

        @BeforeAll
        static void initBooks() {
            BOOKS.addAll(List.of(
                    L_ETRANGER,
                    LA_PESTE,
                    ASTERIX_LE_GAULOIS,
                    LA_SERPE_D_OR,
                    LE_PETIT_NICOLAS
            ));
        }

        @BeforeEach
        void setUp() {
            booksRepository.saveAll(BOOKS);
        }

        @ParameterizedTest
        @MethodSource("unknownBookReferencingProvider")
        @DisplayName("save unknown book when referencing it")
        void referenceBookSaveIt(ReferenceBook command) {
            var book = service.referenceBook(command);

            assertThat(booksRepository.findById(book.id())).isPresent().contains(book);
        }

        public static Stream<Arguments> unknownBookReferencingProvider() {
            return Stream.of(
                    new ReferenceBook(LA_CHUTE.authors(), LA_CHUTE.originalLanguage(), List.copyOf(LA_CHUTE.details())),
                    new ReferenceBook(LA_CHUTE.authors(), LA_CHUTE.originalLanguage(), LA_CHUTE.detailsIn("fr").map(List::of).orElseThrow()),
                    new ReferenceBook(LA_CHUTE.authors(), LA_CHUTE.originalLanguage(), LA_CHUTE.detailsIn("de").map(List::of).orElseThrow()),
                    new ReferenceBook(LA_PESTE.authors(), LA_PESTE.originalLanguage(), List.of(new Book.LocalizedDetail("de", "Die Pest", "description"))),
                    new ReferenceBook(Set.of(Books.author()), LA_PESTE.originalLanguage(), List.copyOf(LA_PESTE.details())),
                    new ReferenceBook(ASTERIX_ET_CLEOPATRE.authors(), ASTERIX_ET_CLEOPATRE.originalLanguage(), List.copyOf(ASTERIX_ET_CLEOPATRE.details()))
            ).map(Arguments::of);
        }

        @ParameterizedTest
        @MethodSource({
                "existingBookReferencingProvider",
                "existingTitleInAnotherLanguageProvider",
                "existingBookWithSimilarAuthorsProvider"
        })
        @DisplayName("refuse referencing already existing book")
        void referenceExistingBook(ReferenceBook command, int expectedPosition) {
            var duplication = assertThrows(DuplicateBookException.class, () -> service.referenceBook(command));
            assertThat(duplication.position()).isEqualTo(expectedPosition);
        }

        public static Stream<Arguments> existingBookReferencingProvider() {
            return Stream.of(
                    Arguments.of(new ReferenceBook(
                            LA_PESTE.authors(),
                            LA_PESTE.originalLanguage(),
                            List.copyOf(LA_PESTE.details())),
                            0
                    ),
                    Arguments.of(new ReferenceBook(
                            LA_PESTE.authors(),
                            LA_PESTE.originalLanguage(),
                            LA_PESTE.detailsIn("fr").map(List::of).orElseThrow()),
                            0
                    ),
                    Arguments.of(new ReferenceBook(
                            LA_PESTE.authors(),
                            LA_PESTE.originalLanguage(),
                            LA_PESTE.detailsIn("en").map(List::of).orElseThrow()),
                            0
                    ),
                    Arguments.of(new ReferenceBook(
                            LA_PESTE.authors(),
                            LA_PESTE.originalLanguage(),
                            List.of(
                                    new Book.LocalizedDetail("de", "Die Pest", "description"),
                                    new Book.LocalizedDetail("fr", "La Peste", "description")
                            )),
                            1
                    ),
                    Arguments.of(new ReferenceBook(
                            LA_SERPE_D_OR.authors(),
                            LA_SERPE_D_OR.originalLanguage(),
                            List.copyOf(LA_SERPE_D_OR.details())),
                            0
                    )
            );
        }

        public static Stream<Arguments> existingTitleInAnotherLanguageProvider() {
            return Stream.of(
                    Arguments.of(new ReferenceBook(
                            LA_PESTE.authors(),
                            LA_PESTE.originalLanguage(),
                            List.of(new Book.LocalizedDetail("de", "La Peste", "description"))),
                            0
                    ),
                    Arguments.of(new ReferenceBook(
                            LA_PESTE.authors(),
                            LA_PESTE.originalLanguage(),
                            List.of(new Book.LocalizedDetail("fr", "The Plague", "description"))),
                            0
                    )
            );
        }

        public static Stream<Arguments> existingBookWithSimilarAuthorsProvider() {
            return Stream.of(
                    Arguments.of(new ReferenceBook(
                            Set.of(RENE_GOSCINNY.id()),
                            LE_PETIT_NICOLAS.originalLanguage(),
                            List.copyOf(LE_PETIT_NICOLAS.details())),
                            0
                    ),
                    Arguments.of(new ReferenceBook(
                            Set.of(JEAN_JACQUES_SEMPE.id()),
                            LE_PETIT_NICOLAS.originalLanguage(),
                            List.copyOf(LE_PETIT_NICOLAS.details())),
                            0
                    ),
                    Arguments.of(new ReferenceBook(
                            Set.of(RENE_GOSCINNY.id(), ALBERT_UDERZO.id(), JEAN_JACQUES_SEMPE.id()),
                            LE_PETIT_NICOLAS.originalLanguage(),
                            List.copyOf(LE_PETIT_NICOLAS.details())),
                            0
                    )
            );
        }

    }

}
