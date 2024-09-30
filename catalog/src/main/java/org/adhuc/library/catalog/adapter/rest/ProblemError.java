package org.adhuc.library.catalog.adapter.rest;

/**
 * A problem error, used to indicate the origin of the error in a validation problem. This can be used in combination
 * with the {@link org.springframework.hateoas.mediatype.problem.Problem} class, as:
 * <pre><code>
 *     Problem.create()
 *                 .withType(URI.create("/problems/some-problem"))
 *                 .withStatus(BAD_REQUEST)
 *                 .withTitle("Some problem")
 *                 .withDetail("Problem details")
 *                 .withProperties(map -> map.put("errors", List.of(new ParameterError("Error detail", "parameter_name"))));
 * </code></pre>
 */
public sealed interface ProblemError permits ProblemError.ParameterError, ProblemError.PointerError {
    /**
     * A parameter error, that contains:
     * <ul>
     *     <li><b>{@code detail}</b> indicates why the error occurred</li>
     *     <li><b>{@code parameter}</b> indicates which request parameter is the source of the error</li>
     * </ul>
     */
    record ParameterError(String detail, String parameter) implements ProblemError {

    }

    /**
     * A pointer error, that contains:
     * <ul>
     *     <li><b>{@code detail}</b> indicates why the error occurred</li>
     *     <li><b>{@code pointer}</b> indicates which part of the request body is the source of the error, based on
     *     <a href="https://www.rfc-editor.org/rfc/rfc6901">a JSON pointer</a></li>
     * </ul>
     */
    record PointerError(String detail, String pointer) implements ProblemError {

    }
}
