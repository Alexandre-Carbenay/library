package org.adhuc.library.referencing.adapter.rest.books;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.HashSet;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Constraint annotation to ensure that a book referencing cannot have duplicate language in details.
 */
@Target(TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = UniqueLanguageInDetails.UniqueLanguageInDetailsValidator.class)
@Documented
public @interface UniqueLanguageInDetails {

    /**
     * The error message if the condition is not met.
     *
     * @return the error message.
     */
    String message() default "{referencing.book.unique-language-in-details}";

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

    String pointerName() default "/details";

    /**
     * The constraint validator for {@link UniqueLanguageInDetails} annotation.
     */
    class UniqueLanguageInDetailsValidator implements ConstraintValidator<UniqueLanguageInDetails, BookReferencingRequest> {
        @Override
        public void initialize(UniqueLanguageInDetails constraintAnnotation) {
            ConstraintValidator.super.initialize(constraintAnnotation);
        }

        @Override
        public boolean isValid(BookReferencingRequest request, ConstraintValidatorContext constraintValidatorContext) {
            var languages = new HashSet<String>();
            for (int i = 0; i < request.details().size(); i++) {
                var language = request.details().get(i).language();
                if (languages.contains(language)) {
                    constraintValidatorContext
                            .buildConstraintViolationWithTemplate("language is duplicated in details")
                            .addPropertyNode("details")
                            .addPropertyNode("language").inIterable().atIndex(i)
                            .addConstraintViolation()
                            .disableDefaultConstraintViolation();
                    return false;
                }
                languages.add(language);
            }
            return true;
        }
    }

}
