package com.example.fishingapp.service.impl;

import com.example.fishingapp.dto.FishCaptureDto;
import com.example.fishingapp.exception.ResourceNotFoundException;
import com.example.fishingapp.mapper.FishCaptureMapper;
import com.example.fishingapp.mapper.UserMapper;
import com.example.fishingapp.model.FishCapture;
import com.example.fishingapp.model.User;
import com.example.fishingapp.repository.FishCaptureRepository;
import com.example.fishingapp.service.FishCaptureService;
import com.example.fishingapp.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FishCaptureServiceImpl implements FishCaptureService {

    private final FishCaptureRepository fishCaptureRepository;

    private final UserService userService;

    public FishCaptureServiceImpl(FishCaptureRepository fishCaptureRepository, UserService userService) {
        this.fishCaptureRepository = fishCaptureRepository;
        this.userService = userService;
    }

    @Override
    public FishCaptureDto createFishCapture(FishCaptureDto fishCaptureDto, Long userId) {

        User user = UserMapper.mapUser(userService.findById(userId));

        FishCapture fishCapture = FishCaptureMapper.mapFishCapture(fishCaptureDto, user);

        FishCapture savedFishCapture = fishCaptureRepository.save(fishCapture);

        return FishCaptureMapper.mapFishCaptureDto(savedFishCapture);
    }

    @Override
    public FishCaptureDto findById(Long id) {

        return fishCaptureRepository.findById(id).map(FishCaptureMapper::mapFishCaptureDto).orElseThrow(
                () -> new ResourceNotFoundException("FishCapture", "id", id.toString())
        );
    }

    @Override
    public List<FishCaptureDto> getAllFishCapturesByUsername(String userName) {

        User user = UserMapper.mapUser(userService.findByUsername(userName));

        return fishCaptureRepository.findByUser(user).stream().map(FishCaptureMapper::mapFishCaptureDto).toList();
    }

    @Override
    public List<FishCaptureDto> getAllFishCapture() {

        return fishCaptureRepository.findAll().stream().map(FishCaptureMapper::mapFishCaptureDto).toList();
    }

    @Override
    public FishCaptureDto updateFishCaptureDto(FishCaptureDto fishCaptureDto, Long requestingUserId) {
        FishCapture existingFishCapture = fishCaptureRepository.findById(fishCaptureDto.id())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "FishCapture", "id", fishCaptureDto.id().toString()
                ));

        // Validar que el usuario sea el dueño
        if (!existingFishCapture.getUser().getId().equals(requestingUserId)) {
            throw new IllegalStateException("No tienes permiso para editar esta captura");
        }

        existingFishCapture.setFishType(fishCaptureDto.fishType());
        existingFishCapture.setCaptureDate(fishCaptureDto.captureData());
        existingFishCapture.setCreatedAt(fishCaptureDto.createdAt());
        existingFishCapture.setLocation(fishCaptureDto.location());
        existingFishCapture.setWeight(fishCaptureDto.weight());

        return FishCaptureMapper.mapFishCaptureDto(fishCaptureRepository.save(existingFishCapture));
    }


    @Override
    public void deleteFishCaptureDto(Long fishCaptureId) {

        fishCaptureRepository.deleteById(fishCaptureId);
    }
}
