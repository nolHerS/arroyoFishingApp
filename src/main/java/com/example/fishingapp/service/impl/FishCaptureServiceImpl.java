package com.example.fishingapp.service.impl;

import com.example.fishingapp.dto.FishCaptureDto;
import com.example.fishingapp.mapper.FishCaptureMapper;
import com.example.fishingapp.repository.FishCaptureRepository;
import com.example.fishingapp.service.FishCaptureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FishCaptureServiceImpl implements FishCaptureService {

    @Autowired
    private FishCaptureMapper fishCaptureMapper;

    private FishCaptureRepository fishCaptureRepository;

    @Override
    public FishCaptureDto createFishCapture(FishCaptureDto fishCaptureDto) {
        return null;
    }

    @Override
    public FishCaptureDto findById(Long id) {
        return null;
    }

    @Override
    public FishCaptureDto getAllByUsername(String userName) {
        return null;
    }

    @Override
    public FishCaptureDto getAllFishCapture() {
        return null;
    }

    @Override
    public FishCaptureDto updateFishCaptureDto(FishCaptureDto fishCaptureDto) {
        return null;
    }

    @Override
    public void deleteFishCaptureDto(Long fishCaptureId) {

    }
}
