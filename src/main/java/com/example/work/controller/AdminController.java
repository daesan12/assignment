package com.example.work.controller;

import com.example.work.entity.RoleType;
import com.example.work.entity.User;
import com.example.work.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "관리자 API", description = "사용자의 역할을 변경하는 관리자 API")
@RestController
@RequestMapping("/admin")
public class AdminController {
    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Operation(
            summary = "관리자 권한 부여",
            description = "사용자의 권한을 ADMIN으로 변경하는 API (관리자만 사용 가능)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "권한 변경 성공",
                    content = @Content(schema = @Schema(example = "{ 'username': 'testuser', 'nickname': '테스트유저', 'roles': { 'role': 'ADMIN' } }"))
            ),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 사용자",
                    content = @Content(schema = @Schema(example = "{ 'error': { 'code': 'USER_NOT_FOUND', 'message': '사용자를 찾을 수 없습니다.' } }"))
            ),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음",
                    content = @Content(schema = @Schema(example = "{ 'error': { 'code': 'ACCESS_DENIED', 'message': '관리자 권한이 필요한 요청입니다. 접근 권한이 없습니다.' } }"))
            )
    })
    @PatchMapping("/users/{userId}/roles")
    public ResponseEntity<?> grantAdminRole(@PathVariable Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        User currentUser = userRepository.findByUsername(currentUsername).orElse(null);
        if (currentUser == null || currentUser.getRole() != RoleType.ADMIN) {
            return ResponseEntity.status(403).body(Map.of(
                    "error", Map.of(
                            "code", "ACCESS_DENIED",
                            "message", "관리자 권한이 필요한 요청입니다. 접근 권한이 없습니다."
                    )
            ));
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", Map.of(
                            "code", "USER_NOT_FOUND",
                            "message", "사용자를 찾을 수 없습니다."
                    )
            ));
        }

        user.setRole(RoleType.ADMIN);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "nickname", user.getNickname(),
                "roles", Map.of("role", user.getRole().name())
        ));
    }
}
