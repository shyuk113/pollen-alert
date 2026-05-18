package com.pollenalert.backend.alert.dto;

public record AlertSettingResponseDto(boolean enabled, int threshold, int notifyDaysBefore, String notifyTime, String fcmToken) {
}
