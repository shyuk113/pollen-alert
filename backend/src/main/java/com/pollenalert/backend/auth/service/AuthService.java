package com.pollenalert.backend.auth.service;

import com.pollenalert.backend.auth.domain.RefreshToken;
import com.pollenalert.backend.auth.dto.LoginRequestDto;
import com.pollenalert.backend.auth.dto.SignupRequestDto;
import com.pollenalert.backend.auth.dto.SignupResponseDto;
import com.pollenalert.backend.auth.dto.TokenResponseDto;
import com.pollenalert.backend.auth.repository.RefreshTokenRepository;
import com.pollenalert.backend.global.jwt.JwtTokenProvider;
import com.pollenalert.backend.member.domain.User;
import com.pollenalert.backend.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Transactional //회원가입
    public SignupResponseDto signup(SignupRequestDto request){
        if(userRepository.existsByEmail(request.email())){
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = User.createUserLocal(request.email(), encodedPassword,request.name());
        userRepository.save(user);

        return SignupResponseDto.from(user);
    }

    //로그인
    @Transactional
    public TokenResponseDto login(LoginRequestDto request){
        User user = userRepository.findByEmail(request.email()).orElseThrow(()->new IllegalArgumentException("존재하지 않는 이메일입니다."));

        //비밀 번호 검증
        if (!passwordEncoder.matches(request.password(), user.getPassword())){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return issueTokens(user,false);
    }

    //토큰 재발급
    @Transactional
    public TokenResponseDto refresh(String refreshTokenValue){
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(()-> new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다."));

        if(refreshToken.isExpired()){
            refreshTokenRepository.delete(refreshToken);
            throw new IllegalArgumentException("만료된 리프레시 토큰입니다. 다시 로그인 해주세요");
        }

        User user = refreshToken.getUser();
        return issueTokens(user,false);
    }

    //로그 아웃
    @Transactional
    public void logout(Long userId){
        refreshTokenRepository.deleteByUserId(userId);
    }

    //토큰 발급 + RefreshToken DB 저장
    private TokenResponseDto issueTokens(User user, boolean isNewUser){
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000);

        //기본 RefreshToken 있으면 업데이트 없으면 새로 생성
        refreshTokenRepository.findByUserId(user.getId())
                .ifPresentOrElse(rt -> rt.update(newRefreshToken,expiresAt),
                        ()-> refreshTokenRepository.save(RefreshToken.create(user, newRefreshToken, expiresAt)));
        return new TokenResponseDto(accessToken, newRefreshToken, 3600, isNewUser);
    }
}
