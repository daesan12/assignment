package com.example.work.controller;

import com.example.work.dto.LoginRequest;
import com.example.work.dto.SignupRequest;
import com.example.work.entity.RoleType;
import com.example.work.entity.User;
import com.example.work.repository.UserRepository;
import com.example.work.config.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "인증 API", description = "회원가입 및 로그인 기능을 제공하는 API")
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "이미 존재하는 사용자",
                    content = @Content(schema = @Schema(example = "{ 'error': { 'code': 'USER_ALREADY_EXISTS', 'message': '이미 가입된 사용자입니다.' } }")))
    })
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", Map.of(
                            "code", "USER_ALREADY_EXISTS",
                            "message", "이미 가입된 사용자입니다."
                    )
            ));
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .role(RoleType.USER)
                .build();

        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "nickname", user.getNickname(),
                "roles", Map.of("role", user.getRole().name())
        ));
    }

    @Operation(summary = "로그인", description = "사용자가 로그인하면 JWT 토큰을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 로그인 정보",
                    content = @Content(schema = @Schema(example = "{ 'error': { 'code': 'INVALID_CREDENTIALS', 'message': '아이디 또는 비밀번호가 올바르지 않습니다.' } }")))
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername()).orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", Map.of(
                            "code", "INVALID_CREDENTIALS",
                            "message", "아이디 또는 비밀번호가 올바르지 않습니다."
                    )
            ));
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());

        return ResponseEntity.ok(Map.of("token", token));
    }
}
