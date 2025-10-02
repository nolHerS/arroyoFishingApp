package com.example.fishingapp.controller;

import com.example.fishingapp.dto.FishCaptureDto;
import com.example.fishingapp.security.AuthUser;
import com.example.fishingapp.service.FishCaptureService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fishCaptures")
public class FishCaptureController {

    private final FishCaptureService fishCaptureService;

    public FishCaptureController(FishCaptureService fishCaptureService) {
        this.fishCaptureService = fishCaptureService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FishCaptureDto> createCapture(
            @RequestBody FishCaptureDto fishCapture,
            @AuthenticationPrincipal AuthUser authUser) {

        Long userId = authUser.getUser().getId();
        return new ResponseEntity<>(
                fishCaptureService.createFishCapture(fishCapture, userId),
                HttpStatus.CREATED
        );
    }

    /**
     * Buscar captura por ID (público o autenticado según prefieras)
     */
    @GetMapping("/{idFishCapture}")
    public ResponseEntity<FishCaptureDto> findFishCaptureById(@PathVariable Long idFishCapture) {
        return new ResponseEntity<>(fishCaptureService.findById(idFishCapture), HttpStatus.OK);
    }

    /**
     * Obtener todas las capturas de un usuario (público)
     */
    @GetMapping("/user/{username}")
    public ResponseEntity<List<FishCaptureDto>> getFishCaptureByUsername(@PathVariable String username) {
        return new ResponseEntity<>(
                fishCaptureService.getAllFishCapturesByUsername(username),
                HttpStatus.OK
        );
    }

    /**
     * Obtener todas las capturas (público para que todos vean)
     */
    @GetMapping
    public ResponseEntity<List<FishCaptureDto>> getAllCaptures() {
        return new ResponseEntity<>(fishCaptureService.getAllFishCapture(), HttpStatus.OK);
    }

    /**
     * Actualizar captura (solo el dueño o admin)
     */
    @PutMapping("/{idFishCapture}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FishCaptureDto> updateFishCapture(
            @PathVariable Long idFishCapture,
            @RequestBody FishCaptureDto fishCaptureDto,
            @AuthenticationPrincipal AuthUser authUser) {

        // TODO: Validar que el usuario sea el dueño de la captura
        return new ResponseEntity<>(
                fishCaptureService.updateFishCaptureDto(fishCaptureDto),
                HttpStatus.OK
        );
    }

    /**
     * Eliminar captura (solo el dueño o admin)
     */
    @DeleteMapping("/{idFishCapture}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> deleteFishCapture(
            @PathVariable Long idFishCapture,
            @AuthenticationPrincipal AuthUser authUser) {

        // TODO: Validar que el usuario sea el dueño de la captura
        FishCaptureDto fishCaptureDto = fishCaptureService.findById(idFishCapture);
        fishCaptureService.deleteFishCaptureDto(idFishCapture);
        return new ResponseEntity<>(
                "Captura borrada: " + fishCaptureDto.toString(),
                HttpStatus.OK
        );
    }
}
