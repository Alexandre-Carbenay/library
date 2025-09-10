package org.adhuc.library.catalog.adapter.autoload;

import org.adhuc.library.catalog.authors.internal.InMemoryAuthorsRepository;
import org.adhuc.library.catalog.books.BooksMother;
import org.adhuc.library.catalog.books.internal.InMemoryBooksRepository;
import org.adhuc.library.catalog.editions.internal.InMemoryEditionsRepository;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

import static org.adhuc.library.catalog.authors.AuthorsMother.Real.LEON_TOLSTOI;
import static org.adhuc.library.catalog.editions.EditionsMother.Real.L_ETRANGER;

@Tag("integration")
@Tag("dataLoading")
@SpringBootTest
@TestPropertySource(properties = {
        "library.catalog.data.auto-load.enabled=true",
        "library.catalog.data.auto-load.authors.resource=classpath:auto-load/authors/authors-test-valid.json",
        "library.catalog.data.auto-load.books.resource=classpath:auto-load/books/books-test-valid.json",
        "library.catalog.data.auto-load.editions.resource=classpath:auto-load/editions/editions-test-valid.json"
})
@DisplayName("Auto-load when enabled should")
class AutoLoadEnabledTests {

    @Autowired
    private InMemoryEditionsRepository editionsRepository;
    @Autowired
    private InMemoryBooksRepository booksRepository;
    @Autowired
    private InMemoryAuthorsRepository authorsRepository;

    @Test
    @DisplayName("load authors, books and editions in repositories")
    void noDataLoaded() {
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(editionsRepository.findAll()).as("Editions should have been loaded").isNotEmpty();
            s.assertThat(editionsRepository.findByIsbn("9782070360024")).isPresent().contains(L_ETRANGER);
            s.assertThat(booksRepository.findAll()).as("Books should have been loaded").isNotEmpty();
            s.assertThat(booksRepository.findById(UUID.fromString("a24564d8-8ab9-474d-9593-8a43c45bbbad")))
                    .isPresent().contains(BooksMother.Real.L_ETRANGER);
            s.assertThat(authorsRepository.findAll()).as("Authors should have been loaded").isNotEmpty();
            s.assertThat(authorsRepository.findById(UUID.fromString("2c98db8d-d0c3-4dfb-8cfe-355256300a91")))
                    .isPresent().contains(LEON_TOLSTOI);
        });
    }

}
