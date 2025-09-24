package com.example.fishingapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class UsernameAlreadyExistsException extends RuntimeException{

    private String message;

    public UsernameAlreadyExistsException(String message){
        super(message);
    }
}
