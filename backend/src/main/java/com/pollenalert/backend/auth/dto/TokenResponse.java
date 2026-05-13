package com.pollenalert.backend.auth.dto;

public record TokenResponse(String accessToken, String refreshToken, long expiresIn, Boolean isNewUser){}
