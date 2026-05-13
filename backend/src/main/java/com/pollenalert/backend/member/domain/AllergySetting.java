package com.pollenalert.backend.member.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Table(name = "allergy_setting")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class AllergySetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "has_pollen_allergy", nullable = false)
    private boolean has_pollen_allergy;

    @Column
    private String types;   //oak, pine, weed 3가지에서 선택

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    //정적 팩토리 메서드
    public static AllergySetting create(User user, boolean hasPollenAllergy, String types){
        AllergySetting setting = new AllergySetting();
        setting.user = user;
        setting.has_pollen_allergy = hasPollenAllergy;
        setting.types = types;
        return setting;
    }

    //수정 메서드
    public void update(boolean hasPollenAllergy, String types){
        this.has_pollen_allergy = hasPollenAllergy;
        this.types = types;
    }
}
