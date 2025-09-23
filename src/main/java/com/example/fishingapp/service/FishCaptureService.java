package com.example.fishingapp.service;

import com.example.fishingapp.DTO.FishCaptureDto;

public interface FishCaptureService {

    FishCaptureDto createFishCapture(FishCaptureDto fishCaptureDto);

    FishCaptureDto findById(Long id);

    FishCaptureDto getAllByUsername(String userName);

    FishCaptureDto getAllFishCapture();

    FishCaptureDto updateFishCaptureDto (FishCaptureDto fishCaptureDto);

    void deleteFishCaptureDto(Long fishCaptureId);
}
