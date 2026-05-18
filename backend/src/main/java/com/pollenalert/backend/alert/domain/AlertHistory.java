package com.pollenalert.backend.alert.domain;

import com.pollenalert.backend.member.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "alert_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class AlertHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false)
    private AlertType alertType;

    @Column(name = "alert_level", nullable = false)
    private int alertLevel;

    @CreatedDate
    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;


    public static AlertHistory createAlertHistory(User user, AlertType alertType, int alertLevel) {
        AlertHistory alertHistory = new AlertHistory();
        alertHistory.user = user;
        alertHistory.alertType = alertType;
        alertHistory.alertLevel = alertLevel;
        return alertHistory;
    }


}
