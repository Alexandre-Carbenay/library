package org.adhuc.library.catalog.adapter.autoload;

import org.adhuc.library.catalog.authors.internal.InMemoryAuthorsRepository;
import org.adhuc.library.catalog.editions.internal.InMemoryEditionsRepository;
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
    private InMemoryEditionsRepository editionsRepository;
    @Autowired
    private InMemoryAuthorsRepository authorsRepository;

    @Test
    @DisplayName("not load any author nor edition in repositories")
    void noDataLoaded() {
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(editionsRepository.findAll()).as("No editions should have been loaded").isEmpty();
            s.assertThat(authorsRepository.findAll()).as("No authors should have been loaded").isEmpty();
        });
    }

}
