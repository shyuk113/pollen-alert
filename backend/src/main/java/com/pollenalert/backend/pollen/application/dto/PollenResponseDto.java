package com.pollenalert.backend.pollen.application.dto;

import java.util.List;

public record PollenResponseDto(String region, String forecastDate, String source, List<PollenTypeResponseDto> pollens) {}
