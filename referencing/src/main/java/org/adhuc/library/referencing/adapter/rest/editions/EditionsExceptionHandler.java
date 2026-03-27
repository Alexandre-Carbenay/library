package org.adhuc.library.referencing.adapter.rest.editions;

import org.adhuc.library.referencing.editions.DuplicateEditionException;
import org.adhuc.library.referencing.editions.UnknownEditionBookException;
import org.adhuc.library.referencing.editions.UnknownEditionPublisherException;
import org.adhuc.library.support.rest.ProblemError;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;

@RestControllerAdvice
class EditionsExceptionHandler {

    @ExceptionHandler(DuplicateEditionException.class)
    ResponseEntity<Object> handleDuplicateEdition(DuplicateEditionException e, WebRequest request) {
        var problem = ProblemDetail.forStatus(CONFLICT);
        problem.setType(URI.create("/problems/duplicate-edition"));
        problem.setTitle("Duplicate edition");
        problem.setDetail("Edition with ISBN already exists");
        problem.setProperty("errors", List.of(new ProblemError.PointerError(requireNonNull(e.getMessage()), "/isbn")));
        return ResponseEntity.status(CONFLICT).contentType(APPLICATION_PROBLEM_JSON).body(problem);
    }

    @ExceptionHandler(UnknownEditionBookException.class)
    ResponseEntity<Object> handleUnknownEditionBook(UnknownEditionBookException e, WebRequest request) {
        var problem = ProblemDetail.forStatus(BAD_REQUEST);
        problem.setType(URI.create("/problems/unknown-edition-book"));
        problem.setTitle("Unknown edition book");
        problem.setDetail("Referenced edition's book does not exist");
        problem.setProperty("errors", List.of(new ProblemError.PointerError(requireNonNull(e.getMessage()), "/book")));
        return ResponseEntity.status(BAD_REQUEST).contentType(APPLICATION_PROBLEM_JSON).body(problem);
    }

    @ExceptionHandler(UnknownEditionPublisherException.class)
    ResponseEntity<Object> handleUnknownEditionPublisher(UnknownEditionPublisherException e, WebRequest request) {
        var problem = ProblemDetail.forStatus(BAD_REQUEST);
        problem.setType(URI.create("/problems/unknown-edition-publisher"));
        problem.setTitle("Unknown edition publisher");
        problem.setDetail("Referenced edition's publisher does not exist");
        problem.setProperty("errors", List.of(new ProblemError.PointerError(requireNonNull(e.getMessage()), "/publisher")));
        return ResponseEntity.status(BAD_REQUEST).contentType(APPLICATION_PROBLEM_JSON).body(problem);
    }

}
