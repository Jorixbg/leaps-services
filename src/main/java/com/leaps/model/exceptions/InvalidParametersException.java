package com.leaps.model.exceptions;

@SuppressWarnings("serial")
public class InvalidParametersException extends Exception {
	
	public InvalidParametersException() {
		super();
	}
	
	public InvalidParametersException(String message) {
		super(message);
	}
}
