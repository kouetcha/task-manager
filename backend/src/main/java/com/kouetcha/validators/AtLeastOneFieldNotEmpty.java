package com.kouetcha.validators;



import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AtLeastOneFieldNotEmptyValidator.class)
@Documented
public @interface AtLeastOneFieldNotEmpty {
    String message() default "At least one of the fields [{fields}] must be provided.";
    String[] fields();
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}