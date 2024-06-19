package org.adhuc.library.catalog.books;

import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class BooksService {

    public Optional<Book> getBook(UUID id) {
        return Optional.empty();
    }

}
