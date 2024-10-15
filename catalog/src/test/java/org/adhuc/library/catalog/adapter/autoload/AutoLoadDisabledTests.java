package org.adhuc.library.catalog.adapter.autoload;

import org.adhuc.library.catalog.authors.internal.InMemoryAuthorsRepository;
import org.adhuc.library.catalog.books.internal.InMemoryBooksRepository;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "library.catalog.data.auto-load.enabled=false"
})
@DisplayName("Auto-load when disabled should")
class AutoLoadDisabledTests {

    @Autowired
    private InMemoryBooksRepository booksRepository;
    @Autowired
    private InMemoryAuthorsRepository authorsRepository;

    @Test
    @DisplayName("not load any author nor book in repositories")
    void noDataLoaded() {
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(booksRepository.findAll()).as("No book should have been loaded").isEmpty();
            s.assertThat(authorsRepository.findAll()).as("No authors should have been loaded").isEmpty();
        });
    }

}
