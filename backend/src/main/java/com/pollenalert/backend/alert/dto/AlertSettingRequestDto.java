package com.pollenalert.backend.alert.dto;

public record AlertSettingRequestDto(boolean enabled, int threshold, int notifyDaysBefore, String notifyTime, String fcmToken) {
}
