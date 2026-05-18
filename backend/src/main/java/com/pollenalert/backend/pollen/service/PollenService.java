package com.pollenalert.backend.pollen.service;

import com.pollenalert.backend.member.domain.AllergySetting;
import com.pollenalert.backend.member.repository.AllergySettingRepository;
import com.pollenalert.backend.pollen.domain.PollenData;
import com.pollenalert.backend.pollen.domain.RegionCode;
import com.pollenalert.backend.pollen.dto.PollenForecastResponseDto;
import com.pollenalert.backend.pollen.dto.PollenResponseDto;
import com.pollenalert.backend.pollen.dto.PollenTypeResponseDto;
import com.pollenalert.backend.pollen.repository.PollenDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PollenService {

    private final PollenDataRepository pollenDataRepository;
    private final AllergySettingRepository allergySettingRepository;


    //꽃가루 지수 조회
    @Transactional(readOnly = true)
    public PollenResponseDto getPollen(String region, Long userId){

        AllergySetting setting = allergySettingRepository.findByUser_id(userId).orElseThrow(()-> new IllegalArgumentException("알러지 설정이 없습니다."));

        List<String> types = Arrays.asList(setting.getTypes().split(","));
        LocalDate today = LocalDate.now();

        List<PollenData> dataList= pollenDataRepository.findByRegionAndForecastDateAndPollenTypeIn(region, today, types);

        List<PollenTypeResponseDto> pollens = dataList.stream().map(d->new PollenTypeResponseDto(d.getPollenType(), d.getLevel(), d.getGrade())).toList();

        if (dataList.isEmpty()){
            throw new IllegalArgumentException("꽃가루 데이터가 없습니다.");
        }

        return new PollenResponseDto(region,today.toString(), dataList.get(0).getSource().name(), pollens);
    }

    //꽃가루 예보 조회
    @Transactional(readOnly = true)
    public PollenForecastResponseDto getForecast(String region, Long userId){

        AllergySetting allergySetting = allergySettingRepository.findByUser_id(userId).orElseThrow(()-> new IllegalArgumentException("알러지 설정이 없습니다."));

        List<String> types = Arrays.asList(allergySetting.getTypes().split(","));
        LocalDate today = LocalDate.now();

        List<PollenData> dataList = pollenDataRepository.findByRegionAndForecastDateBetweenOrderByForecastDateAsc(region, today, today.plusDays(3));

        Map<LocalDate, List<PollenData>> groupedByDate = dataList.stream().filter(
                d-> types.contains(d.getPollenType())).collect(Collectors.groupingBy(PollenData::getForecastDate));

        List<PollenResponseDto> forecasts = groupedByDate.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .map(entry -> new PollenResponseDto(region, entry.getKey().toString(),
                        entry.getValue().get(0).getSource().name(),
                        entry.getValue().stream().map(d->new PollenTypeResponseDto(d.getPollenType(),d.getLevel(), d.getGrade()))
                                .toList()
                )).toList();

        return new PollenForecastResponseDto(region, forecasts);
    }

    //지역 목록 조회
    public List<String> getRegions(){
        return RegionCode.getRegionNames();
    }
}
