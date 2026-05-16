package com.pollenalert.backend.member.dto;

import com.pollenalert.backend.member.domain.User;

public record MemberResponseDto(Long userId, String email, String name, String region, String provider) {

    public static MemberResponseDto from(User user){
        return new MemberResponseDto(user.getId(), user.getEmail(), user.getName(), user.getRegion(), user.getProvider().name());
    }
}
