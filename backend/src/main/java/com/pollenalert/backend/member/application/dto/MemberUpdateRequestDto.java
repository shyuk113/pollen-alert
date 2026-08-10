package com.pollenalert.backend.member.application.dto;

import jakarta.validation.constraints.NotBlank;

public record MemberUpdateRequestDto(
        @NotBlank
        String name,
        @NotBlank
        String region) {
}
