package org.adhuc.library.catalog.editions;

import org.adhuc.library.catalog.editions.internal.InMemoryEditionsRepository;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.data.domain.PageRequest;

import static org.adhuc.library.catalog.editions.EditionsMother.editions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Catalog service should")
class CatalogServiceTests {

    private CatalogService service;
    private InMemoryEditionsRepository editionsRepository;

    @BeforeEach
    void setUp() {
        editionsRepository = new InMemoryEditionsRepository();
        service = new CatalogService(editionsRepository);
    }

    @Test
    @DisplayName("refuse getting page from null request")
    void errorGetPageNull() {
        assertThrows(IllegalArgumentException.class, () -> service.getPage(null));
    }

    @ParameterizedTest
    @CsvSource({
            "0, 10",
            "1, 10",
            "0, 100",
            "10, 100"
    })
    @DisplayName("return an empty page when the catalog is empty")
    void getEmptyPageNoEditionInCatalog(int number, int size) {
        var page = service.getPage(PageRequest.of(number, size));
        assertThat(page.isEmpty()).isTrue();
    }

    @Nested
    @DisplayName("when 106 editions are in the catalog")
    class EditionsInCatalogTests {

        @BeforeEach
        void setUp() {
            editionsRepository.saveAll(editions().list().ofSize(106).sample());
        }

        @ParameterizedTest
        @CsvSource({
                "0, 10, 11, 106",
                "1, 10, 11, 106",
                "9, 10, 11, 106",
                "0, 25, 5, 106",
                "2, 25, 5, 106",
                "3, 25, 5, 106",
                "0, 50, 3, 106",
                "1, 50, 3, 106",
                "0, 100, 2, 106",
                "0, 53, 2, 106",
                "1, 37, 3, 106"
        })
        @DisplayName("return a full page of editions when requested page is not beyond the catalog size")
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
                "10, 10, 6, 11, 106",
                "4, 25, 6, 5, 106",
                "2, 50, 6, 3, 106",
                "1, 100, 6, 2, 106",
                "2, 37, 32, 3, 106"
        })
        @DisplayName("return a partial page of editions when requested page reaches the end of the catalog")
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
                "11, 10",
                "5, 25",
                "3, 50",
                "2, 100",
                "2, 53",
                "3, 37"
        })
        @DisplayName("return an empty page of editions when requested page is beyond the catalog size")
        void getEmptyPageBeyondSize(int number, int size) {
            var page = service.getPage(PageRequest.of(number, size));
            assertThat(page.isEmpty()).isTrue();
        }

    }

}
