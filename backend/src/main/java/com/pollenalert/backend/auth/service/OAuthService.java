package com.pollenalert.backend.auth.service;

import com.pollenalert.backend.auth.dto.TokenResponseDto;
import com.pollenalert.backend.global.jwt.JwtTokenProvider;
import com.pollenalert.backend.global.redis.RedisTokenService;
import com.pollenalert.backend.member.domain.Provider;
import com.pollenalert.backend.member.domain.User;
import com.pollenalert.backend.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.pollenalert.backend.global.exception.BusinessException;
import com.pollenalert.backend.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTokenService redisTokenService;
    private final WebClient webClient;

    @Value("${oauth.kakao.client-id}")
    private String kakaoClientId;
    @Value("${oauth.kakao.client-secret:}")
    private String kakaoClientSecret;
    @Value("${oauth.kakao.redirect-uri}")
    private String kakaoRedirectUri;
    @Value("${oauth.kakao.token-uri}")
    private String kakaoTokenUri;
    @Value("${oauth.kakao.user-info-uri}")
    private String kakaoUserInfoUri;

    @Value("${oauth.naver.client-id}")
    private String naverClientId;
    @Value("${oauth.naver.client-secret}")
    private String naverClientSecret;
    @Value("${oauth.naver.redirect-uri}")
    private String naverRedirectUri;
    @Value("${oauth.naver.token-uri}")
    private String naverTokenUri;
    @Value("${oauth.naver.user-info-uri}")
    private String naverUserInfoUri;

    @Value("${oauth.google.client-id}")
    private String googleClientId;
    @Value("${oauth.google.client-secret}")
    private String googleClientSecret;
    @Value("${oauth.google.redirect-uri}")
    private String googleRedirectUri;
    @Value("${oauth.google.token-uri}")
    private String googleTokenUri;
    @Value("${oauth.google.user-info-uri}")
    private String googleUserInfoUri;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    // ── 카카오 로그인 ──────────────────────────────────
    @Transactional
    public TokenResponseDto kakaoLogin(String code) {
        String accessToken = getKakaoAccessToken(code);
        Map<String, Object> userInfo = getKakaoUserInfo(accessToken);

        String providerId = String.valueOf(userInfo.get("id"));

        // 이메일 없을 수 있음
        String email = null;
        String name = "카카오 사용자";

        Map<String, Object> kakaoAccount = (Map<String, Object>) userInfo.get("kakao_account");
        if (kakaoAccount != null) {
            email = (String) kakaoAccount.get("email");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            if (profile != null) {
                name = (String) profile.get("nickname");
            }
        }

        // 이메일 없으면 providerId로 대체
        if (email == null) {
            email = providerId + "@kakao.com";
        }

        return processOAuthLogin(email, name, providerId, Provider.KAKAO);
    }

    // ── 네이버 로그인 ──────────────────────────────────
    @Transactional
    public TokenResponseDto naverLogin(String code, String state) {
        // 1. 액세스 토큰 요청
        String accessToken = getNaverAccessToken(code, state);

        // 2. 유저 정보 요청
        Map<String, Object> response = getNaverUserInfo(accessToken);
        Map<String, Object> userInfo = (Map<String, Object>) response.get("response");
        String providerId = (String) userInfo.get("id");
        String email = (String) userInfo.get("email");
        String name = (String) userInfo.get("name");

        return processOAuthLogin(email, name, providerId, Provider.NAVER);
    }

    // ── 구글 로그인 ────────────────────────────────────
    @Transactional
    public TokenResponseDto googleLogin(String code) {
        // 1. 액세스 토큰 요청
        String accessToken = getGoogleAccessToken(code);

        // 2. 유저 정보 요청
        Map<String, Object> userInfo = getGoogleUserInfo(accessToken);
        String providerId = (String) userInfo.get("id");
        String email = (String) userInfo.get("email");
        String name = (String) userInfo.get("name");

        return processOAuthLogin(email, name, providerId, Provider.GOOGLE);
    }

    // ── 공통: 소셜 로그인 처리 ─────────────────────────
    private TokenResponseDto processOAuthLogin(String email, String name, String providerId, Provider provider) {
        boolean[] isNew = {false};

        User user = userRepository.findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> {
                    isNew[0] = true;
                    User newUser = User.createUserSocial(email, name, provider, providerId);
                    return userRepository.save(newUser);
                });

        // JWT 토큰 발급
        String jwtAccessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        String jwtRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        redisTokenService.saveRefreshToken(user.getId(), jwtRefreshToken, refreshTokenExpiration / 1000);

        return new TokenResponseDto(jwtAccessToken, jwtRefreshToken, accessTokenExpiration / 1000, isNew[0]);
    }

    // ── 카카오 토큰 요청 ───────────────────────────────
    private String getKakaoAccessToken(String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoClientId);
        params.add("redirect_uri", kakaoRedirectUri);
        params.add("code", code);
        if (kakaoClientSecret != null && !kakaoClientSecret.isBlank()) {
            params.add("client_secret", kakaoClientSecret);
        }

        Map<?, ?> response = webClient.post()
                .uri(kakaoTokenUri)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue(params)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), clientResponse ->
                        clientResponse.bodyToMono(String.class).map(body -> {
                            log.error("카카오 토큰 요청 실패: status={}, body={}", clientResponse.statusCode(), body);
                            return new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
                        })
                )
                .bodyToMono(Map.class)
                .onErrorMap(e -> {
                    log.error("카카오 토큰 요청 에러: {}", e.getMessage(), e);
                    return new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
                })
                .block();

        if (response == null || !response.containsKey("access_token")) {
            log.error("카카오 응답에 access_token 없음: {}", response);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        return (String) response.get("access_token");
    }

    // ── 카카오 유저 정보 요청 ──────────────────────────
    @SuppressWarnings("unchecked")
    private Map<String, Object> getKakaoUserInfo(String accessToken) {
        Map<?, ?> result = webClient.get()
                .uri(kakaoUserInfoUri)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorMap(e -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR))
                .block();
        if (result == null) throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        return (Map<String, Object>) result;
    }

    // ── 네이버 토큰 요청 ───────────────────────────────
    private String getNaverAccessToken(String code, String state) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", naverClientId);
        params.add("client_secret", naverClientSecret);
        params.add("redirect_uri", naverRedirectUri);
        params.add("code", code);
        params.add("state", state);

        Map<?, ?> response = webClient.post()
                .uri(naverTokenUri)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue(params)
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorMap(e -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR))
                .block();

        if (response == null || !response.containsKey("access_token")) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        return (String) response.get("access_token");
    }

    // ── 네이버 유저 정보 요청 ──────────────────────────
    @SuppressWarnings("unchecked")
    private Map<String, Object> getNaverUserInfo(String accessToken) {
        Map<?, ?> result = webClient.get()
                .uri(naverUserInfoUri)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorMap(e -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR))
                .block();
        if (result == null) throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        return (Map<String, Object>) result;
    }

    // ── 구글 토큰 요청 ─────────────────────────────────
    private String getGoogleAccessToken(String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("redirect_uri", googleRedirectUri);
        params.add("code", code);

        Map<?, ?> response = webClient.post()
                .uri(googleTokenUri)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue(params)
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorMap(e -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR))
                .block();

        if (response == null || !response.containsKey("access_token")) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        return (String) response.get("access_token");
    }

    public String getNaverAuthorizeUrl(String state){
        return "https://nid.naver.com/oauth2.0/authorize"
                +"?response_type=code&client_id=" + naverClientId
                +"&redirect_uri=" + naverRedirectUri
                +"&state=" + state;
    }

    // ── 구글 유저 정보 요청 ────────────────────────────
    @SuppressWarnings("unchecked")
    private Map<String, Object> getGoogleUserInfo(String accessToken) {
        Map<?, ?> result = webClient.get()
                .uri(googleUserInfoUri)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorMap(e -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR))
                .block();
        if (result == null) throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        return (Map<String, Object>) result;
    }
}
