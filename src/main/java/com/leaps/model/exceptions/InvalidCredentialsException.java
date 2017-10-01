package com.leaps.model.exceptions;

@SuppressWarnings("serial")
public class InvalidCredentialsException extends RuntimeException {
	
    public InvalidCredentialsException(){
        super();
    }

    public InvalidCredentialsException(String message){
        super(message);
    }
}
