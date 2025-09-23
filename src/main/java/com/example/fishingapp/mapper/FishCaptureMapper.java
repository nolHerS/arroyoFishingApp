package com.example.fishingapp.mapper;

import com.example.fishingapp.DTO.FishCaptureDto;
import com.example.fishingapp.model.FishCapture;

public class FishCaptureMapper {

    public static FishCaptureDto mapFishCaptureDto(FishCapture fishCapture){
        return new FishCaptureDto(
                fishCapture.getId(),
                UserMapper.mapUserSummaryDto(fishCapture.getUser()) ,
                fishCapture.getFishType(),
                fishCapture.getWeight(),
                fishCapture.getCaptureDate(),
                fishCapture.getLocation(),
                fishCapture.getCreatedAt()
        );
    }

    public static FishCapture mapFishCapture (FishCaptureDto fishCaptureDto){
        return new FishCapture(
                fishCaptureDto.id(),
                UserMapper.mapToUserFromUserSummaryDto(fishCaptureDto.user().id),
                fishCaptureDto.fishType(),
                fishCaptureDto.weight(),
                fishCaptureDto.captureData(),
                fishCaptureDto.location(),
                fishCaptureDto.createdAt()
        );
    }

    public static FishCaptureSummaryDto mapFishCaptureSummary (FishCaptureDto fishCaptureDto){
        return new FishCaptureSummaryDto(
                fishCaptureDto.id(),
                fishCaptureDto.fishType(),
                fishCaptureDto.weight(),
                fishCaptureDto.captureData(),
                fishCaptureDto.location(),
                fishCaptureDto.createdAt()
        );
    }
}
