package org.adhuc.library.support.rest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.*;

import static org.springframework.util.StringUtils.hasLength;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RequiredIfRequiredBooleanIsTrue.RequiredIfRequiredBooleanIsTrueValidator.class)
@Documented
@interface RequiredIfRequiredBooleanIsTrue {

    String message() default "{required-if-required-boolean-is-true}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String pointerName() default "";

    class RequiredIfRequiredBooleanIsTrueValidator implements ConstraintValidator<RequiredIfRequiredBooleanIsTrue, Jsr303TestRequest> {
        @Override
        public void initialize(RequiredIfRequiredBooleanIsTrue constraintAnnotation) {
            ConstraintValidator.super.initialize(constraintAnnotation);
        }

        @Override
        public boolean isValid(Jsr303TestRequest value, ConstraintValidatorContext context) {
            return !(Boolean) value.required() || hasLength(value.conditionnallyRequired());
        }
    }

}
