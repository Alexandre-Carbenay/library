package org.adhuc.library.referencing.editions;

public class DuplicateEditionException extends RuntimeException {

    public DuplicateEditionException(String duplicateIsbn) {
        super("Edition with ISBN '%s' already exists".formatted(duplicateIsbn));
    }

}
