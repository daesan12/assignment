package com.example.work.config;

import com.example.work.entity.RoleType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    // ✅ 올바른 SECRET_KEY 설정 (Base64 인코딩된 키 사용)
    private static final String SECRET_KEY_BASE64 = "NxU2bG/7R42uDqUytq0F5pG7o0L+XlBZIfGYYMnZkRM="; // Base64 인코딩 필수
    private static final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256); // 안전한 키 자동 생성

    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 2; // 2시간 (밀리초)

    // ✅ JWT 토큰 생성
    public String generateToken(String username, RoleType role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role.name())  // ✅ Enum → String 변환 후 저장
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256) // ✅ SHA-256 서명
                .compact();
    }

    // ✅ 토큰에서 사용자명(Username) 추출
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // ✅ 토큰에서 역할(RoleType) 추출
    public RoleType extractRole(String token) {
        String role = extractClaim(token, claims -> claims.get("role", String.class));
        return RoleType.valueOf(role); // ✅ String → Enum 변환
    }

    // ✅ 모든 Claims 추출
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ✅ 특정 Claim 추출
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    public String extractUserRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }



    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (Exception e) {
            return false; // ✅ 서명 오류 등 모든 예외 발생 시 false 반환
        }
    }

    // ✅ 토큰 만료 여부 확인
    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
}
