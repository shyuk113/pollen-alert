package com.pollenalert.backend.auth.controller;

import com.pollenalert.backend.auth.dto.LoginRequestDto;
import com.pollenalert.backend.auth.dto.SignupRequestDto;
import com.pollenalert.backend.auth.dto.SignupResponseDto;
import com.pollenalert.backend.auth.dto.TokenResponseDto;
import com.pollenalert.backend.auth.service.AuthService;
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

    /*로그아웃 v1 postgresql DB 사용
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Long userId){
        authService.logout(userId);
        return ResponseEntity.noContent().build();
    }*/

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Long userId, @RequestHeader("Authorization") String bearerToken){
        String accessToken = bearerToken.substring(7);
        authService.logout(userId, accessToken);
        return ResponseEntity.noContent().build();
    }

}
