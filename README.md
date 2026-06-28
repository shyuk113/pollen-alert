# 🌿 Pollen Alert - 꽃가루 알러지 사전 경보 앱

꽃가루 농도가 높아지기 전에 미리 알림을 제공하는 백엔드 서비스입니다.

<br>

## 📌 프로젝트 소개

꽃가루 알러지 환자들이 증상 발현 전에 미리 대비할 수 있도록,  
기상청 꽃가루 예보 데이터를 수집하여 **D-3, D-1, 당일** 푸시 알림을 제공합니다.

<br>

## 🛠 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 4.0.6 |
| Build | Gradle Kotlin DSL |
| Database | PostgreSQL |
| Cache | Redis |
| Auth | JWT (jjwt 0.12.6) + Spring Security |
| Social Login | 카카오, 네이버, 구글 OAuth 2.0 |
| Push | Firebase Cloud Messaging (FCM) |
| External API | 기상청 꽃가루농도위험지수 API V3 |
| HTTP Client | WebClient (WebFlux) |

<br>

## 🏗 아키텍처

```
Client
  │
  ▼
Spring Boot (REST API)
  ├── Spring Security + JWT 인증
  ├── OAuth 2.0 소셜 로그인 (카카오, 네이버, 구글)
  ├── PostgreSQL (유저, 꽃가루 데이터, 알림 이력)
  ├── Redis (RefreshToken, AccessToken BlackList)
  ├── 기상청 API (WebClient, 6시간마다 수집)
  └── FCM (매 정각 알림 조건 체크 후 푸시 발송)
```

<br>

## 📦 패키지 구조

```
com.pollenalert.backend
├── auth        # 인증 (회원가입, 로그인, JWT, 소셜 로그인)
├── member      # 회원 정보, 알러지 설정
├── pollen      # 꽃가루 데이터 수집 및 조회
├── alert       # 알림 설정, FCM 푸시 스케줄러
└── global      # 공통 설정 (Security, Redis, Firebase, 예외 처리)
```

<br>

## 🗂 ERD

| 테이블 | 설명 |
|--------|------|
| users | 회원 정보 (이메일/소셜 로그인 통합) |
| allergy_setting | 알러지 설정 (수종, 유무) |
| alert_setting | 알림 설정 (임계값, 시간, FCM 토큰) |
| alert_history | 알림 발송 이력 |
| pollen_data | 꽃가루 농도 예보 데이터 |
| refresh_token | RefreshToken (DB 방식, Redis 마이그레이션 후 미사용) |

<br>

## 📡 API 명세

### Auth
| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | /api/auth/signup | 회원가입 | ❌ |
| POST | /api/auth/login | 이메일 로그인 | ❌ |
| POST | /api/auth/refresh | 토큰 재발급 | ❌ |
| POST | /api/auth/logout | 로그아웃 | ✅ |
| POST | /api/auth/kakao | 카카오 로그인 | ❌ |
| POST | /api/auth/naver | 네이버 로그인 | ❌ |
| POST | /api/auth/google | 구글 로그인 | ❌ |

### Member
| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| GET | /api/members/{id} | 회원 정보 조회 | ✅ |
| PUT | /api/members/{id} | 회원 정보 수정 | ✅ |
| DELETE | /api/members/{id} | 회원 탈퇴 | ✅ |
| POST | /api/members/{id}/allergy | 알러지 설정 등록/수정 | ✅ |
| GET | /api/members/{id}/allergy | 알러지 설정 조회 | ✅ |

### Pollen
| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| GET | /api/pollen | 현재 꽃가루 지수 조회 | ✅ |
| GET | /api/pollen/forecast | 3일 예보 조회 | ✅ |
| GET | /api/pollen/regions | 지역 목록 조회 | ✅ |

### Alert
| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | /api/alerts/settings | 알림 설정 등록/수정 | ✅ |
| GET | /api/alerts/settings | 알림 설정 조회 | ✅ |
| GET | /api/alerts/history | 알림 이력 조회 | ✅ |

<br>

## ⚙️ 실행 방법

### 1. 사전 준비

- Java 17
- PostgreSQL
- Redis (Docker 권장)
- Firebase 서비스 계정 키
- 카카오/네이버/구글 OAuth 앱 등록 및 키 발급

### 2. Redis 실행

```bash
docker run -d -p 6379:6379 --name redis redis
```

### 3. 환경 설정

`application.yml.example`을 복사하여 `application.yml` 생성 후 설정값 입력

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/{DB명}
    username: {유저명}
    password: {비밀번호}
  data:
    redis:
      host: localhost
      port: 6379

kma:
  api-key: {기상청 API 인증키}
  base-url: https://apis.data.go.kr/1360000/HealthWthrIdxServiceV3

jwt:
  secret: {JWT 시크릿 키 (256비트 이상)}
  access-token-expiration: 900000
  refresh-token-expiration: 604800000

oauth:
  kakao:
    client-id: {카카오 REST API 키}
    redirect-uri: http://localhost:8080/api/auth/kakao
    token-uri: https://kauth.kakao.com/oauth/token
    user-info-uri: https://kapi.kakao.com/v2/user/me
  naver:
    client-id: {네이버 Client ID}
    client-secret: {네이버 Client Secret}
    redirect-uri: http://localhost:8080/api/auth/naver
    token-uri: https://nid.naver.com/oauth2.0/token
    user-info-uri: https://openapi.naver.com/v1/nid/me
  google:
    client-id: {구글 Client ID}
    client-secret: {구글 Client Secret}
    redirect-uri: http://localhost:8080/api/auth/google
    token-uri: https://oauth2.googleapis.com/token
    user-info-uri: https://www.googleapis.com/oauth2/v2/userinfo
```

Firebase 서비스 계정 키를 `src/main/resources/firebase-service-account.json`에 추가

### 4. 실행

```bash
./gradlew bootRun
```

<br>

## 🔐 인증 방식

### 이메일 로그인
- AccessToken (15분) + RefreshToken (7일) 발급
- RefreshToken은 Redis에 저장
- 로그아웃 시 RefreshToken 삭제 + AccessToken BlackList 등록
- 매 요청마다 BlackList 체크

### 소셜 로그인 (카카오, 네이버, 구글)
- 클라이언트에서 인가 코드를 받아 백엔드로 전달
- 백엔드에서 소셜 서버에 액세스 토큰 및 유저 정보 요청
- 신규 유저는 자동 가입 처리
- 동일한 JWT 토큰 방식으로 응답

