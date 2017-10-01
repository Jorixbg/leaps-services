package com.leaps.model.exceptions;

@SuppressWarnings("serial")
public class InvalidInputParamsException extends Exception {
	
	public InvalidInputParamsException() {
		super();
	}
	
	public InvalidInputParamsException(String message) {
		super(message);
	}
}
