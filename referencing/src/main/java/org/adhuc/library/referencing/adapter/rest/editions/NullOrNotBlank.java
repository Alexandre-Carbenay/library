package org.adhuc.library.referencing.adapter.rest.editions;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.jspecify.annotations.Nullable;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(FIELD)
@Retention(RUNTIME)
@Constraint(validatedBy = NullOrNotBlank.Validator.class)
@Documented
public @interface NullOrNotBlank {

    /**
     * The error message if the condition is not met.
     *
     * @return the error message.
     */
    String message() default "{referencing.edition.null-or-not-blank}";

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
     * The constraint validator for {@link NullOrNotBlank} annotation.
     */
    class Validator implements ConstraintValidator<NullOrNotBlank, String> {
        @Override
        public void initialize(NullOrNotBlank constraintAnnotation) {
            ConstraintValidator.super.initialize(constraintAnnotation);
        }

        @Override
        public boolean isValid(@Nullable String field, ConstraintValidatorContext constraintValidatorContext) {
            return field == null || !field.isBlank();
        }
    }

}
