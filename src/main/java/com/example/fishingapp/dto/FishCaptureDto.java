package com.example.fishingapp.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record FishCaptureDto (

     Long id,
     Long userId,
     String fishType,
     Double weight,
     LocalDate captureData,
     String location,
     LocalDateTime createdAt){}
