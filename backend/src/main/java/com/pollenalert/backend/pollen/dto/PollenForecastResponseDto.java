package com.pollenalert.backend.pollen.dto;

import java.util.List;

public record PollenForecastResponseDto(String region, List<PollenResponseDto> forecasts) {
}
