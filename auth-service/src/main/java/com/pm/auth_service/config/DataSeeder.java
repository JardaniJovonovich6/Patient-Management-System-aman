package com.pm.auth_service.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.pm.auth_service.model.User;
import com.pm.auth_service.repo.UserRepository;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // This will run one time when the application starts
        if (userRepository.findByEmail("user@test.com").isEmpty()) {
            User user = new User();
            user.setEmail("user@test.com");
            // Here, we ENCODE the password before saving
            user.setPassword(passwordEncoder.encode("password123"));
            user.setRole("ROLE_USER");
            userRepository.save(user);
            System.out.println("--- Created dummy user 'user@test.com' ---");
        }
    }
}
