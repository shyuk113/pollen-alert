package com.pollenalert.backend.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // ── Auth ──────────────────────────────────────────
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    INVALID_OAUTH_STATE(HttpStatus.BAD_REQUEST, "유효하지 않은 로그인 요청입니다."),

    // ── User ──────────────────────────────────────────
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "본인의 정보만 접근할 수 있습니다."),

    // ── Allergy ───────────────────────────────────────
    ALLERGY_NOT_FOUND(HttpStatus.NOT_FOUND, "알러지 설정이 없습니다."),
    INVALID_ALLERGY_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 알러지 타입입니다. (oak, pine, weed 중 선택)"),

    // ── Alert ─────────────────────────────────────────
    ALERT_SETTING_NOT_FOUND(HttpStatus.NOT_FOUND, "알림 설정이 없습니다."),

    // ── Pollen ────────────────────────────────────────
    POLLEN_DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "꽃가루 데이터가 없습니다."),

    // ── Server ────────────────────────────────────────
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
