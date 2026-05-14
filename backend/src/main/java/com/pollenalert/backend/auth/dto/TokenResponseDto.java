package com.pollenalert.backend.auth.dto;

public record TokenResponseDto(String accessToken, String refreshToken, long expiresIn, Boolean isNewUser){}
