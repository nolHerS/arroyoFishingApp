package com.example.fishingapp.dto;

import java.util.List;

//Salida con capturas
public record UserDto (
    Long id,
    String username,
    String fullName,
    String email){}

