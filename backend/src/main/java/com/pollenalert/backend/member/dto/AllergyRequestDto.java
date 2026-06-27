package com.pollenalert.backend.member.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AllergyRequestDto(
        boolean hasPollenAllergy,

        @NotNull
        List<String> types
) {
}
