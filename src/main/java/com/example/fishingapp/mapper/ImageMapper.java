package com.example.fishingapp.mapper;

import com.example.fishingapp.dto.image.ImageResponseDto;
import com.example.fishingapp.model.CaptureImage;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ImageMapper {
    /**
     * Convierte una entidad CaptureImage a Dto Record
     */
    public ImageResponseDto toDto(CaptureImage image) {
        if (image == null) {
            return null;
        }

        return new ImageResponseDto(
                image.getId(),
                image.getOriginalUrl(),
                image.getThumbnailUrl(),
                image.getFileName(),
                image.getFileSize(),
                image.getMimeType(),
                image.getWidth(),
                image.getHeight(),
                image.getUploadedAt()
        );
    }

    /**
     * Convierte una lista de entidades a Dtos
     */
    public List<ImageResponseDto> toDtoList(List<CaptureImage> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }

        return images.stream()
                .map(this::toDto)
                .toList();
    }
}
