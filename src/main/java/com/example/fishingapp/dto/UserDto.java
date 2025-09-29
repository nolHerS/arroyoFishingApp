package com.example.fishingapp.dto;

//Salida con capturas
public record UserDto (
    Long id,
    String username,
    String fullName,
    String email){}

