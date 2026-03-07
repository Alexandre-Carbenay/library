package org.adhuc.library.referencing.books;

import org.adhuc.library.referencing.books.internal.InMemoryBooksRepository;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.adhuc.library.referencing.books.BooksMother.books;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Books consultation service should")
class BooksConsultationServiceTests {

    private InMemoryBooksRepository booksRepository;
    private BooksConsultationService service;

    @BeforeEach
    void setUp() {
        booksRepository = new InMemoryBooksRepository();
        service = new BooksConsultationService(booksRepository);
    }

    @ParameterizedTest
    @CsvSource({
            "0, 10",
            "0, 50",
            "10, 50"
    })
    @DisplayName("return an empty page when no book is referenced yet")
    void getEmptyPageNoBookReferenced(int number, int size) {
        var page = service.getPage(PageRequest.of(number, size));
        assertThat(page.isEmpty()).isTrue();
    }

    @Nested
    @DisplayName("when 74 books have been referenced")
    class BooksInCatalogTests {

        private static final List<Book> BOOKS = new ArrayList<>();

        @BeforeAll
        static void initBooks() {
            var books = books(74);
            BOOKS.addAll(books);
        }

        @BeforeEach
        void setUp() {
            booksRepository.saveAll(BOOKS);
        }

        @ParameterizedTest
        @CsvSource({
                "0, 10, 8, 74",
                "1, 10, 8, 74",
                "4, 10, 8, 74",
                "0, 25, 3, 74",
                "1, 25, 3, 74",
                "0, 50, 2, 74",
                "0, 74, 1, 74",
                "0, 37, 2, 74"
        })
        @DisplayName("return a full page of books when requested page is not beyond the total number of referenced books")
        void getFullPage(int number, int size, int totalPages, int totalElements) {
            var page = service.getPage(PageRequest.of(number, size));
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
                "7, 10, 4, 8, 74",
                "2, 25, 24, 3, 74",
                "1, 50, 24, 2, 74",
                "0, 100, 74, 1, 74",
                "1, 44, 30, 2, 74"
        })
        @DisplayName("return a partial page of books when requested page reaches the total number of referenced books")
        void getPartialPage(int number, int size, int elements, int totalPages, int totalElements) {
            var page = service.getPage(PageRequest.of(number, size));
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
                "8, 10",
                "3, 25",
                "2, 50",
                "1, 100",
                "2, 74",
                "2, 37"
        })
        @DisplayName("return an empty page of books when requested page is beyond the total number of referenced books")
        void getEmptyPageBeyondSize(int number, int size) {
            var page = service.getPage(PageRequest.of(number, size));
            assertThat(page.isEmpty()).isTrue();
        }

        @ParameterizedTest
        @CsvSource({
                "8, 10",
                "4, 25",
                "2, 50",
                "1, 100"
        })
        @DisplayName("provide the complete list of books when browsing all the pages")
        void browsePagesAllBooks(int numberOfPages, int pageSize) {
            var books = new HashSet<Book>();
            for (int pageNumber = 0; pageNumber < numberOfPages; pageNumber++) {
                books.addAll(service.getPage(PageRequest.of(pageNumber, pageSize)).getContent());
            }
            assertThat(books.size()).isEqualTo(74);
        }

    }

}
