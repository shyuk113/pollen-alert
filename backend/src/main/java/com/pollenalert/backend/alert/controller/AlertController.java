package com.pollenalert.backend.alert.controller;

import com.pollenalert.backend.alert.dto.AlertHistoryResponseDto;
import com.pollenalert.backend.alert.dto.AlertSettingRequestDto;
import com.pollenalert.backend.alert.dto.AlertSettingResponseDto;
import com.pollenalert.backend.alert.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    //알림 설정 저장
    @PostMapping("/setting")
    public ResponseEntity<AlertSettingResponseDto> saveAlertSetting(@AuthenticationPrincipal Long userId, @RequestBody AlertSettingRequestDto request){
        return ResponseEntity.ok(alertService.saveAlertSetting(userId,request));
    }

    //알림 설정 조회
    @GetMapping("/setting")
    public ResponseEntity<AlertSettingResponseDto> getAlertSetting(@AuthenticationPrincipal
                                                                    Long userId){
        return ResponseEntity.ok(alertService.getAlertSetting(userId));
    }

    //알림 기록 조회
    @GetMapping("/history")
    public ResponseEntity<List<AlertHistoryResponseDto>> getAlertHistory(@AuthenticationPrincipal Long userId){
        return ResponseEntity.ok(alertService.getAlertHistory(userId));
    }
}
