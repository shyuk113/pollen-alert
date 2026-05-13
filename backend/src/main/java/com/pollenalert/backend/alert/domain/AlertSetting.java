package com.pollenalert.backend.alert.domain;

import com.pollenalert.backend.member.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Table(name = "alert_setting")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EntityListeners(AuditingEntityListener.class)
public class AlertSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,unique = true)
    private User user;

    @Column(nullable = false)
    private boolean enabled; //알림 온오프

    @Column(nullable = false)
    private int threshold; //알림 발송 등급 (0~3)

    @Column(name = "notify_days_before", nullable = false)
    private int notifyDaysBefore; //며칠전 알림

    @Column(name = "notify_time",nullable = false)
    private String notifyTime;

    @Column(name = "fcm_token")
    private String fcmToken;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static AlertSetting createAlertSetting(User user, boolean enabled, int threshold, int notifyDaysBefore, String notifyTime) {
        AlertSetting alertSetting = new AlertSetting();
        alertSetting.user = user;
        alertSetting.enabled = false; //기본값 얼람off
        alertSetting.threshold = 1; //기본값 보통
        alertSetting.notifyDaysBefore = 3; //기본값 3일
        alertSetting.notifyTime = "08:00"; //기본값 오전8시
        return alertSetting;
    }

    public void updateAlertSetting(boolean enabled, int threshold, int notifyDaysBefore, String notifyTime) {
        this.enabled = enabled;
        this.threshold = threshold;
        this.notifyDaysBefore = notifyDaysBefore;
        this.notifyTime = notifyTime;
    }

    public void updateFcmToken(String fcmToken){
        this.fcmToken = fcmToken;
    }
}
