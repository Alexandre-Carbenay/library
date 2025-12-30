package org.adhuc.library.support.rest.validation;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import net.datafaker.Faker;
import tools.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import tools.jackson.databind.annotation.JsonNaming;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
@JsonInclude(NON_ABSENT)
@JsonNaming(SnakeCaseStrategy.class)
@JsonAutoDetect(fieldVisibility = ANY)
class TestRequest {

    private static final Faker FAKER = new Faker();

    private String id;
    private Object requiredString;
    private Object optionalString;
    private Object requiredInt;
    private Object optionalInt;
    private Object requiredBoolean;
    private Object optionalBoolean;
    private Object requiredDate;
    private Object optionalDate;
    private Object requiredUuid;
    private Object optionalUuid;
    private Object requiredArray;
    private Object optionalArray;
    private Object requiredChild;
    private Object optionalChild;

    static TestRequest testRequest() {
        var request = new TestRequest();
        request.requiredString = FAKER.beer().name();
        request.optionalString = optionalValue(() -> FAKER.restaurant().name());
        request.requiredInt = FAKER.random().nextInt(10, 20);
        request.optionalInt = optionalValue(() -> FAKER.random().nextInt());
        request.requiredBoolean = FAKER.random().nextBoolean();
        request.optionalBoolean = optionalValue(() -> FAKER.random().nextBoolean());
        request.requiredDate = FAKER.timeAndDate().birthday().toString();
        request.optionalDate = optionalValue(() -> FAKER.timeAndDate().birthday().toString());
        request.requiredUuid = UUID.randomUUID().toString();
        request.optionalUuid = optionalValue(() -> UUID.randomUUID().toString());
        request.requiredArray = array(2, 5, () -> FAKER.text().text());
        request.optionalArray = array(0, 5, () -> FAKER.text().text());
        request.requiredChild = TestChild.testChild();
        request.optionalChild = optionalValue(TestChild::testChild);
        return request;
    }

    TestRequest id(String id) {
        this.id = id;
        return this;
    }

    String requiredString() {
        return (String) requiredString;
    }

    TestRequest requiredString(String value) {
        this.requiredString = value;
        return this;
    }

    TestRequest tooShortRequiredString() {
        return requiredString(FAKER.text().text(0, 4));
    }

    TestRequest tooLongRequiredString() {
        return requiredString(FAKER.text().text(101, 1000));
    }

    TestRequest wrongTypeRequiredString() {
        this.requiredString = FAKER.random().nextInt();
        return this;
    }

    Integer requiredInt() {
        return (Integer) requiredInt;
    }

    TestRequest requiredInt(Integer value) {
        this.requiredInt = value;
        return this;
    }

    TestRequest tooLowRequiredInt() {
        return requiredInt(FAKER.random().nextInt(10));
    }

    TestRequest tooHighRequiredInt() {
        return requiredInt(FAKER.random().nextInt(21, Integer.MAX_VALUE));
    }

    TestRequest wrongTypeRequiredInt() {
        this.requiredInt = FAKER.text().text();
        return this;
    }

    TestRequest requiredBoolean(Boolean value) {
        this.requiredBoolean = value;
        return this;
    }

    TestRequest wrongTypeRequiredBoolean() {
        this.requiredBoolean = FAKER.text().text();
        return this;
    }

    String requiredDate() {
        return (String) requiredDate;
    }

    TestRequest requiredDate(String value) {
        this.requiredDate = value;
        return this;
    }

    TestRequest wrongTypeRequiredDate() {
        this.requiredDate = FAKER.random().nextInt();
        return this;
    }

    TestRequest invalidRequiredDateFormat() {
        return requiredDate(FAKER.timeAndDate().birthday().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
    }

    String requiredUuid() {
        return (String) requiredUuid;
    }

    TestRequest requiredUuid(String value) {
        this.requiredUuid = value;
        return this;
    }

    TestRequest wrongTypeRequiredUuid() {
        this.requiredUuid = FAKER.random().nextInt();
        return this;
    }

    TestRequest invalidRequiredUuidFormat() {
        return requiredUuid(FAKER.text().text(36));
    }

    List<?> requiredArray() {
        return (List<?>) requiredArray;
    }

    TestRequest requiredArray(List<?> value) {
        this.requiredArray = value;
        return this;
    }

    TestRequest tooFewRequiredArrayElements() {
        return requiredArray(array(0, 1, () -> FAKER.text().text()));
    }

    TestRequest tooManyRequiredArrayElements() {
        return requiredArray(array(6, 100, () -> FAKER.text().text()));
    }

    TestRequest duplicateRequiredArrayElements() {
        var array = new ArrayList<>(array(1, 4, () -> FAKER.text().text()));
        array.add(array.getFirst());
        return requiredArray(List.copyOf(array));
    }

    TestRequest wrongTypeRequiredArray() {
        this.requiredArray = FAKER.text().text();
        return this;
    }

    TestChild requiredChild() {
        return (TestChild) requiredChild;
    }

    TestRequest requiredChild(Object value) {
        this.requiredChild = value;
        return this;
    }

    TestRequest wrongTypeRequiredChild() {
        this.requiredChild = FAKER.text().text();
        return this;
    }

    @JsonInclude(NON_ABSENT)
    @JsonNaming(SnakeCaseStrategy.class)
    @JsonAutoDetect(fieldVisibility = ANY)
    static class TestChild {

        private Object requiredChildString;
        private Object optionalChildString;

        static TestChild testChild() {
            var child = new TestChild();
            child.requiredChildString = FAKER.text().text(3, 10);
            child.optionalChildString = optionalValue(() -> FAKER.restaurant().name());
            return child;
        }

        String requiredChildString() {
            return (String) requiredChildString;
        }

        TestChild requiredChildString(String value) {
            this.requiredChildString = value;
            return this;
        }

        TestChild tooShortRequiredChildString() {
            return requiredChildString(FAKER.text().text(0, 2));
        }

        TestChild tooLongRequiredChildString() {
            return requiredChildString(FAKER.text().text(11, 100));
        }

        TestChild wrongTypeRequiredChildString() {
            this.requiredChildString = FAKER.random().nextInt();
            return this;
        }

    }

    static <T> T optionalValue(Supplier<T> supplier) {
        return FAKER.bool().bool() ? supplier.get() : null;
    }

    static <T> List<T> array(int minElements, int maxElements, Supplier<T> supplier) {
        var numberOfElements = FAKER.random().nextInt(minElements, maxElements);
        return IntStream.rangeClosed(1, numberOfElements).mapToObj(i -> supplier.get()).toList();
    }

}
