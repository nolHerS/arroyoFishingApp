package com.example.fishingapp.dto;

//Salida con capturas
public record UserDto (
    Long id,
    String username,
//    String password,
    String fullName,
    String email){}

