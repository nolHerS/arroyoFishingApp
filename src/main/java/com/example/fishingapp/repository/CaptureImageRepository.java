package com.example.fishingapp.repository;

import com.example.fishingapp.model.CaptureImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaptureImageRepository extends JpaRepository<CaptureImage, Long> {

    List<CaptureImage> findByFishCaptureId(Long captureId);

    void deleteByFishCaptureId(Long captureId);

    long countByFishCaptureId(Long fishCaptureId);
}
