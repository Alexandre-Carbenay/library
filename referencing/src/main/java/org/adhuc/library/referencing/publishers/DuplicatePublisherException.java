package org.adhuc.library.referencing.publishers;

public class DuplicatePublisherException extends RuntimeException {

    public DuplicatePublisherException(String duplicateName) {
        super("Publisher '%s' already exists".formatted(duplicateName));
    }

}
