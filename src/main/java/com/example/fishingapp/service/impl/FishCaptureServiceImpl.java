package com.example.fishingapp.service.impl;

import com.example.fishingapp.dto.FishCaptureDto;
import com.example.fishingapp.exception.ResourceNotFoundException;
import com.example.fishingapp.mapper.FishCaptureMapper;
import com.example.fishingapp.mapper.UserMapper;
import com.example.fishingapp.model.FishCapture;
import com.example.fishingapp.model.User;
import com.example.fishingapp.repository.FishCaptureRepository;
import com.example.fishingapp.repository.UserRepository;
import com.example.fishingapp.security.AuthUser;
import com.example.fishingapp.service.FishCaptureService;
import com.example.fishingapp.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FishCaptureServiceImpl implements FishCaptureService {

    private final FishCaptureRepository fishCaptureRepository;

    private final UserRepository userRepository;

    public FishCaptureServiceImpl(FishCaptureRepository fishCaptureRepository, UserRepository userRepository) {
        this.fishCaptureRepository = fishCaptureRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public FishCaptureDto createFishCapture(FishCaptureDto fishCaptureDto, Long userId) {
        // Buscar User directamente desde el repositorio
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));

        FishCapture fishCapture = FishCaptureMapper.mapFishCapture(fishCaptureDto, user);
        FishCapture savedFishCapture = fishCaptureRepository.save(fishCapture);

        return FishCaptureMapper.mapFishCaptureDto(savedFishCapture);
    }

    @Override
    @Transactional(readOnly = true)
    public FishCaptureDto findById(Long id) {
        return fishCaptureRepository.findById(id)
                .map(FishCaptureMapper::mapFishCaptureDto)
                .orElseThrow(() -> new ResourceNotFoundException("FishCapture", "id", id.toString()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FishCaptureDto> getAllFishCapturesByUsername(String userName) {
        // Buscar User directamente desde el repositorio
        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", userName));

        return fishCaptureRepository.findByUser(user)
                .stream()
                .map(FishCaptureMapper::mapFishCaptureDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FishCaptureDto> getAllFishCapture() {
        return fishCaptureRepository.findAll()
                .stream()
                .map(FishCaptureMapper::mapFishCaptureDto)
                .toList();
    }

    @Transactional
    public FishCaptureDto updateFishCaptureDto(FishCaptureDto fishCaptureDto, Long requestingUserId, AuthUser authUser) {
        // ⭐ CORREGIDO: Buscar por el ID de la captura, no del usuario
        FishCapture existingFishCapture = fishCaptureRepository.findById(fishCaptureDto.id())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "FishCapture", "id", fishCaptureDto.id().toString()
                ));

        // Validar que el usuario sea el dueño
        if (!existingFishCapture.getUser().getId().equals(authUser.getUser().getId())) {
            throw new IllegalStateException("No tienes permiso para editar esta captura");
        }

        existingFishCapture.setFishType(fishCaptureDto.fishType());
        existingFishCapture.setCaptureDate(fishCaptureDto.captureData());
        existingFishCapture.setLocation(fishCaptureDto.location());
        existingFishCapture.setWeight(fishCaptureDto.weight());

        return FishCaptureMapper.mapFishCaptureDto(fishCaptureRepository.save(existingFishCapture));
    }

    @Override
    @Transactional
    public void deleteFishCaptureDto(Long fishCaptureId, Long requestingUserId) {
        FishCapture fishCapture = fishCaptureRepository.findById(fishCaptureId)
                .orElseThrow(() -> new ResourceNotFoundException("FishCapture", "id", fishCaptureId.toString()));

        // Validar que el usuario sea el dueño
        if (!fishCapture.getUser().getId().equals(requestingUserId)) {
            throw new IllegalStateException("No tienes permiso para eliminar esta captura");
        }

        fishCaptureRepository.deleteById(fishCaptureId);
    }
}
