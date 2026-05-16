package com.pollenalert.backend.pollen.dto;

import com.pollenalert.backend.pollen.domain.PollenData;

import java.time.format.DateTimeFormatter;

public record PollenResponseDto(String region, String forecastDate, int level, String grade, String source) {

    public static PollenResponseDto from(PollenData data){
        return new PollenResponseDto(data.getRegion(), data.getForecastDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                data.getLevel(), data.getGrade(), data.getSource().name());
    }
}
