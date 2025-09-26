package com.example.fishingapp.repository;

import com.example.fishingapp.model.FishCapture;
import com.example.fishingapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FishCaptureRepository extends JpaRepository<FishCapture, Long> {

    List<FishCapture> findByUser(User user);
}
