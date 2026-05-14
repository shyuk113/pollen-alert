package com.pollenalert.backend.auth.controller;

import com.pollenalert.backend.auth.dto.LoginRequestDto;
import com.pollenalert.backend.auth.dto.SignupRequestDto;
import com.pollenalert.backend.auth.dto.SignupResponseDto;
import com.pollenalert.backend.auth.dto.TokenResponseDto;
import com.pollenalert.backend.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
