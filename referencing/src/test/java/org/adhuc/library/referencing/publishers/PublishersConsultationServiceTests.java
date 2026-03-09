package org.adhuc.library.referencing.publishers;

import org.adhuc.library.referencing.publishers.internal.InMemoryPublishersRepository;
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

import static org.adhuc.library.referencing.publishers.PublishersMother.publishers;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Publishers consultation service should")
class PublishersConsultationServiceTests {

    private InMemoryPublishersRepository publishersRepository;
    private PublishersConsultationService service;

    @BeforeEach
    void setUp() {
        publishersRepository = new InMemoryPublishersRepository();
        service = new PublishersConsultationService(publishersRepository);
    }

    @ParameterizedTest
    @CsvSource({
            "0, 10",
            "0, 50",
            "10, 50"
    })
    @DisplayName("return an empty page when no publisher is referenced yet")
    void getEmptyPageNoPublisherReferenced(int number, int size) {
        var page = service.getPage(PageRequest.of(number, size));
        assertThat(page.isEmpty()).isTrue();
    }

    @Nested
    @DisplayName("when 65 publishers have been referenced")
    class PublishersInCatalogTests {

        private static final List<Publisher> PUBLISHERS = new ArrayList<>();

        @BeforeAll
        static void initPublishers() {
            var publishers = publishers(65);
            PUBLISHERS.addAll(publishers);
        }

        @BeforeEach
        void setUp() {
            publishersRepository.saveAll(PUBLISHERS);
        }

        @ParameterizedTest
        @CsvSource({
                "0, 10, 7, 65",
                "1, 10, 7, 65",
                "4, 10, 7, 65",
                "0, 25, 3, 65",
                "1, 25, 3, 65",
                "0, 50, 2, 65",
                "0, 65, 1, 65",
                "0, 32, 3, 65"
        })
        @DisplayName("return a full page of publishers when requested page is not beyond the total number of referenced publishers")
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
                "6, 10, 5, 7, 65",
                "2, 25, 15, 3, 65",
                "1, 50, 15, 2, 65",
                "0, 100, 65, 1, 65",
                "1, 48, 17, 2, 65"
        })
        @DisplayName("return a partial page of publishers when requested page reaches the total number of referenced publishers")
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
                "7, 10",
                "3, 25",
                "2, 50",
                "1, 100",
                "2, 65",
                "3, 33"
        })
        @DisplayName("return an empty page of publishers when requested page is beyond the total number of referenced publishers")
        void getEmptyPageBeyondSize(int number, int size) {
            var page = service.getPage(PageRequest.of(number, size));
            assertThat(page.isEmpty()).isTrue();
        }

        @ParameterizedTest
        @CsvSource({
                "7, 10",
                "3, 25",
                "2, 50",
                "1, 100"
        })
        @DisplayName("provide the complete list of publishers when browsing all the pages")
        void browsePagesAllPublishers(int numberOfPages, int pageSize) {
            var publishers = new HashSet<Publisher>();
            for (int pageNumber = 0; pageNumber < numberOfPages; pageNumber++) {
                publishers.addAll(service.getPage(PageRequest.of(pageNumber, pageSize)).getContent());
            }
            assertThat(publishers.size()).isEqualTo(65);
        }

    }

}
