package com.pollenalert.backend.auth.application.dto;

public record TokenResponseDto(String accessToken, String refreshToken, long expiresIn, Boolean isNewUser){}
