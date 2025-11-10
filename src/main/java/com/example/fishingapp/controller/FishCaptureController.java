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
@RequestMapping("/api/fish-captures")
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

    @GetMapping("/{idFishCapture}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FishCaptureDto> findFishCaptureById(@PathVariable Long idFishCapture) {
        return new ResponseEntity<>(fishCaptureService.findById(idFishCapture), HttpStatus.OK);
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<List<FishCaptureDto>> getFishCaptureByUsername(@PathVariable String username) {
        return new ResponseEntity<>(
                fishCaptureService.getAllFishCapturesByUsername(username),
                HttpStatus.OK
        );
    }

    @GetMapping
    public ResponseEntity<List<FishCaptureDto>> getAllCaptures() {
        return new ResponseEntity<>(fishCaptureService.getAllFishCapture(), HttpStatus.OK);
    }

    @PutMapping("/{idFishCapture}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FishCaptureDto> updateFishCapture(
            @PathVariable Long idFishCapture,
            @RequestBody FishCaptureDto fishCaptureDto,
            @AuthenticationPrincipal AuthUser authUser) {

        Long userId = authUser.getUser().getId();
        // Pasar el userId para validación
        return new ResponseEntity<>(
                fishCaptureService.updateFishCaptureDto(fishCaptureDto, idFishCapture, authUser),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/{idFishCapture}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FishCaptureDto> deleteFishCapture(
            @PathVariable Long idFishCapture,
            @AuthenticationPrincipal AuthUser authUser) {

        Long userId = authUser.getUser().getId();
        FishCaptureDto fishCaptureDto = fishCaptureService.findById(idFishCapture);
        // Pasar el userId para validación
        fishCaptureService.deleteFishCaptureDto(idFishCapture, userId);
        return new ResponseEntity<>(
                fishCaptureDto,
                HttpStatus.OK
        );
    }
}