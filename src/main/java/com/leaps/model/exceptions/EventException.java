package com.leaps.model.exceptions;

@SuppressWarnings("serial")
public class EventException extends Exception {
	
	public EventException() {
		super();
	}
	
	public EventException(String message) {
		super(message);
	}
}
