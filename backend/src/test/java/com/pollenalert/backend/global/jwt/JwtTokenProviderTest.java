package com.pollenalert.backend.global.jwt;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private static final String SECRET = "test-secret-key-must-be-at-least-256-bits-long-for-hs256-alg";

    @BeforeEach
    void setUp(){
        jwtTokenProvider = new JwtTokenProvider(SECRET,1000 * 60 * 60, 1000L * 60 * 60 * 24 * 7 );
    }

    @Test
    @DisplayName("엑세스 토큰에 userId와 email claim이 올바르게 들어감")
    void createAccessToken() {
        String token = jwtTokenProvider.createAccessToken(1L,"test@test.com");
        assertThat(jwtTokenProvider.getUserId(token)).isEqualTo(1L);
        assertThat(jwtTokenProvider.getClaims(token).get("email")).isEqualTo("test@test.com");
    }



    @Test
    @DisplayName("정상 토큰은 validateToken이 true")
    void validateToken() {
        String token = jwtTokenProvider.createAccessToken(1L,"test@test.com");
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("만료된 토큰은 validateToken이 false 반환")
    void validateTokenFalse() throws InterruptedException{
        JwtTokenProvider shortLived = new JwtTokenProvider(SECRET,1,1);
        String token = shortLived.createAccessToken(1L, "test@test.com");

        Thread.sleep(10);

        assertThat(shortLived.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("다른 키로 서명된 토큰은 validateToken이 false")
    void validateTokenWrongSignature(){
        JwtTokenProvider otherKeyProvider = new JwtTokenProvider("test-secret-key-must-be-at-least-256-bits-long-for-hs256-abc", 1000 * 60 * 60, 1000L * 60 * 60 * 24 * 7);

        String token = otherKeyProvider.createAccessToken(1L,"test@test.com");
        assertThat(jwtTokenProvider.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("변조된 토큰은 validateToken이 false 반환")
    void validateTokenTampered(){
        String token = jwtTokenProvider.createAccessToken(1L,"test@test.com");
        String tampered = token.substring(0,token.length()-1) + "x";

        assertThat(jwtTokenProvider.validateToken(tampered)).isFalse();
    }

}