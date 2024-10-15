package org.adhuc.library.catalog.adapter.autoload;

import org.adhuc.library.catalog.authors.internal.InMemoryAuthorsRepository;
import org.adhuc.library.catalog.books.internal.InMemoryBooksRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
@ConditionalOnProperty(prefix = "library.catalog.data.auto-load", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(CatalogAutoLoadProperties.class)
class CatalogAutoLoadConfiguration {

    @Bean
    InMemoryAuthorsLoader authorsLoader(InMemoryAuthorsRepository authorsRepository,
                                        CatalogAutoLoadProperties properties) {
        var loader = new InMemoryAuthorsLoader(authorsRepository, properties.authors().resource());
        loader.load();
        return loader;
    }

    @Bean
    @DependsOn("authorsLoader")
    InMemoryBooksLoader booksLoader(InMemoryBooksRepository booksRepository,
                                    InMemoryAuthorsRepository authorsRepository,
                                    CatalogAutoLoadProperties properties) {
        var loader = new InMemoryBooksLoader(booksRepository, authorsRepository, properties.books().resource());
        loader.load();
        return loader;
    }

}
