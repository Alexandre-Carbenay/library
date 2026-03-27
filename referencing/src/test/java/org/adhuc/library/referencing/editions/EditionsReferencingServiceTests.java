package org.adhuc.library.referencing.editions;

import org.adhuc.library.referencing.books.Book;
import org.adhuc.library.referencing.books.BooksMother;
import org.adhuc.library.referencing.books.internal.InMemoryBooksRepository;
import org.adhuc.library.referencing.editions.internal.InMemoryEditionsRepository;
import org.adhuc.library.referencing.publishers.PublishersMother;
import org.adhuc.library.referencing.publishers.internal.InMemoryPublishersRepository;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;
import java.util.stream.Stream;

import static org.adhuc.library.referencing.editions.EditionsMother.Editions.*;
import static org.adhuc.library.referencing.editions.EditionsMother.edition;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Editions referencing service should")
class EditionsReferencingServiceTests {

    private InMemoryBooksRepository booksRepository;
    private InMemoryPublishersRepository publishersRepository;
    private InMemoryEditionsRepository editionsRepository;
    private EditionsReferencingService service;

    @BeforeEach
    void setUp() {
        booksRepository = new InMemoryBooksRepository();
        publishersRepository = new InMemoryPublishersRepository();
        editionsRepository = new InMemoryEditionsRepository();
        service = new EditionsReferencingService(booksRepository, publishersRepository, editionsRepository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t   ", "invalid"})
    @DisplayName("fail referencing edition if its ISBN is empty or invalid")
    void emptyOrInvalidIsbn(String isbn) {
        assertThrows(IllegalArgumentException.class, () -> new ReferenceEdition(isbn, language(), book(), publisher(), publicationDate(), title(), summary()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t   "})
    @DisplayName("fail referencing edition if its language is empty")
    void emptyLanguage(String language) {
        assertThrows(IllegalArgumentException.class, () -> new ReferenceEdition(isbn(), language, book(), publisher(), publicationDate(), title(), summary()));
    }

    @Test
    @DisplayName("fail referencing edition if its book is unknown")
    void unknownBook() {
        var command = new ReferenceEdition(isbn(), language(), book(), publisher(), publicationDate(), title(), summary());

        publishersRepository.save(PublishersMother.builder().id(command.publisher()).build());

        assertThrows(UnknownEditionBookException.class, () -> service.referenceEdition(command));
    }

    @Test
    @DisplayName("fail referencing edition if its publisher is unknown")
    void unknownPublisher() {
        var command = new ReferenceEdition(isbn(), language(), book(), publisher(), publicationDate(), title(), summary());

        booksRepository.save(BooksMother.builder().id(command.book()).build());

        assertThrows(UnknownEditionPublisherException.class, () -> service.referenceEdition(command));
    }

    @Test
    @DisplayName("refuse referencing already existing edition")
    void referenceExistingEdition() {
        var edition = edition();
        booksRepository.save(BooksMother.builder().id(edition.book()).build());
        publishersRepository.save(PublishersMother.builder().id(edition.publisher()).build());

        editionsRepository.save(edition);

        var command = new ReferenceEdition(edition.isbn(), language(), book(), publisher(), publicationDate(), title(), summary());
        assertThrows(DuplicateEditionException.class, () -> service.referenceEdition(command));
    }

    @Test
    @DisplayName("reference edition successfully, providing an edition with all information")
    void referenceEditionSuccess() {
        var command = new ReferenceEdition(isbn(), language(), book(), publisher(), publicationDate(), title(), summary());

        booksRepository.save(BooksMother.builder().id(command.book()).build());
        publishersRepository.save(PublishersMother.builder().id(command.publisher()).build());

        var edition = service.referenceEdition(command);

        SoftAssertions.assertSoftly(s -> {
            s.assertThat(edition.isbn()).isEqualTo(command.isbn());
            s.assertThat(edition.language()).isEqualTo(command.language());
            s.assertThat(edition.book()).isEqualTo(command.book());
            s.assertThat(edition.publisher()).isEqualTo(command.publisher());
            s.assertThat(edition.publicationDate()).isEqualTo(command.publicationDate());
            s.assertThat(edition.title()).isEqualTo(command.title().orElseThrow());
            s.assertThat(edition.summary()).isEqualTo(command.summary().orElseThrow());
        });
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("editionFromBookProvider")
    @DisplayName("reference edition without title and/or summary successfully, copying the book's title and/or description")
    void referenceEditionWithoutTitleOrSummary(String scenario, ReferenceEdition command, Book book, Edition expected) {
        booksRepository.save(book);
        publishersRepository.save(PublishersMother.builder().id(command.publisher()).build());

        var actual = service.referenceEdition(command);

        SoftAssertions.assertSoftly(s -> {
            s.assertThat(actual.isbn()).isEqualTo(expected.isbn());
            s.assertThat(actual.language()).isEqualTo(expected.language());
            s.assertThat(actual.book()).isEqualTo(expected.book());
            s.assertThat(actual.publisher()).isEqualTo(expected.publisher());
            s.assertThat(actual.publicationDate()).isEqualTo(expected.publicationDate());
            s.assertThat(actual.title()).isEqualTo(expected.title());
            s.assertThat(actual.summary()).isEqualTo(expected.summary());
        });
    }

    private static Stream<Arguments> editionFromBookProvider() {
        var isbn = isbn();
        var book = BooksMother.builder()
                .originalLanguage("fr")
                .details(Set.of(
                        new Book.LocalizedDetail("fr", "French title", "French description"),
                        new Book.LocalizedDetail("en", "English title", "English description")
                ))
                .build();
        var publisherId = publisher();
        var publicationDate = publicationDate();

        return Stream.of(
                Arguments.of(
                        "Edition in book's original language",
                        new ReferenceEdition(isbn, "fr", book.id(), publisherId, publicationDate),
                        book,
                        new Edition(isbn, "fr", book.id(), publisherId, publicationDate, "French title", "French description")
                ),
                Arguments.of(
                        "Edition in book's other language",
                        new ReferenceEdition(isbn, "en", book.id(), publisherId, publicationDate),
                        book,
                        new Edition(isbn, "en", book.id(), publisherId, publicationDate, "English title", "English description")
                ),
                Arguments.of(
                        "Edition in unknown language",
                        new ReferenceEdition(isbn, "de", book.id(), publisherId, publicationDate),
                        book,
                        new Edition(isbn, "de", book.id(), publisherId, publicationDate, "French title", "French description")
                ),
                Arguments.of(
                        "Edition with only title in book's original language",
                        new ReferenceEdition(isbn, "fr", book.id(), publisherId, publicationDate, "Title", null),
                        book,
                        new Edition(isbn, "fr", book.id(), publisherId, publicationDate, "Title", "French description")
                ),
                Arguments.of(
                        "Edition with only title in book's other language",
                        new ReferenceEdition(isbn, "en", book.id(), publisherId, publicationDate, "Title", null),
                        book,
                        new Edition(isbn, "en", book.id(), publisherId, publicationDate, "Title", "English description")
                ),
                Arguments.of(
                        "Edition with only title in unknown language",
                        new ReferenceEdition(isbn, "de", book.id(), publisherId, publicationDate, "Title", null),
                        book,
                        new Edition(isbn, "de", book.id(), publisherId, publicationDate, "Title", "French description")
                ),
                Arguments.of(
                        "Edition with only summary in book's original language",
                        new ReferenceEdition(isbn, "fr", book.id(), publisherId, publicationDate, null, "Summary"),
                        book,
                        new Edition(isbn, "fr", book.id(), publisherId, publicationDate, "French title", "Summary")
                ),
                Arguments.of(
                        "Edition with only summary in book's other language",
                        new ReferenceEdition(isbn, "en", book.id(), publisherId, publicationDate, null, "Summary"),
                        book,
                        new Edition(isbn, "en", book.id(), publisherId, publicationDate, "English title", "Summary")
                ),
                Arguments.of(
                        "Edition with only summary in unknown language",
                        new ReferenceEdition(isbn, "de", book.id(), publisherId, publicationDate, null, "Summary"),
                        book,
                        new Edition(isbn, "de", book.id(), publisherId, publicationDate, "French title", "Summary")
                )
        );
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("editionReferencingErrorProvider")
    @DisplayName("not reference edition without title and/or summary when book has no replacement for title or description")
    void referenceEditionWithoutTitleOrSummary(String scenario, ReferenceEdition command, Book book) {
        booksRepository.save(book);
        publishersRepository.save(PublishersMother.builder().id(command.publisher()).build());

        assertThrows(IllegalStateException.class, () -> service.referenceEdition(command));
    }

    private static Stream<Arguments> editionReferencingErrorProvider() {
        var isbn = isbn();
        var book = BooksMother.builder()
                .originalLanguage("fr")
                .details(Set.of(
                        new Book.LocalizedDetail("en", "English title", "English description")
                ))
                .build();
        var publisherId = publisher();
        var publicationDate = publicationDate();

        return Stream.of(
                Arguments.of(
                        "Edition without title",
                        new ReferenceEdition(isbn, "fr", book.id(), publisherId, publicationDate, null, "Summary"),
                        book
                ),
                Arguments.of(
                        "Edition without summary",
                        new ReferenceEdition(isbn, "fr", book.id(), publisherId, publicationDate, "Title", null),
                        book
                ),
                Arguments.of(
                        "Edition without title nor summary",
                        new ReferenceEdition(isbn, "fr", book.id(), publisherId, publicationDate),
                        book
                )
        );
    }

    @Test
    @DisplayName("save edition when referencing it")
    void referenceEditionSaveIt() {
        var command = new ReferenceEdition(isbn(), language(), book(), publisher(), publicationDate(), title(), summary());

        booksRepository.save(BooksMother.builder().id(command.book()).build());
        publishersRepository.save(PublishersMother.builder().id(command.publisher()).build());

        var edition = service.referenceEdition(command);

        assertThat(editionsRepository.findByIsbn(edition.isbn())).isPresent().contains(edition);
    }

}
