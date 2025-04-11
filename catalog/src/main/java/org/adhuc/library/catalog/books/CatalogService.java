package org.adhuc.library.catalog.books;

import org.jmolecules.architecture.onion.classical.ApplicationServiceRing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Locale;

@Service
@ApplicationServiceRing
public class CatalogService {

    private final BooksRepository repository;

    CatalogService(BooksRepository repository) {
        this.repository = repository;
    }

    public Page<Book> getPage(Pageable request, Locale language) {
        Assert.notNull(request, "Cannot get catalog page from null request");
        Assert.notNull(language, "Cannot get catalog page for null language");
        return repository.findByLanguage(language, request);
    }

}
