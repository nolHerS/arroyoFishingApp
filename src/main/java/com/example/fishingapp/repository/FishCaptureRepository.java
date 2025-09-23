package com.example.fishingapp.repository;

import com.example.fishingapp.model.FishCapture;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FishCaptureRepository extends JpaRepository<FishCapture, Long> {
}
