package com.pollenalert.backend.member.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String password;

    @Column(nullable = false)
    private String name;

    @Column
    private String region;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;

    @Column(name = "provider_id")
    String provider_id;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    //정적 팩토리 메서드
    public static User createUserLocal(String email, String password, String name){
        User user = new User();
        user.email = email;
        user.password = password;
        user.name = name;
        user.provider = Provider.LOCAL;
        return user;
    }

    public static User createUserSocial(String email, String name, Provider provider, String providerId){
        User user = new User();
        user.email = email;
        user.name = name;
        user.provider = provider;
        user.provider_id = providerId;
        return user;
    }

    //수정 메서드
    public void updateProfile(String name, String region){
        this.name = name;
        this.region = region;
    }

}
