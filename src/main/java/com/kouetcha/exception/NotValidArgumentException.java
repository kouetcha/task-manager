package com.kouetcha.exception;

public class NotValidArgumentException extends Exception {

    private final String fieldName;

    public NotValidArgumentException(String message) {
        super(message);
        this.fieldName = "message";
    }

    public NotValidArgumentException(String message, String fieldName) {
        super(message);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}