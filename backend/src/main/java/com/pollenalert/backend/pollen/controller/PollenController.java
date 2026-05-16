package com.pollenalert.backend.pollen.controller;

import com.pollenalert.backend.pollen.dto.PollenForecastResponseDto;
import com.pollenalert.backend.pollen.dto.PollenResponseDto;
import com.pollenalert.backend.pollen.service.PollenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/pollen")
@RequiredArgsConstructor
public class PollenController {

    private final PollenService pollenService;

    //꽃가루 조회
    @GetMapping
    public ResponseEntity<PollenResponseDto> getPollen(@RequestParam String region, @AuthenticationPrincipal Long userId){
        return ResponseEntity.ok(pollenService.getPollen(region, userId));
    }

    //꽃가루 예보 조회
    @GetMapping("/forecast")
    public ResponseEntity<PollenForecastResponseDto> getForecast(@RequestParam String region, @AuthenticationPrincipal Long userId){
        return ResponseEntity.ok(pollenService.getForecast(region, userId));
    }

    //지역 조회
    @GetMapping("/regions")
    public ResponseEntity<List<String>> getRegions(){
        return ResponseEntity.ok(pollenService.getRegions());
    }
}
