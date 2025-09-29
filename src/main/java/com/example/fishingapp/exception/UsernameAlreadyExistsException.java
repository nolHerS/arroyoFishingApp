package com.example.fishingapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class UsernameAlreadyExistsException extends RuntimeException{

    private final String message;

    public UsernameAlreadyExistsException(String message) {
        this.message = message;
    }
}
