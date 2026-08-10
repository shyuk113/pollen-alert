package com.pollenalert.backend.auth.application.dto;

import jakarta.validation.constraints.NotBlank;

public record SignupRequestDto(
        @NotBlank
        String name,
        @NotBlank
        String email,
        @NotBlank
        String password){ }
