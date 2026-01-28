package org.adhuc.library.referencing.books;

import org.jmolecules.architecture.onion.classical.ApplicationServiceRing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@ApplicationServiceRing
public class BooksConsultationService {

    private final List<Book> books = new ArrayList<>();

    public Page<Book> getPage(Pageable request) {
        return books.isEmpty() ? Page.<Book>empty() : new PageImpl<>(books, request, books.size());
    }

}
