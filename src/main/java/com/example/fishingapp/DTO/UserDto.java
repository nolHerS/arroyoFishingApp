package com.example.fishingapp.DTO;

import com.example.fishingapp.model.FishCapture;

import java.util.List;

//Salida con capturas
public record UserDto (
    Long id,
    String username,
    String fullName,
    String email,
    List<FishCaptureDto> fishCaptures){}

