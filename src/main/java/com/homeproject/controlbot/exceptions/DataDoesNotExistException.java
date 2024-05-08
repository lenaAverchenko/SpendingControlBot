package com.homeproject.controlbot.exceptions;

public class DataDoesNotExistException extends RuntimeException{
    public DataDoesNotExistException(String message) {
        super(message);
    }
}
