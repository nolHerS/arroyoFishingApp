package com.example.fishingapp.service;

import com.example.fishingapp.dto.FishCaptureDto;

import java.util.List;

public interface FishCaptureService {

    FishCaptureDto createFishCapture(FishCaptureDto fishCaptureDto, Long userId);

    FishCaptureDto findById(Long id);

    List<FishCaptureDto> getAllFishCapturesByUsername(String userName);

    List<FishCaptureDto> getAllFishCapture();

    FishCaptureDto updateFishCaptureDto (FishCaptureDto fishCaptureDto);

    void deleteFishCaptureDto(Long fishCaptureId);
}
