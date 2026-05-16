package com.pollenalert.backend.pollen.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pollen_data",
uniqueConstraints = @UniqueConstraint(columnNames = {"region", "forecast_date","source"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class PollenData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String region;

    @Column(name = "forecast_date", nullable = false)
    private LocalDate forecastDate;

    @Column(nullable = false)
    private int level;  //0, 1, 2, 3

    @Column(nullable = false)
    private String grade;   //낮음, 보통, 높음, 매우높음

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Source source;

    @CreatedDate
    @Column(name = "collected_at", updatable = false)
    private LocalDateTime collectedAt;

    public static PollenData create(String region, LocalDate forecastDate, int level, String grade, Source source) {
        PollenData pollenData = new PollenData();
        pollenData.region = region;
        pollenData.forecastDate = forecastDate;
        pollenData.level = level;
        pollenData.grade = grade;
        pollenData.source = source;
        return pollenData;
    }
}
