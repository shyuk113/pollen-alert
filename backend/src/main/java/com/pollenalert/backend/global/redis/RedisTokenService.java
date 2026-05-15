package com.pollenalert.backend.global.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisTokenService {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "refresh:";
    private static final String BLACKLIST_PREFIX = "blacklist:";


    //RefreshToken 저장
    public void saveRefreshToken(Long userId, String refreshToken, long expireSeconds){
        stringRedisTemplate.opsForValue().set(REFRESH_TOKEN_PREFIX + userId, refreshToken, expireSeconds, TimeUnit.SECONDS);
    }

    //RefreshToken 조회
    public String getRefreshToken(Long userId){
        return stringRedisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);
    }

    //리프레시 토큰 제거(로그아웃)
    public void deleteRefreshToken(Long userId){
        stringRedisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
    }

    //액세스 토큰 블랙리스트 등록(로그아웃)
    public void addBlackList(String accessToken, long expireSeconds){
        stringRedisTemplate.opsForValue().set(BLACKLIST_PREFIX +accessToken, "logout", expireSeconds, TimeUnit.SECONDS);
    }

    //블랙리스트 여부 확인
    public Boolean isBlackListed(String accessToken){
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(BLACKLIST_PREFIX + accessToken));
    }
}
