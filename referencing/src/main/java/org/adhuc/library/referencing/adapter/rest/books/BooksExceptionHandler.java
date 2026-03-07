package org.adhuc.library.referencing.adapter.rest.books;

import org.adhuc.library.referencing.books.DuplicateBookException;
import org.adhuc.library.support.rest.ProblemError;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;

@RestControllerAdvice
class BooksExceptionHandler {

    @ExceptionHandler(DuplicateBookException.class)
    ResponseEntity<Object> handleDuplicateBook(DuplicateBookException e, WebRequest request) {
        var problem = ProblemDetail.forStatus(CONFLICT);
        problem.setType(URI.create("/problems/duplicate-book"));
        problem.setTitle("Duplicate book");
        problem.setDetail("Book with title already exists");
        problem.setProperty("errors", List.of(new ProblemError.PointerError(requireNonNull(e.getMessage()), "/details/%d/title".formatted(e.position()))));
        return ResponseEntity.status(CONFLICT).contentType(APPLICATION_PROBLEM_JSON).body(problem);
    }

}
