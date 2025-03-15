package com.example.work.config;

import com.example.work.entity.RoleType;
import com.example.work.entity.User;
import com.example.work.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

            User adminUser = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123")) // 기본 비밀번호
                    .nickname("SuperAdmin")
                    .role(RoleType.ADMIN) // ADMIN 역할 설정
                    .build();
            userRepository.save(adminUser);
            System.out.println("✅ 기본 ADMIN 계정 생성 완료 (username: admin, password: admin123)");

    }
}
