package org.adhuc.library.referencing.books;

public class DuplicateBookException extends RuntimeException {

    private final int position;

    public DuplicateBookException(int position, String duplicateTitle) {
        super("Title '%s' already exists".formatted(duplicateTitle));
        this.position = position;
    }

    public int position() {
        return position;
    }

}
