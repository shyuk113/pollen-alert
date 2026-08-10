package com.pollenalert.backend.alert.application;

import com.pollenalert.backend.alert.domain.AlertSetting;
import com.pollenalert.backend.alert.application.dto.AlertHistoryResponseDto;
import com.pollenalert.backend.alert.application.dto.AlertSettingRequestDto;
import com.pollenalert.backend.alert.application.dto.AlertSettingResponseDto;
import com.pollenalert.backend.alert.infrastructure.AlertHistoryRepository;
import com.pollenalert.backend.alert.infrastructure.AlertSettingRepository;
import com.pollenalert.backend.global.exception.BusinessException;
import com.pollenalert.backend.global.exception.ErrorCode;
import com.pollenalert.backend.member.domain.User;
import com.pollenalert.backend.member.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertHistoryRepository alertHistoryRepository;
    private final AlertSettingRepository alertSettingRepository;
    private final UserRepository userRepository;


    //알림 설정 저장
    @Transactional
    public AlertSettingResponseDto saveAlertSetting(Long userId, AlertSettingRequestDto request){
        User user = userRepository.findById(userId).orElseThrow(()->new BusinessException(ErrorCode.USER_NOT_FOUND));

        AlertSetting setting = alertSettingRepository.findByUser_id(userId).orElse(null);

        if (setting == null) {
            setting = AlertSetting.createAlertSetting(user, request.enabled(), request.threshold(), request.notifyDaysBefore(), request.notifyTime(), request.fcmToken());
            alertSettingRepository.save(setting);
        } else {
            setting.updateAlertSetting(request.enabled(), request.threshold(), request.notifyDaysBefore(), request.notifyTime(), request.fcmToken());
        }

        return AlertSettingResponseDto.from(setting);
    }

    //알림 설정 조회
    @Transactional(readOnly = true)
    public AlertSettingResponseDto getAlertSetting(Long userId){
        AlertSetting setting = alertSettingRepository.findByUser_id(userId).orElseThrow(()->new BusinessException(ErrorCode.ALERT_SETTING_NOT_FOUND));
        return AlertSettingResponseDto.from(setting);
    }

    //알림 기록 조회
    @Transactional(readOnly = true)
    public List<AlertHistoryResponseDto> getAlertHistory(Long userId){
        return alertHistoryRepository.findByUser_idOrderBySentAtDesc(userId, PageRequest.of(0,20)).stream()
                .map(AlertHistoryResponseDto::from)
                .toList();
    }



}
