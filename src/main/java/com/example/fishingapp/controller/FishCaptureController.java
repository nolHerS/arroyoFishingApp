package com.example.fishingapp.controller;

import com.example.fishingapp.dto.FishCaptureDto;
import com.example.fishingapp.service.FishCaptureService;
import com.example.fishingapp.service.impl.FishCaptureServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fishCaptures")
public class FishCaptureController {

    @Autowired
    private FishCaptureService fishCaptureService;

    @PostMapping("/{username}")
    public ResponseEntity<FishCaptureDto> createCapture(@RequestBody FishCaptureDto fishCapture, @PathVariable Long username) {
        return new ResponseEntity<>(fishCaptureService.createFishCapture(fishCapture,username),HttpStatus.OK);
    }
    @PostMapping("/{idFishCapture}")
    public ResponseEntity<FishCaptureDto> findFishCaptureById(@PathVariable Long idFishCapture){
        return new ResponseEntity<>(fishCaptureService.findById(idFishCapture), HttpStatus.OK);
    }

    @GetMapping("/{username}")
    public ResponseEntity<List<FishCaptureDto>> getFishCaptureByUsername(@PathVariable String username){
        return new ResponseEntity<>(fishCaptureService.getAllFishCapturesByUsername(username),HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<FishCaptureDto>> getAllCaptures() {
        return new ResponseEntity<>(fishCaptureService.getAllFishCapture(), HttpStatus.OK);
    }

   @PutMapping
    public ResponseEntity<FishCaptureDto> updateFishCapture(@RequestBody FishCaptureDto fishCaptureDto){
        return new ResponseEntity<>(fishCaptureService.updateFishCaptureDto(fishCaptureDto), HttpStatus.OK);
   }

   @DeleteMapping("/{idFishCapture}")
    public ResponseEntity<String> deleteFishCapture(@PathVariable Long idFishCapture){
        FishCaptureDto findFishCaptureDto = fishCaptureService.findById(idFishCapture);
        fishCaptureService.deleteFishCaptureDto(idFishCapture);
        return new ResponseEntity<>("Captura borrada: "+findFishCaptureDto.toString(),HttpStatus.OK);
   }

}
