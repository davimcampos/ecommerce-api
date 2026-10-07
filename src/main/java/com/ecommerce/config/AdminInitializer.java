package com.ecommerce.config;

import com.ecommerce.user.Role;
import com.ecommerce.user.User;
import com.ecommerce.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

    private final AppProperties properties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        AppProperties.Admin admin = properties.admin();
        if (admin == null || isBlank(admin.email()) || isBlank(admin.password())) {
            return;
        }
        String email = admin.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            return;
        }
        userRepository.save(User.builder()
                .name(isBlank(admin.name()) ? "Administrador" : admin.name())
                .email(email)
                .password(passwordEncoder.encode(admin.password()))
                .role(Role.ADMIN)
                .build());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
