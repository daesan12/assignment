#  바로인턴 10기 JAVA 과제

## 📌 개요
- **사용자 인증 및 관리자 권한 관리** 기능 구현  
- JWT 기반 인증, **회원가입, 로그인, 관리자 권한 변경** 등의 기능 제공  
- **Swagger UI**를 통해 API 확인 및 테스트 가능  
- **AWS EC2 서버에 배포 완료**
  
- **배포 서버 (EC2 IP)**: `http://3.39.231.118:8080/`

## 📄 API 문서

### 🔗 Swagger UI & API Docs

- **Swagger UI**: [Swagger UI 바로가기](http://3.39.231.118:8080/swagger-ui/index.html)  
  → API를 시각적으로 테스트할 수 있습니다.

- **API 명세서 (JSON)**: [v3 API Docs](http://3.39.231.118:8080/v3/api-docs)  
  → API 정보를 JSON 형태로 확인할 수 있습니다.
---

### 기본 관리자 계정 (서버 실행 시 자동 생성)
| **Username** | **Password** | **Nickname** | **Role** |
|-------------|-------------|-------------|---------|
| admin      | admin123   | SuperAdmin  | ADMIN  |



## 📑 1. 사용자 API

### ✅ 1-1. 회원가입 API
#### 🔹 **[POST] /auth/signup**
- **설명:** 새로운 사용자를 등록합니다.
- **요청 헤더:**
  ```http
  Content-Type: application/json
  ```
- **요청 바디:**
  ```json
  {
    "username": "testuser",
    "password": "password123",
    "nickname": "테스트유저"
  }
  ```
- **응답 예시 (성공):**
  ```json
  {
    "username": "testuser",
    "nickname": "테스트유저",
    "roles": { "role": "USER" }
  }
  ```
- **에러 응답:**
  ```json
  {
    "error": {
      "code": "USER_ALREADY_EXISTS",
      "message": "이미 가입된 사용자입니다."
    }
  }
  ```
- **상태 코드:**
  - ✅ `200 OK` - 회원가입 성공
  - ❌ `400 Bad Request` - 중복된 사용자

---

### ✅ 1-2. 로그인 API
#### 🔹 **[POST] /auth/login**
- **설명:** 로그인 후 JWT 토큰을 발급합니다.
- **요청 헤더:**
  ```http
  Content-Type: application/json
  ```
- **요청 바디:**
  ```json
  {
    "username": "testuser",
    "password": "password123"
  }
  ```
- **응답 예시 (성공):**
  ```json
  {
    "token": "jwt-token-string"
  }
  ```
- **에러 응답:**
  ```json
  {
    "error": {
      "code": "INVALID_CREDENTIALS",
      "message": "아이디 또는 비밀번호가 올바르지 않습니다."
    }
  }
  ```
- **상태 코드:**
  - ✅ `200 OK` - 로그인 성공 (JWT 발급)
  - ❌ `400 Bad Request` - 아이디 또는 비밀번호 오류

---

## 📑 2. 관리자 API (Admin)

### ✅ 2-1. 관리자 권한 부여 API
#### 🔹 **[PATCH] /admin/users/{userId}/roles**
- **설명:** 특정 유저의 역할을 `ADMIN`으로 변경합니다.  
  _(이 API는 관리자만 사용할 수 있습니다.)_
- **요청 헤더:**
  ```http
  Authorization: Bearer {JWT_TOKEN}
  Content-Type: application/json
  ```
- **요청 예시:**
  ```http
  PATCH /admin/users/5/roles
  ```
- **응답 예시 (성공):**
  ```json
  {
    "username": "testuser",
    "nickname": "테스트유저",
    "roles": { "role": "ADMIN" }
  }
  ```
- **에러 응답:**
  ```json
  {
    "error": {
      "code": "ACCESS_DENIED",
      "message": "관리자 권한이 필요한 요청입니다. 접근 권한이 없습니다."
    }
  }
  ```
  또는
  ```json
  {
    "error": {
      "code": "USER_NOT_FOUND",
      "message": "사용자를 찾을 수 없습니다."
    }
  }
  ```
- **상태 코드:**
  - ✅ `200 OK` - 권한 변경 성공
  - ❌ `400 Bad Request` - 사용자 없음
  - ❌ `403 Forbidden` - 관리자 권한 없음
