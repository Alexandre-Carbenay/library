package org.adhuc.library.support.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.*;
import java.util.List;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ExpectedChildCondition.ExpectedChildConditionValidator.class)
@Documented
public @interface ExpectedChildCondition {

    String message() default "";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String[] expectedValues() default {};

    class ExpectedChildConditionValidator implements ConstraintValidator<ExpectedChildCondition, Jsr303TestRequest> {
        private ExpectedChildCondition constraintAnnotation;
        @Override
        public void initialize(ExpectedChildCondition constraintAnnotation) {
            ConstraintValidator.super.initialize(constraintAnnotation);
            this.constraintAnnotation = constraintAnnotation;
        }

        @Override
        public boolean isValid(Jsr303TestRequest value, ConstraintValidatorContext context) {
            var conditions = value.conditions();
            if (conditions.size() < constraintAnnotation.expectedValues().length) {
                context.buildConstraintViolationWithTemplate("Missing child condition %s".formatted(constraintAnnotation.expectedValues()[conditions.size()]))
                        .addPropertyNode("conditions")
                        .addConstraintViolation()
                        .disableDefaultConstraintViolation();
                return false;
            }
            for (int i = 0; i < conditions.size(); i++) {
                var childCondition = conditions.get(i);
                var expectedValue = constraintAnnotation.expectedValues()[i];
                if (!childCondition.name().equals(expectedValue)) {
                    context.buildConstraintViolationWithTemplate("Expected child condition %s but was %s".formatted(expectedValue, childCondition.name()))
                            .addPropertyNode("conditions")
                            .addPropertyNode("name").inIterable().atIndex(i)
                            .addConstraintViolation()
                            .disableDefaultConstraintViolation();
                    return false;
                }
            }
            return true;
        }
    }

}
