package com.pollenalert.backend.auth.controller;

import com.pollenalert.backend.auth.dto.TokenResponseDto;
import com.pollenalert.backend.auth.service.OAuthService;
import com.pollenalert.backend.global.exception.BusinessException;
import com.pollenalert.backend.global.exception.ErrorCode;
import com.pollenalert.backend.global.redis.RedisTokenService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuthControllerTest {

    @Mock private OAuthService oAuthService;
    @Mock private RedisTokenService redisTokenService;
    @Mock private HttpServletResponse response;

    @InjectMocks
    private OAuthController oAuthController;

    @Test
    @DisplayName("state가 유효하지 않으면 INVALID_OAUTH_STATE 예외")
    void naverInvalidOAuthState() {
        when(redisTokenService.validateAndConsumeOAuthState("bad-state")).thenReturn(false);
        assertThatThrownBy(()-> oAuthController.naverCallback("code","bad-state",response))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_OAUTH_STATE);

        verifyNoInteractions(oAuthService);
    }

    @Test
    @DisplayName("state가 유효하면 로그인 처리 후 accessToken을 httpOnly 쿠키로")
    void naverCallbackSetAccessToken() {
        when(redisTokenService.validateAndConsumeOAuthState("good-state")).thenReturn(true);

        TokenResponseDto token = new TokenResponseDto("access-token", "refresh-token", 3600L, false);
        when(oAuthService.naverLogin("code", "good-state")).thenReturn(token);

        ResponseEntity<TokenResponseDto> result = oAuthController.naverCallback("code","good-state",response);

        assertThat(result.getBody()).isEqualTo(token);

        ArgumentCaptor<String> cookieCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).addHeader(eq(HttpHeaders.SET_COOKIE),  cookieCaptor.capture());

        assertThat(cookieCaptor.getValue())
                .contains("accessToken=access-token")
                .contains("HttpOnly")
                .contains("Secure");
    }

    @Test
    @DisplayName("네이버 로그인 시작 시 state를 생성해 Redis에 저장하고 인가 Url을 반환")
    void naverSaveState(){
        when(oAuthService.getNaverAuthorizeUrl(anyString())).thenReturn("https://nid.naver.com/dummy-url");

        ResponseEntity<Map<String,String>> result = oAuthController.naverAuthorizeUrl();

        verify(redisTokenService).saveOAuthState(anyString(),eq(300L));
        assertThat(result.getBody()).containsEntry("url", "https://nid.naver.com/dummy-url");
    }
}