package org.adhuc.library.referencing.adapter.rest.authors;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.adhuc.library.referencing.authors.AliveOrDead;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Constraint annotation to ensure that an author is born before being dead.
 */
@Target(TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = BornBeforeDead.BornBeforeDeadValidator.class)
@Documented
public @interface BornBeforeDead {

    /**
     * The error message if the condition is not met.
     *
     * @return the error message.
     */
    String message() default "{referencing.author.born-before-dead}";

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

    String pointerName() default "/";

    /**
     * The constraint validator for {@link BornBeforeDead} annotation.
     */
    class BornBeforeDeadValidator implements ConstraintValidator<BornBeforeDead, AliveOrDead> {
        @Override
        public void initialize(BornBeforeDead constraintAnnotation) {
            ConstraintValidator.super.initialize(constraintAnnotation);
        }

        @Override
        public boolean isValid(AliveOrDead aliveOrDead, ConstraintValidatorContext constraintValidatorContext) {
            return aliveOrDead.isBornBeforeDead();
        }
    }

}
