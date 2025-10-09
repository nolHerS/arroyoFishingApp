package com.example.fishingapp.service;

import com.example.fishingapp.dto.FishCaptureDto;
import com.example.fishingapp.security.AuthUser;

import java.util.List;

public interface FishCaptureService {

    FishCaptureDto createFishCapture(FishCaptureDto fishCaptureDto, Long userId);

    FishCaptureDto findById(Long id);

    List<FishCaptureDto> getAllFishCapturesByUsername(String userName);

    List<FishCaptureDto> getAllFishCapture();

    FishCaptureDto updateFishCaptureDto (FishCaptureDto fishCaptureDto, Long userId, AuthUser authUser);

    void deleteFishCaptureDto(Long fishCaptureId, Long userCaptureId);
}
