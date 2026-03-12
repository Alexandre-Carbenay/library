package org.adhuc.library.referencing.books;

import org.jmolecules.architecture.onion.classical.ApplicationServiceRing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@ApplicationServiceRing
public class BooksConsultationService {

    private final BooksRepository booksRepository;

    public BooksConsultationService(BooksRepository booksRepository) {
        this.booksRepository = booksRepository;
    }

    public Page<Book> getPage(Pageable request) {
        return booksRepository.findPage(request);
    }

}
