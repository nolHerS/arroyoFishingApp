package com.example.fishingapp.mapper;

import com.example.fishingapp.dto.FishCaptureDto;
import com.example.fishingapp.model.FishCapture;
import com.example.fishingapp.model.User;

public class FishCaptureMapper {

    public static FishCaptureDto mapFishCaptureDto(FishCapture fishCapture){
        return new FishCaptureDto(
                fishCapture.getId(),
                fishCapture.getUser().getId() ,
                fishCapture.getFishType(),
                fishCapture.getWeight(),
                fishCapture.getCaptureDate(),
                fishCapture.getLocation(),
                fishCapture.getCreatedAt()
        );
    }

    public static FishCapture mapFishCapture(FishCaptureDto fishCaptureDto, User user){
        return new FishCapture(
                fishCaptureDto.id(),
                fishCaptureDto.captureData(),
                fishCaptureDto.createdAt(),
                fishCaptureDto.fishType(),
                fishCaptureDto.location(),
                fishCaptureDto.weight(),
                user
        );
    }
}
