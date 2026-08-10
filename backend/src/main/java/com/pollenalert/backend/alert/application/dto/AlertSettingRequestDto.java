package com.pollenalert.backend.alert.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AlertSettingRequestDto(
        boolean enabled,

        @Min(0) @Max(3)
        int threshold,

        @Min(0) @Max(7)
        int notifyDaysBefore,

        @NotBlank
        @jakarta.validation.constraints.Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "HH:mm 형식이어야 합니다.")
        String notifyTime,

        String fcmToken
) {
}
