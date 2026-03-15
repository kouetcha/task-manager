package com.kouetcha.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;


import java.util.Collection;

public class AtLeastOneFieldNotEmptyValidator
        implements ConstraintValidator<AtLeastOneFieldNotEmpty, Object> {

    private String[] fields;
    private String messageTemplate;

    @Override
    public void initialize(AtLeastOneFieldNotEmpty annotation) {
        this.fields = annotation.fields();
        this.messageTemplate = annotation.message();
    }

    @Override
    public boolean isValid(Object dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return false;
        }

        BeanWrapper wrapper = new BeanWrapperImpl(dto);
        for (String field : fields) {
            Object value = wrapper.getPropertyValue(field);
            if (value instanceof String) {
                if (!((String) value).trim().isEmpty()) {
                    return true;
                }
            } else if (value instanceof Collection) {
                if (!((Collection<?>) value).isEmpty()) {
                    return true;
                }
            } else if (value != null) {
                return true;
            }
        }

        // Build violation message with field list
        String joined = String.join(", ", fields);
        String msg = messageTemplate.replace("{fields}", joined);

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(msg)
                .addConstraintViolation();
        return false;
    }
}