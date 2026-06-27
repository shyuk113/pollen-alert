package com.pollenalert.backend.auth.service;

import com.pollenalert.backend.auth.dto.LoginRequestDto;
import com.pollenalert.backend.auth.dto.SignupRequestDto;
import com.pollenalert.backend.auth.dto.SignupResponseDto;
import com.pollenalert.backend.auth.dto.TokenResponseDto;
import com.pollenalert.backend.global.jwt.JwtTokenProvider;
import com.pollenalert.backend.global.redis.RedisTokenService;
import com.pollenalert.backend.member.domain.User;
import com.pollenalert.backend.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTokenService redisTokenService;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

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
    public TokenResponseDto login(LoginRequestDto request){
        User user = userRepository.findByEmail(request.email()).orElseThrow(()->new IllegalArgumentException("존재하지 않는 이메일입니다."));

        //비밀 번호 검증
        if (!passwordEncoder.matches(request.password(), user.getPassword())){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return issueTokens(user,false);
    }

    //토큰 재발급
    public TokenResponseDto refresh(String refreshToken){
        if (!jwtTokenProvider.validateToken(refreshToken)){
            throw new IllegalArgumentException("유효라지 않은 리프레시 토큰입니다.");
        }

        Long userId =jwtTokenProvider.getUserId(refreshToken);

        String savedToken = redisTokenService.getRefreshToken(userId);
        if (savedToken == null || !savedToken.equals(refreshToken)){
            throw new IllegalArgumentException("만료되었거나 유효하지 않은 리프레시 토큰입니다.");
        }

        User user = userRepository.findById(userId).orElseThrow(()-> new IllegalArgumentException("존재하지 않는 유저입니다."));
        return issueTokens(user,false);
    }

    //로그 아웃
    public void logout(Long userId, String accessToken){
        redisTokenService.deleteRefreshToken(userId);   //리프레시 토큰 제거
        redisTokenService.addBlackList(accessToken, accessTokenExpiration / 1000);  //블랙리스트에 추가
    }

    private TokenResponseDto issueTokens(User user, boolean isNewUser){
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        redisTokenService.saveRefreshToken(user.getId(), refreshToken, refreshTokenExpiration / 1000);
        return new TokenResponseDto(accessToken, refreshToken, accessTokenExpiration / 1000, isNewUser);
    }
}
