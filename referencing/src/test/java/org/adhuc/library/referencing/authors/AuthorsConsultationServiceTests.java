package org.adhuc.library.referencing.authors;

import org.adhuc.library.referencing.authors.internal.InMemoryAuthorsRepository;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.adhuc.library.referencing.authors.AuthorsMother.authors;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Authors consultation service should")
class AuthorsConsultationServiceTests {

    private InMemoryAuthorsRepository authorsRepository;
    private AuthorsConsultationService service;

    @BeforeEach
    void setUp() {
        authorsRepository = new InMemoryAuthorsRepository();
        service = new AuthorsConsultationService(authorsRepository);
    }

    @ParameterizedTest
    @CsvSource({
            "0, 10",
            "0, 50",
            "10, 50"
    })
    @DisplayName("return an empty page when no author is referenced yet")
    void getEmptyPageNoAuthorReferenced(int number, int size) {
        var page = service.getPage(PageRequest.of(number, size));
        assertThat(page.isEmpty()).isTrue();
    }

    @Nested
    @DisplayName("when 53 authors have been referenced")
    class BooksInCatalogTests {

        private static final List<Author> AUTHORS = new ArrayList<>();

        @BeforeAll
        static void initAuthors() {
            var authors = authors(53);
            AUTHORS.addAll(authors);
        }

        @BeforeEach
        void setUp() {
            authorsRepository.saveAll(AUTHORS);
        }

        @ParameterizedTest
        @CsvSource({
                "0, 10, 6, 53",
                "1, 10, 6, 53",
                "4, 10, 6, 53",
                "0, 25, 3, 53",
                "1, 25, 3, 53",
                "0, 50, 2, 53",
                "0, 53, 1, 53",
                "0, 37, 2, 53"
        })
        @DisplayName("return a full page of authors when requested page is not beyond the total number of referenced authors")
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
                "5, 10, 3, 6, 53",
                "2, 25, 3, 3, 53",
                "1, 50, 3, 2, 53",
                "0, 100, 53, 1, 53",
                "1, 37, 16, 2, 53"
        })
        @DisplayName("return a partial page of authors when requested page reaches the total number of referenced authors")
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
                "6, 10",
                "3, 25",
                "2, 50",
                "1, 100",
                "1, 53",
                "2, 37"
        })
        @DisplayName("return an empty page of books when requested page is beyond the total number of referenced authors")
        void getEmptyPageBeyondSize(int number, int size) {
            var page = service.getPage(PageRequest.of(number, size));
            assertThat(page.isEmpty()).isTrue();
        }

        @ParameterizedTest
        @CsvSource({
                "6, 10",
                "3, 25",
                "2, 50",
                "1, 100"
        })
        @DisplayName("provide the complete list of authors when browsing all the pages")
        void browsePagesAllAuthors(int numberOfPages, int pageSize) {
            var authors = new HashSet<Author>();
            for (int pageNumber = 0; pageNumber < numberOfPages; pageNumber++) {
                authors.addAll(service.getPage(PageRequest.of(pageNumber, pageSize)).getContent());
            }
            assertThat(authors.size()).isEqualTo(53);
        }

    }

}
