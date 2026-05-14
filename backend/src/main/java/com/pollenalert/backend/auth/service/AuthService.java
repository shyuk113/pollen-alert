package com.pollenalert.backend.auth.service;

import com.pollenalert.backend.auth.dto.LoginRequestDto;
import com.pollenalert.backend.auth.dto.SignupRequestDto;
import com.pollenalert.backend.auth.dto.SignupResponseDto;
import com.pollenalert.backend.auth.dto.TokenResponseDto;
import com.pollenalert.backend.global.jwt.JwtTokenProvider;
import com.pollenalert.backend.member.domain.User;
import com.pollenalert.backend.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

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
    public TokenResponseDto  login(LoginRequestDto request){
        User user = userRepository.findByEmail(request.email()).orElseThrow(()->new IllegalArgumentException("존재하지 않는 이메일입니다."));

        //비밀 번호 검증
        if (!passwordEncoder.matches(request.password(), user.getPassword())){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // jwt 토큰 발급
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        return new TokenResponseDto(accessToken, refreshToken, 3600, false);
    }
}
