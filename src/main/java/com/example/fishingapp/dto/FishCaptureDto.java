package com.example.fishingapp.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record FishCaptureDto (

     Long id,
     Long userId,
     String fishType,
     Float weight,
     LocalDate captureData,
     String location,
     LocalDateTime createdAt){}
