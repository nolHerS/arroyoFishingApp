package com.example.fishingapp.exception;

/**
 * Excepción para accesos no autorizados
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
