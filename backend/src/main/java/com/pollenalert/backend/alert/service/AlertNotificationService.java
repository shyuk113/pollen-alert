package com.pollenalert.backend.alert.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.pollenalert.backend.alert.domain.AlertHistory;
import com.pollenalert.backend.alert.domain.AlertSetting;
import com.pollenalert.backend.alert.domain.AlertType;
import com.pollenalert.backend.alert.repository.AlertHistoryRepository;
import com.pollenalert.backend.alert.repository.AlertSettingRepository;
import com.pollenalert.backend.member.domain.AllergySetting;
import com.pollenalert.backend.member.repository.AllergySettingRepository;
import com.pollenalert.backend.pollen.domain.PollenData;
import com.pollenalert.backend.pollen.repository.PollenDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertNotificationService {

    private final AlertSettingRepository alertSettingRepository;
    private final AlertHistoryRepository alertHistoryRepository;
    private final AllergySettingRepository allergySettingRepository;
    private final PollenDataRepository pollenDataRepository;

    // 매일 설정된 시간에 가장 근접한 정각(00분)에 실행 — 실제 알림 시간 필터링은 내부에서 처리
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void sendScheduledAlerts() {
        String currentHour = String.format("%02d:00", LocalDateTime.now().getHour());
        log.info("알림 스케줄러 실행: {}", currentHour);

        List<AlertSetting> settings = alertSettingRepository.findAllByEnabledTrue();

        for (AlertSetting setting : settings) {
            if (!currentHour.equals(setting.getNotifyTime())) {
                continue;
            }
            try {
                processUserAlert(setting);
            } catch (Exception e) {
                log.error("알림 처리 실패: userId={}, error={}", setting.getUser().getId(), e.getMessage());
            }
        }
    }

    private void processUserAlert(AlertSetting setting) {
        Long userId = setting.getUser().getId();
        String region = setting.getUser().getRegion();

        if (region == null || region.isBlank()) {
            log.debug("지역 미설정 유저 스킵: userId={}", userId);
            return;
        }

        Optional<AllergySetting> allergyOpt = allergySettingRepository.findByUser_id(userId);
        if (allergyOpt.isEmpty() || !allergyOpt.get().isHasPollenAllergy()) {
            return;
        }

        List<String> types = Arrays.asList(allergyOpt.get().getTypes().split(","));
        LocalDate today = LocalDate.now();

        // 사용자 threshold 이상인 꽃가루 데이터 확인
        List<PollenData> highPollenData = pollenDataRepository
                .findByRegionAndForecastDateAndPollenTypeIn(region, today, types)
                .stream()
                .filter(d -> d.getLevel() >= setting.getThreshold())
                .toList();

        if (highPollenData.isEmpty()) {
            // 이전에 TODAY 알림을 보낸 적 있다면 CLEAR 알림 발송
            boolean hadAlert = alertHistoryRepository.existsByUser_idAndAlertTypeAndSentAtAfter(
                    userId, AlertType.TODAY, LocalDateTime.now().minusDays(1));
            if (hadAlert) {
                sendAlert(setting, AlertType.CLEAR, 0, "꽃가루 경보 해제", "오늘은 꽃가루 농도가 낮아졌습니다.");
            }
            return;
        }

        int maxLevel = highPollenData.stream().mapToInt(PollenData::getLevel).max().orElse(0);

        // 당일 알림 중복 방지
        boolean alreadySent = alertHistoryRepository.existsByUser_idAndAlertTypeAndSentAtAfter(
                userId, AlertType.TODAY, LocalDateTime.now().minusHours(12));
        if (!alreadySent) {
            sendAlert(setting, AlertType.TODAY, maxLevel, "꽃가루 경보", buildAlertBody(highPollenData, maxLevel));
        }

        // D-1, D-3 예보 알림
        if (setting.getNotifyDaysBefore() >= 1) {
            checkForecastAlert(setting, today.plusDays(1), AlertType.D_1, types);
        }
        if (setting.getNotifyDaysBefore() >= 3) {
            checkForecastAlert(setting, today.plusDays(3), AlertType.D_3, types);
        }
    }

    private void checkForecastAlert(AlertSetting setting, LocalDate targetDate, AlertType alertType, List<String> types) {
        List<PollenData> forecast = pollenDataRepository
                .findByRegionAndForecastDateAndPollenTypeIn(setting.getUser().getRegion(), targetDate, types)
                .stream()
                .filter(d -> d.getLevel() >= setting.getThreshold())
                .toList();

        if (forecast.isEmpty()) return;

        boolean alreadySent = alertHistoryRepository.existsByUser_idAndAlertTypeAndSentAtAfter(
                setting.getUser().getId(), alertType, LocalDateTime.now().minusHours(20));
        if (alreadySent) return;

        int maxLevel = forecast.stream().mapToInt(PollenData::getLevel).max().orElse(0);
        String title = alertType == AlertType.D_1 ? "내일 꽃가루 예보" : "3일 후 꽃가루 예보";
        sendAlert(setting, alertType, maxLevel, title, buildAlertBody(forecast, maxLevel));
    }

    private void sendAlert(AlertSetting setting, AlertType alertType, int level, String title, String body) {
        String fcmToken = setting.getFcmToken();
        if (fcmToken == null || fcmToken.isBlank()) {
            log.debug("FCM 토큰 없음: userId={}", setting.getUser().getId());
            return;
        }

        try {
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putData("alertType", alertType.name())
                    .putData("alertLevel", String.valueOf(level))
                    .build();

            FirebaseMessaging.getInstance().send(message);
            log.info("FCM 알림 발송 성공: userId={}, type={}", setting.getUser().getId(), alertType);
        } catch (FirebaseMessagingException e) {
            log.error("FCM 알림 발송 실패: userId={}, type={}, error={}", setting.getUser().getId(), alertType, e.getMessage());
            return;
        }

        AlertHistory history = AlertHistory.createAlertHistory(setting.getUser(), alertType, level);
        alertHistoryRepository.save(history);
    }

    private String buildAlertBody(List<PollenData> data, int maxLevel) {
        String grade = switch (maxLevel) {
            case 1 -> "보통";
            case 2 -> "높음";
            case 3 -> "매우 높음";
            default -> "낮음";
        };
        String types = data.stream()
                .map(d -> switch (d.getPollenType()) {
                    case "oak" -> "참나무";
                    case "pine" -> "소나무";
                    case "weed" -> "잡초류";
                    default -> d.getPollenType();
                })
                .distinct()
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        return types + " 꽃가루 농도가 " + grade + " 수준입니다. 외출 시 주의하세요.";
    }
}
