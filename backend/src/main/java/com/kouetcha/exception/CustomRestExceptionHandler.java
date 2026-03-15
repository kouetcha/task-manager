package com.kouetcha.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ControllerAdvice
public class CustomRestExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Validation des arguments annotés avec @Valid
     */
    @Override
    @NonNull
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @NonNull org.springframework.web.bind.MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        List<String> errors = new ArrayList<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.add(error.getField() + ": " + error.getDefaultMessage()));
        ex.getBindingResult().getGlobalErrors()
                .forEach(error -> errors.add(error.getObjectName() + ": " + error.getDefaultMessage()));

        CustomErrorMessage body = new CustomErrorMessage(HttpStatus.BAD_REQUEST, "Validation failed", errors);
        return handleExceptionInternal(ex, body, headers, HttpStatus.BAD_REQUEST, request);
    }

    /**
     * Paramètre manquant dans la requête
     */
    @Override
    @NonNull
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            @NonNull MissingServletRequestParameterException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        String error = ex.getParameterName() + " parameter is missing";
        CustomErrorMessage body = new CustomErrorMessage(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), error);
        return new ResponseEntity<>(body, headers, HttpStatus.BAD_REQUEST);
    }

    /**
     * Violation de contraintes Jakarta Validation
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> errors = new ArrayList<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.add(violation.getRootBeanClass().getSimpleName() + "." +
                    violation.getPropertyPath() + ": " + violation.getMessage());
        }
        CustomErrorMessage body = new CustomErrorMessage(HttpStatus.BAD_REQUEST, "Constraint violations", errors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Type d’argument incorrect
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String error = ex.getName() + " should be of type " +
                Objects.requireNonNull(ex.getRequiredType()).getSimpleName();
        CustomErrorMessage body = new CustomErrorMessage(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), error);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Méthode HTTP non supportée
     */
    @Override
    @NonNull
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            @NonNull HttpRequestMethodNotSupportedException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        StringBuilder builder = new StringBuilder();
        builder.append(ex.getMethod())
                .append(" method is not supported. Supported methods: ");
        Objects.requireNonNull(ex.getSupportedHttpMethods())
                .forEach(t -> builder.append(t).append(" "));

        CustomErrorMessage body = new CustomErrorMessage(HttpStatus.METHOD_NOT_ALLOWED,
                ex.getLocalizedMessage(),
                builder.toString());
        return new ResponseEntity<>(body, headers, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Type de média non supporté
     */
    @Override
    @NonNull
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            @NonNull HttpMediaTypeNotSupportedException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        StringBuilder builder = new StringBuilder();
        builder.append(ex.getContentType()).append(" media type is not supported. Supported media types: ");
        ex.getSupportedMediaTypes().forEach(t -> builder.append(t).append(", "));
        if (builder.length() > 2) builder.setLength(builder.length() - 2); // supprime la dernière virgule

        CustomErrorMessage body = new CustomErrorMessage(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                ex.getLocalizedMessage(),
                builder.toString());
        return new ResponseEntity<>(body, headers, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }
}
