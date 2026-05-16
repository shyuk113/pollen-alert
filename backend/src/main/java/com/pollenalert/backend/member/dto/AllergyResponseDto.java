package com.pollenalert.backend.member.dto;

import com.pollenalert.backend.member.domain.AllergySetting;

import java.util.Arrays;
import java.util.List;

public record AllergyResponseDto(boolean hasPollenAllergy, List<String> types) {

    public static AllergyResponseDto from(AllergySetting setting){
        List<String> typeList = setting.getTypes() != null ? Arrays.asList(setting.getTypes().split(",")) : List.of();

        return new AllergyResponseDto(setting.isHasPollenAllergy(), typeList);
    }
}
