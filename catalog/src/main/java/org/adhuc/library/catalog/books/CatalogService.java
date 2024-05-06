package org.adhuc.library.catalog.books;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CatalogService {

    public Page<Book> getPage(Pageable request) {
        return Page.empty(request);
    }

}
