package org.adhuc.library.support.rest.validation;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import net.datafaker.Faker;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;

@JsonInclude(NON_ABSENT)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonAutoDetect(fieldVisibility = ANY)
@ExpectedChildCondition(expectedValues = {"a", "b", "c"})
@RequiredIfRequiredBooleanIsTrue(pointerName = "/pointer_name_test")
public class Jsr303TestRequest {

    private static final Faker FAKER = new Faker();

    @NotBlank
    private String notBlankValue;
    @Positive
    private Integer positiveValue;
    @NegativeOrZero
    private Integer negativeOrZeroValue;
    @Email
    private String emailValue;
    @PastOrPresent
    private LocalDate pastOrPresentValue;
    @Size(min = 2, max = 5)
    private List<String> sizedListValue;
    @Digits(integer = 3, fraction = 2)
    private BigDecimal amount;
    private Boolean required;
    private String conditionallyRequired;
    private List<ChildCondition> conditions;

    static Jsr303TestRequest jsr303TestRequest() {
        var request = new Jsr303TestRequest();
        request.notBlankValue = FAKER.beer().name();
        request.positiveValue = FAKER.number().positive();
        request.negativeOrZeroValue = FAKER.number().numberBetween(-100, 0);
        request.emailValue = FAKER.internet().emailAddress();
        request.pastOrPresentValue = FAKER.timeAndDate().birthday();
        request.sizedListValue = IntStream.rangeClosed(1, FAKER.number().numberBetween(2, 5))
                .mapToObj(_ -> FAKER.text().text(1, 10))
                .toList();
        request.amount = BigDecimal.valueOf(FAKER.number().numberBetween(-999.0d, 999.0d)).round(new MathContext(2));
        request.required = FAKER.bool().bool();
        request.conditionallyRequired = request.required ? FAKER.name().name() : (FAKER.bool().bool() ? FAKER.name().name() : null);
        request.conditions = List.of(
                new ChildCondition("a", FAKER.bool().bool()),
                new ChildCondition("b", FAKER.bool().bool()),
                new ChildCondition("c", FAKER.bool().bool())
        );
        return request;
    }

    Jsr303TestRequest notBlankValue(String value) {
        notBlankValue = value;
        return this;
    }

    Jsr303TestRequest positiveValue(Integer value) {
        positiveValue = value;
        return this;
    }

    Jsr303TestRequest negativeOrZeroValue(Integer value) {
        negativeOrZeroValue = value;
        return this;
    }

    Jsr303TestRequest emailValue(String value) {
        emailValue = value;
        return this;
    }

    Jsr303TestRequest pastOrPresentValue(LocalDate value) {
        pastOrPresentValue = value;
        return this;
    }

    Jsr303TestRequest sizedListValue(List<String> value) {
        sizedListValue = value;
        return this;
    }

    Jsr303TestRequest amount(BigDecimal value) {
        amount = value;
        return this;
    }

    boolean required() {
        return required != null ? required : false;
    }

    Jsr303TestRequest required(boolean value) {
        required = value;
        return this;
    }

    String conditionnallyRequired() {
        return conditionallyRequired;
    }

    Jsr303TestRequest conditionallyRequired(String value) {
        conditionallyRequired = value;
        return this;
    }

    List<ChildCondition> conditions() {
        return conditions;
    }

    Jsr303TestRequest conditions(List<ChildCondition> conditions) {
        this.conditions = List.copyOf(conditions);
        return this;
    }

    record ChildCondition(String name, Boolean enabled) {

    }

}
