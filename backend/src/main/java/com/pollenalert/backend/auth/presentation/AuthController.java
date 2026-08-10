package com.pollenalert.backend.auth.presentation;

import com.pollenalert.backend.auth.application.dto.LoginRequestDto;
import com.pollenalert.backend.auth.application.dto.SignupRequestDto;
import com.pollenalert.backend.auth.application.dto.SignupResponseDto;
import com.pollenalert.backend.auth.application.dto.TokenResponseDto;
import com.pollenalert.backend.auth.application.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    //회원가입
    @PostMapping("/signup")
    public ResponseEntity<SignupResponseDto> signup(@RequestBody SignupRequestDto request){
        return ResponseEntity.ok(authService.signup(request));
    }

    //로그인
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody LoginRequestDto request){
        return ResponseEntity.ok(authService.login(request));
    }

    //토큰 재발급
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDto> refresh(@RequestBody Map<String,String> body){
        String refreshToken = body.get("refreshToken");
        return ResponseEntity.ok(authService.refresh(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Long userId, @RequestHeader("Authorization") String bearerToken){
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new IllegalArgumentException("유효하지 않은 Authorization 헤더입니다.");
        }
        String accessToken = bearerToken.substring(7);
        authService.logout(userId, accessToken);
        return ResponseEntity.noContent().build();
    }

}
