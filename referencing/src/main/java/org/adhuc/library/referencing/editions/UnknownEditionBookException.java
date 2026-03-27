package org.adhuc.library.referencing.editions;

import java.util.UUID;

public class UnknownEditionBookException extends RuntimeException {

    public UnknownEditionBookException(String isbn, UUID bookId) {
        super("Edition with ISBN '%s' cannot be referenced for unknown book '%s'".formatted(isbn, bookId));
    }

}
