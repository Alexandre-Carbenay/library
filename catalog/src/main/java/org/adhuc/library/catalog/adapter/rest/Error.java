package org.adhuc.library.catalog.adapter.rest;

import org.springframework.util.Assert;

import java.time.ZonedDateTime;
import java.util.List;

public record Error(ZonedDateTime timestamp, int status, String error, String description, List<? extends ErrorSource> sources) {

    public Error(ZonedDateTime timestamp, int status, String error, String description) {
        this(timestamp, status, error, description, List.of());
    }

    public Error {
        Assert.notNull(sources, "Error sources cannot be null");
    }

    /**
     * An error source, used to indicate the source of the error in the incoming request.
     */
    public sealed interface ErrorSource permits ParameterError, PointerError {

    }

    /**
     * A parameter error source, that contains:
     * <ul>
     *     <li><b>{@code reason}</b> indicates why the error occurred</li>
     *     <li><b>{@code parameter}</b> indicates which request parameter is the source of the error</li>
     * </ul>
     */
    public record ParameterError(String reason, String parameter) implements ErrorSource {

    }

    /**
     * A pointer error source, that contains:
     * <ul>
     *     <li><b>{@code reason}</b> indicates why the error occurred</li>
     *     <li><b>{@code pointer}</b> indicates which part of the request body is the source of the error, based on
     *     <a href="https://www.rfc-editor.org/rfc/rfc6901">a JSON pointer</a></li>
     * </ul>
     */
    public record PointerError(String reason, String pointer) implements ErrorSource {

    }

}
