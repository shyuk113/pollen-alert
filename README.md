# pollen-alert
꽃가루 알러지 사전 경보 앱


## 주요 기능

- **회원** : 회원가입 / 로그인 (JWT 인증, 소셜 로그인) / 로그 아웃 / 회원 정보 조회, 수정, 탈퇴 
- **알러지** : 알러지 유무 등록 및 수정 / 유저의 알러지 정보 조회
- **꽃가루 예보** : 꽃가루 예보 3일 조회 / 지역 목록 조회
- **알림** : 알림 설정 등록 및 수정 / 알림 설정 조회 / 알림 발생 이력 조회

## API 명세

### Auth
| Method | URL | 설명 | 권한 |
|--------|-----|------|------|
| POST | /api/auth/signup | 회원가입 | 누구나 |
| POST | /api/auth/login | 로그인 | 누구나 |
| POST | /api/auth/kakao | 카카오 소셜 로그인 | 누구나 |
| POST | /api/auth/naver | 네이버 소셜 로그인 | 누구나 |
| POST | /api/auth/google | 구글 소셜 로그인 | 누구나 |
| POST | /api/auth/logout | 로그아웃 | 회원 |
| POST | /api/auth/refresh | 토큰 재발급 | 누구나 |

### Member
| Method | URL | 설명 | 권한 |
|--------|-----|------|------|
| GET | /api/members/{id} | 회원 정보 조회 | 회원 |
| PUT | /api/members/{id} | 회원 정보 수정 | 회원 |
| DELETE | /api/members/{id} | 회원 정보 수정 | 회원 |

### Allergy
| Method | URL | 설명 | 권한 |
|--------|-----|------|------|
| POST | /api/members/{id}/allergy | 알러지 유무 등록 및 수정 | 회원 |
| GET | /api/members/{id}/allergy | 알러지 정보 조회 | 회원 |


### Pollen
| Method | URL | 설명 | 권한 |
|--------|-----|------|------|
| GET | /api/pollen | 현재 꽃가루 지수 조회 | 회원 |
| GET | /api/pollen/forecast | 꽃가루 지수 예보 조회 | 회원 |
| GET | /api/pollen/regions | 지역 목록 조회 | 회원 |

### Alert
| Method | URL | 설명 | 권한 |
|--------|-----|------|------|
| POST | /api/members/{id}/alert | 꽃가루 알림 등록 및 수정 | 회원 |
| GET | /api/members/{id}/alert | 알림 설정 조회 | 회원 |
| GET | /api/members/{id}/alert/history | 알림 발송 이력 조회 | 회원 |

