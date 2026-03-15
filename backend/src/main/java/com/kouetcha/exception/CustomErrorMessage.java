package com.kouetcha.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;

@Setter
@Getter
public class CustomErrorMessage {
	private HttpStatus status;
    private String message;
    private List<String> errors;

    public CustomErrorMessage(HttpStatus status, String message, List<String> errors) {

        this.status = status;
        this.message = message;
        this.errors = errors;
    }

    public CustomErrorMessage(HttpStatus status, String message, String error) {

        this.status = status;
        this.message = message;
        errors = Arrays.asList(error);
    }
}