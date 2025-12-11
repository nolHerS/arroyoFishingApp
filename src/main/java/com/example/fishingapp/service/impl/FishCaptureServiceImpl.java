package com.example.fishingapp.service.impl;

import com.example.fishingapp.dto.FishCaptureDto;
import com.example.fishingapp.exception.ResourceNotFoundException;
import com.example.fishingapp.exception.UnauthorizedException;
import com.example.fishingapp.mapper.FishCaptureMapper;
import com.example.fishingapp.model.FishCapture;
import com.example.fishingapp.model.User;
import com.example.fishingapp.repository.FishCaptureRepository;
import com.example.fishingapp.repository.UserRepository;
import com.example.fishingapp.security.AuthUser;
import com.example.fishingapp.service.CaptureImageService;
import com.example.fishingapp.service.FishCaptureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class FishCaptureServiceImpl implements FishCaptureService {

    private final FishCaptureRepository fishCaptureRepository;

    private final UserRepository userRepository;

    private final CaptureImageService captureImageService;

    public FishCaptureServiceImpl(FishCaptureRepository fishCaptureRepository, UserRepository userRepository, CaptureImageService captureImageService) {
        this.fishCaptureRepository = fishCaptureRepository;
        this.userRepository = userRepository;
        this.captureImageService = captureImageService;
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

    @Transactional
    public void deleteFishCaptureDto(Long idFishCapture, Long userId) {
        log.info("🗑️ Iniciando eliminación de captura {} por usuario {}", idFishCapture, userId);

        // 1. Verificar que existe
        FishCapture fishCapture = fishCaptureRepository.findById(idFishCapture)
                .orElseThrow(() -> {
                    log.error("❌ Captura no encontrada con ID: {}", idFishCapture);
                    return new ResourceNotFoundException("Captura no encontrada con ID: " + idFishCapture);
                });

        log.info("✅ Captura encontrada: {}", fishCapture.getFishType());

        // 2. Verificar permisos
        if (!fishCapture.getUser().getId().equals(userId)) {
            log.error("❌ Usuario {} no tiene permisos para eliminar captura {}", userId, idFishCapture);
            throw new UnauthorizedException("No tienes permisos para eliminar esta captura");
        }

        log.info("✅ Permisos verificados");

        // 3. Eliminar imágenes de Tebi y BD (SIN validar userId nuevamente)
        try {
            captureImageService.deleteAllImagesByCaptureInternal(idFishCapture);
            log.info("✅ Imágenes eliminadas correctamente");
        } catch (Exception e) {
            log.error("⚠️ Error eliminando imágenes: {}", e.getMessage(), e);
            // Decidir si continuar o lanzar excepción
            // throw new RuntimeException("Error al eliminar imágenes", e);
        }

        // 4. Eliminar la captura de la BD
        fishCaptureRepository.delete(fishCapture);
        fishCaptureRepository.flush(); // Forzar ejecución inmediata

        log.info("✅ Captura {} eliminada exitosamente", idFishCapture);
    }
}
