package com.kouetcha.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class RequestException extends RuntimeException {
    private final HttpStatus status;

    public RequestException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public RequestException(String message) {
        super(message);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    }
}