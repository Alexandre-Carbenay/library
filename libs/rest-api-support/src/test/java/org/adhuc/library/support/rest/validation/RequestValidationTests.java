package org.adhuc.library.support.rest.validation;

import jakarta.validation.Valid;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;
import java.util.stream.Stream;

import static org.adhuc.library.support.rest.validation.TestRequest.TestChild.testChild;
import static org.adhuc.library.support.rest.validation.TestRequest.testRequest;
import static org.adhuc.library.support.rest.validation.WithNullableElementRequest.WithNullableChild.nullableChild;
import static org.adhuc.library.support.rest.validation.WithNullableElementRequest.nullableElementRequest;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("unused")
@Tag("integration")
@Tag("restApi")
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "management.endpoints.web.base-path=/management-custom",
        "management.endpoints.web.exposure.include=health,beans,info",
        "org.adhuc.library.support.rest.validation.open-api.location=classpath:validation/openapi-test.yml",
        "org.adhuc.library.support.rest.validation.jsr303.enabled=true",
        "org.adhuc.library.support.rest.validation.jsr303.message-basename=classpath:validation/messages-test"
})
@DisplayName("Request validation should")
class RequestValidationTests {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private JsonMapper mapper;

    @ParameterizedTest
    @ValueSource(strings = {
            "/health",
            "/info",
            "/beans"
    })
    @DisplayName("ignore management paths")
    void managementPaths(String path) throws Exception {
        mvc.perform(get("/management-custom" + path))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("not allow accessing path not specified in openapi endpoints")
    void unknownPath() throws Exception {
        mvc.perform(get("/api/v1/unknown"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("respond successfully when get request is valid")
    void respond200OnValidGetRequest() throws Exception {
        mvc.perform(
                get("/test/request")
                        .queryParam("required_parameter", "parameter1")
                        .header("Required-Header", "header1")
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("missingRequiredParameter")
    @DisplayName("respond with error detail on get request with missing required parameter")
    void respond400MissingRequiredParameter(String name, RequestBuilder requestBuilder, String errorDetail) throws Exception {
        var result = mvc.perform(requestBuilder).andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"));

        result
                .andExpect(jsonPath("type", equalTo("/problems/invalid-request")))
                .andExpect(jsonPath("status", equalTo(400)))
                .andExpect(jsonPath("title", equalTo("Request validation error")))
                .andExpect(jsonPath("detail", equalTo("Request parameters or body are invalid compared to the OpenAPI specification. See errors for more information")))
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0].detail", equalTo(errorDetail)))
                .andExpect(jsonPath("errors[0].parameter", equalTo("required_parameter")))
                .andExpect(jsonPath("errors[0].pointer").doesNotExist());
    }

    static Stream<Arguments> missingRequiredParameter() {
        return Stream.of(
                Arguments.of(
                        "Missing required parameter",
                        get("/test/request")
                                .header("Required-Header", "header1")
                                .accept(MediaType.APPLICATION_JSON),
                        "Query parameter 'required_parameter' is required on path '/test/request' but not found in request."
                ),
                Arguments.of(
                        "Empty required parameter",
                        get("/test/request")
                                .queryParam("required_parameter", "")
                                .header("Required-Header", "header1")
                                .accept(MediaType.APPLICATION_JSON),
                        "Parameter 'required_parameter' is required but is missing."
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("missingRequiredHeader")
    @DisplayName("respond with error detail on get request with missing required header")
    void respond400MissingRequiredHeader(String name, RequestBuilder requestBuilder, String errorDetail) throws Exception {
        var result = mvc.perform(requestBuilder).andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"));

        result
                .andExpect(jsonPath("type", equalTo("/problems/invalid-request")))
                .andExpect(jsonPath("status", equalTo(400)))
                .andExpect(jsonPath("title", equalTo("Request validation error")))
                .andExpect(jsonPath("detail", equalTo("Request parameters or body are invalid compared to the OpenAPI specification. See errors for more information")))
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0].detail", equalTo(errorDetail)))
                .andExpect(jsonPath("errors[0].parameter", equalTo("Required-Header")))
                .andExpect(jsonPath("errors[0].pointer").doesNotExist());
    }

    static Stream<Arguments> missingRequiredHeader() {
        return Stream.of(
                Arguments.of(
                        "Missing required header",
                        get("/test/request")
                                .queryParam("required_parameter", "parameter1")
                                .accept(MediaType.APPLICATION_JSON),
                        "Header parameter 'Required-Header' is required on path '/test/request' but not found in request."
                ),
                Arguments.of(
                        "Empty required header",
                        get("/test/request")
                                .queryParam("required_parameter", "parameter1")
                                .header("Required-Header", "")
                                .accept(MediaType.APPLICATION_JSON),
                        "Parameter 'Required-Header' is required but is missing."
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("validRequestProvider")
    @DisplayName("respond successfully when post request is valid")
    void respond201OnValidPostRequest(String name, TestRequest request) throws Exception {
        mvc.perform(
                post("/test/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
        ).andExpect(status().isCreated());
    }

    static Stream<Arguments> validRequestProvider() {
        return Stream.of(
                Arguments.of("Random valid request", testRequest()),
                Arguments.of(
                        "Valid request without conditional value when condition is not met",
                        testRequest().requiredBoolean(false).requiredIfRequiredBooleanIsTrue(null)
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource({
            "requiredStringValidationErrorProvider",
            "requiredIntValidationErrorProvider",
            "requiredBooleanValidationErrorProvider",
            "requiredDateValidationErrorProvider",
            "requiredUuidValidationErrorProvider",
            "requiredArrayValidationErrorProvider",
            "requiredChildValidationErrorProvider",
            "jsr303ValidationErrorProvider"
    })
    @DisplayName("respond with error detail on request body validation error")
    void respond400OnRequestValidationError(String name, TestRequest request, String expectedDetail, String expectedPointer) throws Exception {
        var result = mvc.perform(
                        post("/test/request")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(request))
                ).andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"));

        result
                .andExpect(jsonPath("type", equalTo("/problems/invalid-request")))
                .andExpect(jsonPath("status", equalTo(400)))
                .andExpect(jsonPath("title", equalTo("Request validation error")))
                .andExpect(jsonPath("detail", equalTo("Request parameters or body are invalid compared to the OpenAPI specification. See errors for more information")))
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0].detail", equalTo(expectedDetail)))
                .andExpect(jsonPath("errors[0].parameter").doesNotExist())
                .andExpect(jsonPath("errors[0].pointer", equalTo(expectedPointer)));
    }

    static Stream<Arguments> requiredStringValidationErrorProvider() {
        var tooShortRequiredStringRequest = testRequest().tooShortRequiredString();
        var tooShort = tooShortRequiredStringRequest.requiredString();
        var tooLongRequiredStringRequest = testRequest().tooLongRequiredString();
        var tooLong = tooLongRequiredStringRequest.requiredString();

        return Stream.of(
                Arguments.of("Required string is not present",
                        testRequest().requiredString(null),
                        "Missing required property",
                        "/required_string"
                ),
                Arguments.of("Required string is too short",
                        tooShortRequiredStringRequest,
                        "String \"" + tooShort + "\" is too short (length: " + tooShort.length() + ", required minimum: 5)",
                        "/required_string"
                ),
                Arguments.of("Required string is too long",
                        tooLongRequiredStringRequest,
                        "String \"" + tooLong + "\" is too long (length: " + tooLong.length() + ", maximum allowed: 100)",
                        "/required_string"
                ),
                Arguments.of("Required string is not a string",
                        testRequest().wrongTypeRequiredString(),
                        "Instance type (integer) does not match any allowed primitive type (allowed: [\"string\"])",
                        "/required_string"
                )
        );
    }

    static Stream<Arguments> requiredIntValidationErrorProvider() {
        var tooLowRequiredIntRequest = testRequest().tooLowRequiredInt();
        var tooLow = tooLowRequiredIntRequest.requiredInt();
        var tooHighRequiredIntRequest = testRequest().tooHighRequiredInt();
        var tooHigh = tooHighRequiredIntRequest.requiredInt();

        return Stream.of(
                Arguments.of("Required integer is not present",
                        testRequest().requiredInt(null),
                        "Missing required property",
                        "/required_int"
                ),
                Arguments.of("Required integer is lower than minimum",
                        tooLowRequiredIntRequest,
                        "Numeric instance is lower than the required minimum (minimum: 10, found: " + tooLow + ")",
                        "/required_int"
                ),
                Arguments.of("Required integer is higher than minimum",
                        tooHighRequiredIntRequest,
                        "Numeric instance is greater than the required maximum (maximum: 20, found: " + tooHigh + ")",
                        "/required_int"
                ),
                Arguments.of("Required integer is not an integer",
                        testRequest().wrongTypeRequiredInt(),
                        "Instance type (string) does not match any allowed primitive type (allowed: [\"integer\"])",
                        "/required_int"
                )
        );
    }

    static Stream<Arguments> requiredBooleanValidationErrorProvider() {
        return Stream.of(
                Arguments.of("Required boolean is not present",
                        testRequest().requiredBoolean(null),
                        "Missing required property",
                        "/required_boolean"
                ),
                Arguments.of("Required boolean is not a boolean",
                        testRequest().wrongTypeRequiredBoolean(),
                        "Instance type (string) does not match any allowed primitive type (allowed: [\"boolean\"])",
                        "/required_boolean"
                )
        );
    }

    static Stream<Arguments> requiredDateValidationErrorProvider() {
        var invalidRequiredDateFormatRequest = testRequest().invalidRequiredDateFormat();
        var invalidDate = invalidRequiredDateFormatRequest.requiredDate();

        return Stream.of(
                Arguments.of("Required date is not present",
                        testRequest().requiredDate(null),
                        "Missing required property",
                        "/required_date"
                ),
                Arguments.of("Required date is not a date",
                        testRequest().wrongTypeRequiredDate(),
                        "Instance type (integer) does not match any allowed primitive type (allowed: [\"string\"])",
                        "/required_date"
                ),
                Arguments.of("Required date has not date format",
                        invalidRequiredDateFormatRequest,
                        "String \"" + invalidDate + "\" is invalid against requested date format(s) yyyy-MM-dd",
                        "/required_date"
                )
        );
    }

    static Stream<Arguments> requiredUuidValidationErrorProvider() {
        var invalidRequiredUuidFormatRequest = testRequest().invalidRequiredUuidFormat();
        var invalidUuid = invalidRequiredUuidFormatRequest.requiredUuid();

        return Stream.of(
                Arguments.of("Required UUID is not present",
                        testRequest().requiredUuid(null),
                        "Missing required property",
                        "/required_uuid"
                ),
                Arguments.of("Required UUID is not a UUID",
                        testRequest().wrongTypeRequiredUuid(),
                        "Instance type (integer) does not match any allowed primitive type (allowed: [\"string\"])",
                        "/required_uuid"
                ),
                Arguments.of("Required UUID has not UUID format",
                        invalidRequiredUuidFormatRequest,
                        "Input string \"" + invalidUuid + "\" is not a valid UUID",
                        "/required_uuid"
                )
        );
    }

    static Stream<Arguments> requiredArrayValidationErrorProvider() {
        var tooFewElementsRequiredArrayRequest = testRequest().tooFewRequiredArrayElements();
        var tooFewElements = tooFewElementsRequiredArrayRequest.requiredArray().size();
        var tooManyElementsRequiredArrayRequest = testRequest().tooManyRequiredArrayElements();
        var tooManyElements = tooManyElementsRequiredArrayRequest.requiredArray().size();

        return Stream.of(
                Arguments.of("Required array is not present",
                        testRequest().requiredArray(null),
                        "Missing required property",
                        "/required_array"
                ),
                Arguments.of("Required array has too few elements",
                        tooFewElementsRequiredArrayRequest,
                        "Array is too short: must have at least 2 elements but instance has " + tooFewElements + " elements",
                        "/required_array"
                ),
                Arguments.of("Required array has too many elements",
                        tooManyElementsRequiredArrayRequest,
                        "Array is too long: must have at most 5 elements but instance has " + tooManyElements + " elements",
                        "/required_array"
                ),
                Arguments.of("Required array has duplicate elements",
                        testRequest().duplicateRequiredArrayElements(),
                        "Array must not contain duplicate elements",
                        "/required_array"
                ),
                Arguments.of("Required array is not an array",
                        testRequest().wrongTypeRequiredArray(),
                        "Instance type (string) does not match any allowed primitive type (allowed: [\"array\"])",
                        "/required_array"
                )
        );
    }

    static Stream<Arguments> requiredChildValidationErrorProvider() {
        var tooShortRequiredChildStringRequest = testRequest().requiredChild(testChild().tooShortRequiredChildString());
        var tooShort = tooShortRequiredChildStringRequest.requiredChild().requiredChildString();
        var tooLongRequiredChildStringRequest = testRequest().requiredChild(testChild().tooLongRequiredChildString());
        var tooLong = tooLongRequiredChildStringRequest.requiredChild().requiredChildString();

        return Stream.of(
                Arguments.of("Required child is not present",
                        testRequest().requiredChild(null),
                        "Missing required property",
                        "/required_child"
                ),
                Arguments.of("Required child's string is not present",
                        testRequest().requiredChild(testChild().requiredChildString(null)),
                        "Missing required property",
                        "/required_child/required_child_string"
                ),
                Arguments.of("Required child's string is too short",
                        tooShortRequiredChildStringRequest,
                        "String \"" + tooShort + "\" is too short (length: " + tooShort.length() + ", required minimum: 3)",
                        "/required_child/required_child_string"
                ),
                Arguments.of("Required child's string is too long",
                        tooLongRequiredChildStringRequest,
                        "String \"" + tooLong + "\" is too long (length: " + tooLong.length() + ", maximum allowed: 10)",
                        "/required_child/required_child_string"
                ),
                Arguments.of("Required array is not an array",
                        testRequest().wrongTypeRequiredChild(),
                        "Instance type (string) does not match any allowed primitive type (allowed: [\"object\"])",
                        "/required_child"
                ),
                Arguments.of("Required child's string is not a string",
                        testRequest().requiredChild(testChild().wrongTypeRequiredChildString()),
                        "Instance type (integer) does not match any allowed primitive type (allowed: [\"string\"])",
                        "/required_child/required_child_string"
                )
        );
    }

    static Stream<Arguments> jsr303ValidationErrorProvider() {
        return Stream.of(
                Arguments.of("String is blank",
                        testRequest().someOtherString("     "),
                        "String \"     \" must not be blank",
                        "/some_other_string"
                ),
                Arguments.of("Conditionally required field is not present",
                        testRequest().requiredBoolean(true).requiredIfRequiredBooleanIsTrue(null),
                        "Field is required if required boolean is true",
                        "/pointer_name_test"
                )
        );
    }

    @Test
    @DisplayName("respond with error detail on request with multiple validation error")
    void respond400WithMultipleValidationErrors() throws Exception {
        var request = testRequest()
                .requiredString(null)
                .requiredInt(5)
                .requiredDate("2000/01/01")
                .requiredChild(testChild().wrongTypeRequiredChildString());

        var result = mvc.perform(
                        post("/test/request")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(request))
                ).andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"));

        result
                .andExpect(jsonPath("type", equalTo("/problems/invalid-request")))
                .andExpect(jsonPath("status", equalTo(400)))
                .andExpect(jsonPath("title", equalTo("Request validation error")))
                .andExpect(jsonPath("detail", equalTo("Request parameters or body are invalid compared to the OpenAPI specification. See errors for more information")))
                .andExpect(jsonPath("errors", hasSize(4)))
                .andExpect(jsonPath("errors[0].detail", equalTo("Missing required property")))
                .andExpect(jsonPath("errors[0].parameter").doesNotExist())
                .andExpect(jsonPath("errors[0].pointer", equalTo("/required_string")))
                .andExpect(jsonPath("errors[1].detail", equalTo("Instance type (integer) does not match any allowed primitive type (allowed: [\"string\"])")))
                .andExpect(jsonPath("errors[1].parameter").doesNotExist())
                .andExpect(jsonPath("errors[1].pointer", equalTo("/required_child/required_child_string")))
                .andExpect(jsonPath("errors[2].detail", equalTo("String \"2000/01/01\" is invalid against requested date format(s) yyyy-MM-dd")))
                .andExpect(jsonPath("errors[2].parameter").doesNotExist())
                .andExpect(jsonPath("errors[2].pointer", equalTo("/required_date")))
                .andExpect(jsonPath("errors[3].detail", equalTo("Numeric instance is lower than the required minimum (minimum: 10, found: 5)")))
                .andExpect(jsonPath("errors[3].parameter").doesNotExist())
                .andExpect(jsonPath("errors[3].pointer", equalTo("/required_int")));
    }

    @RepeatedTest(5)
    @DisplayName("respond successfully when nullable request is valid")
    void respond204OnValidNullableRequest() throws Exception {
        mvc.perform(
                post("/test/nullable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(nullableElementRequest()))
        ).andExpect(status().isNoContent());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("nullableRequestValidationErrorProvider")
    @DisplayName("respond with error detail on request body validation error")
    void respond400OnNullableRequestValidationError(String name, WithNullableElementRequest request, String expectedPointer) throws Exception {
        var result = mvc.perform(
                        post("/test/nullable")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(request))
                ).andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"));

        result
                .andExpect(jsonPath("type", equalTo("/problems/invalid-request")))
                .andExpect(jsonPath("status", equalTo(400)))
                .andExpect(jsonPath("title", equalTo("Request validation error")))
                .andExpect(jsonPath("detail", equalTo("Request parameters or body are invalid compared to the OpenAPI specification. See errors for more information")))
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0].detail", equalTo("Not nullable property")))
                .andExpect(jsonPath("errors[0].parameter").doesNotExist())
                .andExpect(jsonPath("errors[0].pointer", equalTo(expectedPointer)));
    }

    static Stream<Arguments> nullableRequestValidationErrorProvider() {
        return Stream.of(
                Arguments.of("Non nullable string has null value",
                        nullableElementRequest().nullNonNullableString(),
                        "/non_nullable_string"
                ),
                Arguments.of("Non nullable integer has null value",
                        nullableElementRequest().nullNonNullableInteger(),
                        "/non_nullable_integer"
                ),
                Arguments.of("Non nullable child has null value",
                        nullableElementRequest().nullNonNullableChild(),
                        "/non_nullable_child"
                ),
                Arguments.of("Non nullable string in child has null value",
                        nullableElementRequest().nonNullableChild(nullableChild().nullNonNullableStringChild()),
                        "/non_nullable_child/non_nullable_string_child"
                )
        );
    }

    @Test
    @DisplayName("response successfully when expected parameters are provided")
    void respond204AllExpectedParameters() throws Exception {
        mvc.perform(
                get("/test/params/{uuid}/{int}", UUID.randomUUID(), 1)
                        .queryParam("optional_int", "2")
        ).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("response successfully when expected parameters are provided, without optional ones")
    void respond204AllExpectedRequiredParameters() throws Exception {
        mvc.perform(
                get("/test/params/{uuid}/{int}", UUID.randomUUID(), 1)
        ).andExpect(status().isNoContent());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidParameterErrorProvider")
    @DisplayName("respond with error detail on request parameters validation error")
    void respond400ParametersValidationError(String name, Object uuidParam, Object intParam, String optionalIntParam,
                                             String expectedDetail, String expectedParam) throws Exception {
        var result = mvc.perform(
                        get("/test/params/{uuid}/{int}", uuidParam, intParam)
                                .queryParam("optional_int", optionalIntParam)
                ).andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"));

        result
                .andExpect(jsonPath("type", equalTo("/problems/invalid-request")))
                .andExpect(jsonPath("status", equalTo(400)))
                .andExpect(jsonPath("title", equalTo("Request validation error")))
                .andExpect(jsonPath("detail", equalTo("Request parameters or body are invalid compared to the OpenAPI specification. See errors for more information")))
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0].detail", equalTo(expectedDetail)))
                .andExpect(jsonPath("errors[0].parameter", equalTo(expectedParam)))
                .andExpect(jsonPath("errors[0].pointer").doesNotExist());
    }

    static Stream<Arguments> invalidParameterErrorProvider() {
        return Stream.of(
                Arguments.of(
                        "Invalid UUID path parameter",
                        "invalid",
                        1,
                        "2",
                        "Input string \"invalid\" is not a valid UUID",
                        "uuid"
                ),
                Arguments.of(
                        "Invalid int path parameter",
                        UUID.randomUUID(),
                        "not an int",
                        "2",
                        "Instance type (string) does not match any allowed primitive type (allowed: [\"integer\"])",
                        "int"
                ),
                Arguments.of(
                        "Invalid int query parameter",
                        UUID.randomUUID(),
                        1,
                        "not an int",
                        "Instance type (string) does not match any allowed primitive type (allowed: [\"integer\"])",
                        "optional_int"
                ),
                Arguments.of(
                        "Too low int path parameter",
                        UUID.randomUUID(),
                        -1,
                        "2",
                        "Numeric instance is lower than the required minimum (minimum: 0, found: -1)",
                        "int"
                ),
                Arguments.of(
                        "Too high int path parameter",
                        UUID.randomUUID(),
                        11,
                        "2",
                        "Numeric instance is greater than the required maximum (maximum: 10, found: 11)",
                        "int"
                ),
                Arguments.of(
                        "Too low int query parameter",
                        UUID.randomUUID(),
                        1,
                        "1",
                        "Numeric instance is lower than the required minimum (minimum: 2, found: 1)",
                        "optional_int"
                ),
                Arguments.of(
                        "Too high int query parameter",
                        UUID.randomUUID(),
                        1,
                        "6",
                        "Numeric instance is greater than the required maximum (maximum: 5, found: 6)",
                        "optional_int"
                )
        );
    }

    @SpringBootApplication
    static class RequestValidationTestApplication {

        static void main(String[] args) {
            SpringApplication.run(RequestValidationTestApplication.class, args);
        }

        @RestController
        static class ValidationTestController {

            @GetMapping("/test/request")
            TestRequest testRequestGet(@RequestParam("required_parameter") String requiredParameter,
                                       @RequestHeader("Required-Header") String requiredHeader,
                                       @RequestHeader(name = "Optional-Header", required = false) String optionalHeader) {
                return testRequest().id("sampleId");
            }

            @PostMapping("/test/request")
            @ResponseStatus(HttpStatus.CREATED)
            TestRequest testRequestPost(@RequestBody @Valid TestRequest testRequest) {
                return testRequest.id("sampleId");
            }

            @PostMapping("/test/nullable")
            @ResponseStatus(HttpStatus.NO_CONTENT)
            void testNullablePost(@RequestBody WithNullableElementRequest request) {
                // no-op
            }

            @GetMapping("/test/params/{uuid}/{int}")
            @ResponseStatus(HttpStatus.NO_CONTENT)
            void testWithParams(@PathVariable("uuid") UUID uuid, @PathVariable("int") Integer integer,
                                @RequestParam(name = "optional_int", required = false) Integer optionalInt) {
                // no-op
            }

        }

    }

}
