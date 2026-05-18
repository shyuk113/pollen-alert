package com.pollenalert.backend.alert.dto;

import com.pollenalert.backend.alert.domain.AlertHistory;

import java.time.format.DateTimeFormatter;

public record AlertHistoryResponseDto(Long id, String alertType, int alertLevel, String sentAt) {

    public static AlertHistoryResponseDto from(AlertHistory history){
        return new AlertHistoryResponseDto(
                history.getId(),
                history.getAlertType().toString(),
                history.getAlertLevel(),
                history.getSentAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }
}
