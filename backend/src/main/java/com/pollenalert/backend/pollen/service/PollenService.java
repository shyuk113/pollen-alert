package com.pollenalert.backend.pollen.service;

import com.pollenalert.backend.pollen.domain.PollenData;
import com.pollenalert.backend.pollen.domain.RegionCode;
import com.pollenalert.backend.pollen.dto.PollenForecastResponseDto;
import com.pollenalert.backend.pollen.dto.PollenResponseDto;
import com.pollenalert.backend.pollen.repository.PollenDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PollenService {

    private final PollenDataRepository pollenDataRepository;

    //꽃가루 지수 조회
    @Transactional(readOnly = true)
    public PollenResponseDto getPollen(String region){
        PollenData data = pollenDataRepository.findByRegionAndForecastDate(region, LocalDate.now()).orElseThrow(()->new IllegalArgumentException("꽃가루 데이터가 없습니다."));
        return PollenResponseDto.from(data);
    }

    //꽃가루 예보 조회
    @Transactional(readOnly = true)
    public PollenForecastResponseDto getForecast(String region){
        LocalDate today = LocalDate.now();
        List<PollenData> forecasts = pollenDataRepository.findByRegionAndForecastDateBetweenOrderByForecastDateAsc(region, today, today.plusDays(3));


        List<PollenResponseDto> responses = forecasts.stream().map(PollenResponseDto::from).collect(Collectors.toList());

        return new PollenForecastResponseDto(region,responses);
    }

    //지역 목록 조회
    public List<String> getRegions(){
        return RegionCode.getRegionNames();
    }
}
