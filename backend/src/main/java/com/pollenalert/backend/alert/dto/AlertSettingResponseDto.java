package com.pollenalert.backend.alert.dto;

import com.pollenalert.backend.alert.domain.AlertSetting;

public record AlertSettingResponseDto(boolean enabled, int threshold, int notifyDaysBefore, String notifyTime, String fcmToken) {

    public static AlertSettingResponseDto from(AlertSetting setting){
        return new AlertSettingResponseDto(
                setting.isEnabled(),
                setting.getThreshold(),
                setting.getNotifyDaysBefore(),
                setting.getNotifyTime(),
                setting.getFcmToken()
        );
    }
}
