package com.example.work.controller;

import com.example.work.config.JwtUtil;
import com.example.work.dto.LoginRequest;
import com.example.work.dto.SignupRequest;
import com.example.work.entity.RoleType;
import com.example.work.entity.User;
import com.example.work.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthController authController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void 회원가입_성공() throws Exception {
        SignupRequest signupRequest = new SignupRequest("testuser", "password123", "테스트유저");
        System.out.println("signupRequest: " + objectMapper.writeValueAsString(signupRequest));
        // ✅ 사용자 중복 체크 설정
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        // ✅ 저장될 사용자 객체 생성
        User savedUser = new User(1L, "testuser", "encodedPassword", "테스트유저", RoleType.USER);

        // ✅ userRepository.save()가 실제로 저장될 객체를 반환하도록 설정
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);  // 저장할 객체를 가져옴
            return new User(1L, user.getUsername(), user.getPassword(), user.getNickname(), user.getRole());
        });

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())  // HTTP 200 응답 확인
                .andExpect(jsonPath("$.username").value("testuser"))  // username 검증
                .andExpect(jsonPath("$.nickname").value("테스트유저"))  // nickname 검증
                .andExpect(jsonPath("$.roles.role").value("USER"));  // role 검증
    }


    @Test
    void 회원가입_실패_중복된사용자() throws Exception {
        SignupRequest signupRequest = new SignupRequest("testuser", "password123", "테스트유저");

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("USER_ALREADY_EXISTS"));
    }

    @Test
    void 로그인_성공() throws Exception {
        LoginRequest loginRequest = new LoginRequest("testuser", "password123");
        String encodedPassword = "encodedPassword";
        User mockUser = new User(1L, "testuser", encodedPassword, "테스트유저", RoleType.USER);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password123", encodedPassword)).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), any(RoleType.class))).thenReturn("mocked_jwt_token"); // ✅ JWT 토큰 반환

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked_jwt_token"));
    }

    @Test
    void 로그인_실패_잘못된비밀번호() throws Exception {
        LoginRequest loginRequest = new LoginRequest("testuser", "wrongpassword");
        String encodedPassword = "encodedPassword";
        User mockUser = new User(1L, "testuser", encodedPassword, "테스트유저", RoleType.USER);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrongpassword", encodedPassword)).thenReturn(false);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)));
    }
}