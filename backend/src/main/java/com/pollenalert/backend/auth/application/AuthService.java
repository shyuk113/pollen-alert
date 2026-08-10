package com.pollenalert.backend.auth.application;

import com.pollenalert.backend.auth.application.dto.LoginRequestDto;
import com.pollenalert.backend.auth.application.dto.SignupRequestDto;
import com.pollenalert.backend.auth.application.dto.SignupResponseDto;
import com.pollenalert.backend.auth.application.dto.TokenResponseDto;
import com.pollenalert.backend.global.exception.BusinessException;
import com.pollenalert.backend.global.exception.ErrorCode;
import com.pollenalert.backend.global.jwt.JwtTokenProvider;
import com.pollenalert.backend.global.redis.RedisTokenService;
import com.pollenalert.backend.member.domain.User;
import com.pollenalert.backend.member.infrastructure.UserRepository;
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
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = User.createUserLocal(request.email(), encodedPassword,request.name());
        userRepository.save(user);

        return SignupResponseDto.from(user);
    }

    //로그인
    public TokenResponseDto login(LoginRequestDto request){
        User user = userRepository.findByEmail(request.email()).orElseThrow(()->new BusinessException(ErrorCode.USER_NOT_FOUND));

        //비밀 번호 검증
        if (!passwordEncoder.matches(request.password(), user.getPassword())){
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        return issueTokens(user,false);
    }

    //토큰 재발급
    public TokenResponseDto refresh(String refreshToken){
        if (!jwtTokenProvider.validateToken(refreshToken)){
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        Long userId =jwtTokenProvider.getUserId(refreshToken);

        String savedToken = redisTokenService.getRefreshToken(userId);
        if (savedToken == null || !savedToken.equals(refreshToken)){
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        User user = userRepository.findById(userId).orElseThrow(()-> new BusinessException(ErrorCode.USER_NOT_FOUND));
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
