package org.adhuc.library.website;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpStatusCodeException;

import java.net.URI;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

@ControllerAdvice
public class GeneralAdvice {

    @ExceptionHandler(TimeoutException.class)
    public String handleTimeoutException(Model model, TimeoutException exception) {
        model.addAttribute("message", exception.getMessage());
        return "error";
    }

    @ExceptionHandler(HttpStatusCodeException.class)
    public String handleHttpStatusCodeException(Model model, HttpStatusCodeException exception) {
        var responseBody = Objects.requireNonNull(exception.getResponseBodyAs(Problem.class));
        model.addAttribute("message", responseBody.detail);
        return "error";
    }

    private static class Problem {
        private final URI type;
        private final String title;
        private final HttpStatus status;
        private final String detail;
        private final URI instance;

        @JsonCreator
        private Problem(@JsonProperty("type") URI type, @JsonProperty("title") String title, @JsonProperty("status") int status, @JsonProperty("detail") String detail, @JsonProperty("instance") URI instance) {
            this.type = type;
            this.title = title;
            this.status = Objects.requireNonNull(HttpStatus.resolve(status));
            this.detail = detail;
            this.instance = instance;
        }
    }

}
