package org.adhuc.library.catalog.books;

import org.adhuc.library.catalog.books.internal.InMemoryBooksRepository;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.adhuc.library.catalog.books.BooksMother.Real.*;
import static org.adhuc.library.catalog.books.BooksMother.books;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
@Tag("useCase")
@DisplayName("Catalog service should")
class CatalogServiceTests {

    private CatalogService service;
    private InMemoryBooksRepository booksRepository;

    @BeforeEach
    void setUp() {
        booksRepository = new InMemoryBooksRepository();
        booksRepository.saveAll(List.of(L_ETRANGER, LA_PESTE, LA_CHUTE));
        service = new CatalogService(booksRepository);
    }

    @ParameterizedTest
    @CsvSource({
            "0, 10, it",
            "0, 10, es",
            "1, 10, en",
            "0, 100, it",
            "10, 100, en"
    })
    @DisplayName("return an empty page when the catalog is empty for language")
    void getEmptyPageNoBookInCatalog(int number, int size, String language) {
        var page = service.getPage(PageRequest.of(number, size), Locale.of(language));
        assertThat(page.isEmpty()).isTrue();
    }

    @Nested
    @DisplayName("when 106 books are in the catalog")
    class BooksInCatalogTests {

        private static final List<Book> BOOKS = new ArrayList<>();

        @BeforeAll
        static void initBooks() {
            var booksInFrenchAndEnglish = books(80, "fr", Set.of("en"));
            var booksOnlyInFrench = books(26, "fr", Set.of());
            var booksOnlyInEnglish = books(5, "en", Set.of());
            BOOKS.addAll(booksInFrenchAndEnglish);
            BOOKS.addAll(booksOnlyInFrench);
            BOOKS.addAll(booksOnlyInEnglish);
        }

        @BeforeEach
        void setUp() {
            booksRepository.saveAll(BOOKS);
        }

        @ParameterizedTest
        @CsvSource({
                "0, 10, 11, 106, fr",
                "1, 10, 11, 106, fr",
                "9, 10, 11, 106, fr",
                "0, 25, 5, 106, fr",
                "2, 25, 5, 106, fr",
                "3, 25, 5, 106, fr",
                "0, 50, 3, 106, fr",
                "1, 50, 3, 106, fr",
                "0, 100, 2, 106, fr",
                "0, 53, 2, 106, fr",
                "1, 37, 3, 106, fr",
                "0, 10, 9, 85, en",
                "1, 10, 9, 85, en",
                "0, 25, 4, 85, en",
                "0, 50, 2, 85, en"
        })
        @DisplayName("return a full page of books when requested page is not beyond the catalog size")
        void getFullPage(int number, int size, int totalPages, int totalElements, String language) {
            var page = service.getPage(PageRequest.of(number, size), Locale.of(language));
            SoftAssertions.assertSoftly(s -> {
                s.assertThat(page.isEmpty()).isFalse();
                s.assertThat(page.getNumber()).isEqualTo(number);
                s.assertThat(page.getSize()).isEqualTo(size);
                s.assertThat(page.getNumberOfElements()).isEqualTo(size);
                s.assertThat(page.getTotalPages()).isEqualTo(totalPages);
                s.assertThat(page.getTotalElements()).isEqualTo(totalElements);
            });
        }

        @ParameterizedTest
        @CsvSource({
                "10, 10, 6, 11, 106, fr",
                "4, 25, 6, 5, 106, fr",
                "2, 50, 6, 3, 106, fr",
                "1, 100, 6, 2, 106, fr",
                "2, 37, 32, 3, 106, fr",
                "8, 10, 5, 9, 85, en",
                "1, 50, 35, 2, 85, en",
                "0, 100, 85, 1, 85, en"
        })
        @DisplayName("return a partial page of books when requested page reaches the end of the catalog")
        void getPartialPage(int number, int size, int elements, int totalPages, int totalElements, String language) {
            var page = service.getPage(PageRequest.of(number, size), Locale.of(language));
            SoftAssertions.assertSoftly(s -> {
                s.assertThat(page.isEmpty()).isFalse();
                s.assertThat(page.getNumber()).isEqualTo(number);
                s.assertThat(page.getSize()).isEqualTo(size);
                s.assertThat(page.getNumberOfElements()).isEqualTo(elements);
                s.assertThat(page.getTotalPages()).isEqualTo(totalPages);
                s.assertThat(page.getTotalElements()).isEqualTo(totalElements);
            });
        }

        @ParameterizedTest
        @CsvSource({
                "11, 10, fr",
                "5, 25, fr",
                "3, 50, fr",
                "2, 100, fr",
                "2, 53, fr",
                "3, 37, fr",
                "9, 10, en",
                "3, 50, en",
                "1, 100, en"
        })
        @DisplayName("return an empty page of books when requested page is beyond the catalog size")
        void getEmptyPageBeyondSize(int number, int size, String language) {
            var page = service.getPage(PageRequest.of(number, size), Locale.of(language));
            assertThat(page.isEmpty()).isTrue();
        }

    }

}
