package com.pollenalert.backend.auth.application.dto;

import jakarta.validation.constraints.NotBlank;
import org.checkerframework.checker.units.qual.N;

public record LoginRequestDto(
        @NotBlank
        String email,
        @NotBlank
        String password){}
