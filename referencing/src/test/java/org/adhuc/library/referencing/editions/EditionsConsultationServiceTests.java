package org.adhuc.library.referencing.editions;

import org.adhuc.library.referencing.editions.internal.InMemoryEditionsRepository;
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

import static org.adhuc.library.referencing.editions.EditionsMother.editions;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Editions consultation service should")
class EditionsConsultationServiceTests {

    private InMemoryEditionsRepository editionsRepository;
    private EditionsConsultationService service;

    @BeforeEach
    void setUp() {
        editionsRepository = new InMemoryEditionsRepository();
        service = new EditionsConsultationService(editionsRepository);
    }

    @ParameterizedTest
    @CsvSource({
            "0, 10",
            "0, 50",
            "10, 50"
    })
    @DisplayName("return an empty page when no edition is referenced yet")
    void getEmptyPageNoEditionReferenced(int number, int size) {
        var page = service.getPage(PageRequest.of(number, size));
        assertThat(page.isEmpty()).isTrue();
    }

    @Nested
    @DisplayName("when 83 editions have been referenced")
    class EditionsInCatalogTests {

        private static final List<Edition> EDITIONS = new ArrayList<>();

        @BeforeAll
        static void initEditions() {
            var editions = editions(83);
            EDITIONS.addAll(editions);
        }

        @BeforeEach
        void setUp() {
            editionsRepository.saveAll(EDITIONS);
        }

        @ParameterizedTest
        @CsvSource({
                "0, 10, 9, 83",
                "1, 10, 9, 83",
                "7, 10, 9, 83",
                "0, 25, 4, 83",
                "1, 25, 4, 83",
                "0, 50, 2, 83",
                "0, 83, 1, 83",
                "0, 41, 3, 83"
        })
        @DisplayName("return a full page of editions when requested page is not beyond the total number of referenced editions")
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
                "8, 10, 3, 9, 83",
                "3, 25, 8, 4, 83",
                "1, 50, 33, 2, 83",
                "0, 100, 83, 1, 83",
                "1, 42, 41, 2, 83"
        })
        @DisplayName("return a partial page of editions when requested page reaches the total number of referenced editions")
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
                "9, 10",
                "4, 25",
                "2, 50",
                "1, 100",
                "2, 83",
                "3, 41"
        })
        @DisplayName("return an empty page of editions when requested page is beyond the total number of referenced editions")
        void getEmptyPageBeyondSize(int number, int size) {
            var page = service.getPage(PageRequest.of(number, size));
            assertThat(page.isEmpty()).isTrue();
        }

        @ParameterizedTest
        @CsvSource({
                "9, 10",
                "4, 25",
                "2, 50",
                "1, 100"
        })
        @DisplayName("provide the complete list of editions when browsing all the pages")
        void browsePagesAllEditions(int numberOfPages, int pageSize) {
            var editions = new HashSet<Edition>();
            for (int pageNumber = 0; pageNumber < numberOfPages; pageNumber++) {
                editions.addAll(service.getPage(PageRequest.of(pageNumber, pageSize)).getContent());
            }
            assertThat(editions.size()).isEqualTo(83);
        }

    }

}
