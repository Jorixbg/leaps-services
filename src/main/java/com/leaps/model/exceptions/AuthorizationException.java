package com.leaps.model.exceptions;

@SuppressWarnings("serial")
public class AuthorizationException extends Exception {
	
	public AuthorizationException() {
		super();
	}
	
	public AuthorizationException(String message) {
		super(message);
	}
}
