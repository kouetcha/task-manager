package com.kouetcha.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor


public class Erreur {
	
	private HttpStatus status;
    private String message;
    private List<ChampsMessage> errors;
    private TypeErreur typeErreur;
}