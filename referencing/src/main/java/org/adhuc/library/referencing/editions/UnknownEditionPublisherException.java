package org.adhuc.library.referencing.editions;

import java.util.UUID;

public class UnknownEditionPublisherException extends RuntimeException {

    public UnknownEditionPublisherException(String isbn, UUID publisherId) {
        super("Edition with ISBN '%s' cannot be referenced for unknown publisher '%s'".formatted(isbn, publisherId));
    }

}
