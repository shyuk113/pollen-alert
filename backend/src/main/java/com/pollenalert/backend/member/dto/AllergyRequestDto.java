package com.pollenalert.backend.member.dto;

import java.util.List;

public record AllergyRequestDto(boolean hasPollenAllergy, List<String> types) {
}
