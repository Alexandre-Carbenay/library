package org.adhuc.library.support.rest.validation;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import net.datafaker.Faker;

import java.util.function.Supplier;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static org.adhuc.library.support.rest.validation.WithNullableElementRequest.WithNullableChild.nullableChild;

@SuppressWarnings({"FieldCanBeLocal", "unused", "NotNullFieldNotInitialized"})
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonAutoDetect(fieldVisibility = ANY)
class WithNullableElementRequest {

    private static final Faker FAKER = new Faker();

    private String nonNullableString;
    private String nullableString;
    private Integer nonNullableInteger;
    private Integer nullableInteger;
    private WithNullableChild nonNullableChild;
    private WithNullableChild nullableChild;

    static WithNullableElementRequest nullableElementRequest() {
        var request = new WithNullableElementRequest();
        request.nonNullableString = FAKER.beer().name();
        request.nullableString = optionalValue(() -> FAKER.lordOfTheRings().character());
        request.nonNullableInteger = FAKER.random().nextInt();
        request.nullableInteger = optionalValue(() -> FAKER.random().nextInt());
        request.nonNullableChild = nullableChild();
        request.nullableChild = optionalValue(WithNullableChild::nullableChild);
        return request;
    }

    WithNullableElementRequest nullNonNullableString() {
        this.nonNullableString = null;
        return this;
    }

    WithNullableElementRequest nullNonNullableInteger() {
        this.nonNullableInteger = null;
        return this;
    }

    WithNullableElementRequest nullNonNullableChild() {
        this.nonNullableChild = null;
        return this;
    }

    WithNullableElementRequest nonNullableChild(WithNullableChild child) {
        this.nonNullableChild = child;
        return this;
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonAutoDetect(fieldVisibility = ANY)
    static class WithNullableChild {

        private String nonNullableStringChild;
        private String nullableStringChild;

        static WithNullableChild nullableChild() {
            var child = new WithNullableChild();
            child.nonNullableStringChild = FAKER.restaurant().name();
            child.nullableStringChild = optionalValue(() -> FAKER.company().name());
            return child;
        }

        WithNullableChild nullNonNullableStringChild() {
            this.nonNullableStringChild = null;
            return this;
        }

    }

    static <T> T optionalValue(Supplier<T> supplier) {
        return FAKER.bool().bool() ? supplier.get() : null;
    }

}
