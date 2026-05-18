package com.pollenalert.backend.alert.service;

import com.pollenalert.backend.alert.domain.AlertSetting;
import com.pollenalert.backend.alert.dto.AlertHistoryResponseDto;
import com.pollenalert.backend.alert.dto.AlertSettingRequestDto;
import com.pollenalert.backend.alert.dto.AlertSettingResponseDto;
import com.pollenalert.backend.alert.repository.AlertHistoryRepository;
import com.pollenalert.backend.alert.repository.AlertSettingRepository;
import com.pollenalert.backend.member.domain.User;
import com.pollenalert.backend.member.repository.UserRepository;
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
        User user = userRepository.findById(userId).orElseThrow(()->new IllegalArgumentException("존재하지 않는 유저입니다."));

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
        AlertSetting setting = alertSettingRepository.findByUser_id(userId).orElseThrow(()->new IllegalArgumentException("알림 설정이 없습니다."));
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
