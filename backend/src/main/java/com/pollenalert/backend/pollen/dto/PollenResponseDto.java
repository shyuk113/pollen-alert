package com.pollenalert.backend.pollen.dto;

import com.pollenalert.backend.pollen.domain.PollenData;
import com.pollenalert.backend.pollen.domain.Source;

import java.time.format.DateTimeFormatter;
import java.util.List;

public record PollenResponseDto(String region, String forecastDate, String source, List<PollenTypeResponseDto> pollens) {}
