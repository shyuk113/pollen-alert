package com.pollenalert.backend.auth.dto;

import com.pollenalert.backend.member.domain.User;

public record SignupResponseDto(Long userId, String email, String name) {

    public static SignupResponseDto from(User user){
        return new SignupResponseDto(user.getId(), user.getEmail(), user.getName());
    }
}
