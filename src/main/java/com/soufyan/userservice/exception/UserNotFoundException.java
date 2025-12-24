package com.soufyan.userservice.exception;

public class UserNotFoundException  extends RuntimeException{
    public UserNotFoundException(long userId) {
        super(String.valueOf(userId));
    }
    
}
