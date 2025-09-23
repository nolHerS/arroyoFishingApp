package com.example.fishingapp.controller;

import com.example.fishingapp.model.FishCapture;
import com.example.fishingapp.repository.FishCaptureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/captures")
public class FishCaptureController {

    @Autowired
    private FishCaptureRepository fishCaptureRepository;

    //GET :: Todas las capturas
    @GetMapping
    public List<FishCapture> getAllCaptures() {
        return fishCaptureRepository.findAll();
    }

    @GetMapping("/{captureId}")
    public FishCapture getOneCapture(@PathVariable Long captureId){
        return fishCaptureRepository.findById(captureId).orElseThrow();
    }

    @PostMapping
    public FishCapture createCapture(@RequestBody FishCapture capture) {
        return fishCaptureRepository.save(capture);
    }

}
