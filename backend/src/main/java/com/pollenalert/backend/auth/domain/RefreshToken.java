package com.pollenalert.backend.auth.domain;

import com.pollenalert.backend.member.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable =false)
    private String token;

    @Column(name = "expires_at",nullable = false)
    private LocalDateTime expiresAt;

    //정적 팩토리 메서드
    public static RefreshToken create(User user, String token, LocalDateTime expiresAt){
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.user = user;
        refreshToken.token = token;
        refreshToken.expiresAt = expiresAt;
        return refreshToken;
    }

    //토큰 갱신
    public void update(String token, LocalDateTime expiresAt){
        this.token = token;
        this.expiresAt = expiresAt;
    }

    //만료 여부 확인
    public boolean isExpired(){
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
