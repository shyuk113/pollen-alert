package com.pollenalert.backend.auth.presentation;

import com.pollenalert.backend.auth.application.dto.TokenResponseDto;
import com.pollenalert.backend.auth.application.OAuthService;
import com.pollenalert.backend.global.exception.BusinessException;
import com.pollenalert.backend.global.exception.ErrorCode;
import com.pollenalert.backend.global.redis.RedisTokenService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthService oAuthService;
    private final RedisTokenService redisTokenService;

    // ── 카카오 로그인 ──────────────────────────────────
    @PostMapping("/kakao")
    public ResponseEntity<TokenResponseDto> kakaoLogin(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(oAuthService.kakaoLogin(body.get("code")));
    }

    @GetMapping("/kakao")
    public ResponseEntity<TokenResponseDto> kakaoCallback(@RequestParam String code, HttpServletResponse response){
        TokenResponseDto token = oAuthService.kakaoLogin(code);

        setAccessTokenCookie(response, token);

        return ResponseEntity.ok(token);
    }

    // ── 네이버 로그인 ──────────────────────────────────
    @PostMapping("/naver")
    public ResponseEntity<TokenResponseDto> naverLogin(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(oAuthService.naverLogin(body.get("code"), body.get("state")));
    }

    @GetMapping("/naver")
    public ResponseEntity<TokenResponseDto> naverCallback(@RequestParam String code, @RequestParam String state, HttpServletResponse response){
        if(!redisTokenService.validateAndConsumeOAuthState(state)){
            throw new BusinessException(ErrorCode.INVALID_OAUTH_STATE);
        }
        TokenResponseDto token = oAuthService.naverLogin(code, state);
        setAccessTokenCookie(response, token);
        return ResponseEntity.ok(token);
    }

    @GetMapping("/naver/authorize-url")
    public ResponseEntity<Map<String,String>> naverAuthorizeUrl(){
        String state = UUID.randomUUID().toString();
        redisTokenService.saveOAuthState(state, 300);

        return ResponseEntity.ok(Map.of("url", oAuthService.getNaverAuthorizeUrl(state)));
    }

    // ── 구글 로그인 ────────────────────────────────────
    @PostMapping("/google")
    public ResponseEntity<TokenResponseDto> googleLogin(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(oAuthService.googleLogin(body.get("code")));
    }

    @GetMapping("/google")
    public ResponseEntity<TokenResponseDto> googleCallback(@RequestParam String code, HttpServletResponse response){
        TokenResponseDto token = oAuthService.googleLogin(code);

        setAccessTokenCookie(response, token);
        return ResponseEntity.ok(token);
    }

    private void setAccessTokenCookie(HttpServletResponse response, TokenResponseDto token){
        ResponseCookie cookie = ResponseCookie.from("accessToken", token.accessToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(token.expiresIn())
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
