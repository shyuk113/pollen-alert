package com.pollenalert.backend.auth.repository;

import com.pollenalert.backend.auth.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken>  findByUserId(Long userId);

    Optional<RefreshToken> findByToken(String token);

    void deleteByUserId(Long userId);
}
