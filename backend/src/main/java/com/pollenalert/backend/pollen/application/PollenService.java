package com.pollenalert.backend.pollen.application;

import com.pollenalert.backend.global.exception.BusinessException;
import com.pollenalert.backend.global.exception.ErrorCode;
import com.pollenalert.backend.member.domain.AllergySetting;
import com.pollenalert.backend.member.infrastructure.AllergySettingRepository;
import com.pollenalert.backend.pollen.domain.PollenData;
import com.pollenalert.backend.pollen.domain.RegionCode;
import com.pollenalert.backend.pollen.application.dto.PollenForecastResponseDto;
import com.pollenalert.backend.pollen.application.dto.PollenResponseDto;
import com.pollenalert.backend.pollen.application.dto.PollenTypeResponseDto;
import com.pollenalert.backend.pollen.infrastructure.PollenDataRepository;
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

        AllergySetting setting = allergySettingRepository.findByUser_id(userId).orElseThrow(()-> new BusinessException(ErrorCode.ALLERGY_NOT_FOUND));

        List<String> types = Arrays.asList(setting.getTypes().split(","));
        LocalDate today = LocalDate.now();

        List<PollenData> dataList= pollenDataRepository.findByRegionAndForecastDateAndPollenTypeIn(region, today, types);

        List<PollenTypeResponseDto> pollens = dataList.stream().map(d->new PollenTypeResponseDto(d.getPollenType(), d.getLevel(), d.getGrade())).toList();

        if (dataList.isEmpty()){
            throw new BusinessException(ErrorCode.POLLEN_DATA_NOT_FOUND);
        }

        return new PollenResponseDto(region,today.toString(), dataList.get(0).getSource().name(), pollens);
    }

    //꽃가루 예보 조회
    @Transactional(readOnly = true)
    public PollenForecastResponseDto getForecast(String region, Long userId){

        AllergySetting allergySetting = allergySettingRepository.findByUser_id(userId).orElseThrow(()-> new BusinessException(ErrorCode.ALLERGY_NOT_FOUND));

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
