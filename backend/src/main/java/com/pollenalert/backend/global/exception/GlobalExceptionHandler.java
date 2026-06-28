package com.pollenalert.backend.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 비즈니스 예외 처리 ─────────────────────────────
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException e) {
        log.warn("비즈니스 예외: {}", e.getMessage());
        return buildResponse(e.getErrorCode());
    }

    // ── 서버 내부 오류 (500) ───────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        log.error("서버 오류: {}", e.getMessage(), e);
        return buildResponse(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(ErrorCode errorCode) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", errorCode.getStatus().value());
        body.put("error", errorCode.getStatus().getReasonPhrase());
        body.put("message", errorCode.getMessage());
        return ResponseEntity.status(errorCode.getStatus()).body(body);
    }
}
