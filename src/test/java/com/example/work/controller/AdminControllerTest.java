package com.example.work.controller;

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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminController adminController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
    }

    private void mockAuthentication(String username, RoleType roleType) {
        User mockUser = new User(1L, username, "encodedPassword", "관리자", roleType);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));
    }

    @Test
    void 관리자권한부여_성공() throws Exception {
        mockAuthentication("adminUser", RoleType.ADMIN);
        User targetUser = new User(2L, "normalUser", "encodedPassword", "일반유저", RoleType.USER);

        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));

        mockMvc.perform(patch("/admin/users/2/roles")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("normalUser"))
                .andExpect(jsonPath("$.roles.role").value("ADMIN"));

        verify(userRepository, times(1)).save(targetUser);
    }

    @Test
    void 관리자권한부여_실패_일반유저() throws Exception {
        mockAuthentication("normalUser", RoleType.USER);

        mockMvc.perform(patch("/admin/users/2/roles")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));
    }

    @Test
    void 관리자권한부여_실패_없는유저() throws Exception {
        mockAuthentication("adminUser", RoleType.ADMIN);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(patch("/admin/users/999/roles")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("USER_NOT_FOUND"));
    }
}
