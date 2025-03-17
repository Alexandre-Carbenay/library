package org.adhuc.library.catalog.books;

import org.jmolecules.architecture.onion.classical.ApplicationServiceRing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
@ApplicationServiceRing
public class CatalogService {

    private final BooksRepository repository;

    CatalogService(BooksRepository repository) {
        this.repository = repository;
    }

    public Page<Book> getPage(Pageable request) {
        Assert.notNull(request, "Cannot get catalog page from null request");
        return repository.find(request);
    }

}
