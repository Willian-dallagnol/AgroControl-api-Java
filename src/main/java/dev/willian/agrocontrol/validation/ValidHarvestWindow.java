package dev.willian.agrocontrol.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Constraint de classe: quando plantingDate e harvestDate estao presentes,
 * a colheita nao pode ser anterior ao plantio.
 */
@Documented
@Target(TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = HarvestWindowValidator.class)
public @interface ValidHarvestWindow {

    String message() default "data de colheita nao pode ser anterior a data de plantio";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
