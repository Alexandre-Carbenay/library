package org.adhuc.library.referencing.adapter.rest.editions;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.apache.commons.validator.routines.ISBNValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(FIELD)
@Retention(RUNTIME)
@Constraint(validatedBy = Isbn.Validator.class)
@Documented
public @interface Isbn {

    /**
     * The error message if the condition is not met.
     *
     * @return the error message.
     */
    String message() default "{referencing.edition.isbn}";

    /**
     * The groups associated to the validation.
     *
     * @return the groups associated to the validation.
     */
    Class<?>[] groups() default {};

    /**
     * The payload used for extensibility purpose.
     *
     * @return the payload used for extensibility purpose.
     */
    Class<? extends Payload>[] payload() default {};

    String pointerName() default "";

    /**
     * The constraint validator for {@link Isbn} annotation.
     */
    class Validator implements ConstraintValidator<Isbn, String> {
        private static final ISBNValidator ISBN_VALIDATOR = new ISBNValidator();

        @Override
        public void initialize(Isbn constraintAnnotation) {
            ConstraintValidator.super.initialize(constraintAnnotation);
        }

        @Override
        public boolean isValid(String field, ConstraintValidatorContext constraintValidatorContext) {
            return ISBN_VALIDATOR.isValidISBN13(field);
        }
    }

}
