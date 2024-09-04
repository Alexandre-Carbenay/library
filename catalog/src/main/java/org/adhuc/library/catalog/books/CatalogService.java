package org.adhuc.library.catalog.books;

import org.jmolecules.architecture.onion.classical.ApplicationServiceRing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@ApplicationServiceRing
public class CatalogService {

    private final BooksRepository repository;

    CatalogService(BooksRepository repository) {
        this.repository = repository;
    }

    public Page<Book> getPage(Pageable request) {
        return repository.find(request);
    }

}
