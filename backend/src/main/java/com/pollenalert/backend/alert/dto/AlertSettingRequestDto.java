package com.pollenalert.backend.alert.dto;

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
        String notifyTime,

        String fcmToken
) {
}
