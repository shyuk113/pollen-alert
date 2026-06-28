package com.pollenalert.backend.auth.controller;

import com.pollenalert.backend.auth.dto.TokenResponseDto;
import com.pollenalert.backend.auth.service.OAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthService oAuthService;

    // ── 카카오 로그인 ──────────────────────────────────
    @PostMapping("/kakao")
    public ResponseEntity<TokenResponseDto> kakaoLogin(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(oAuthService.kakaoLogin(body.get("code")));
    }

    // ── 네이버 로그인 ──────────────────────────────────
    @PostMapping("/naver")
    public ResponseEntity<TokenResponseDto> naverLogin(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(oAuthService.naverLogin(body.get("code"), body.get("state")));
    }

    // ── 구글 로그인 ────────────────────────────────────
    @PostMapping("/google")
    public ResponseEntity<TokenResponseDto> googleLogin(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(oAuthService.googleLogin(body.get("code")));
    }
}
