package com.pm.auth_service.service;

import java.util.Optional;

// --- Make sure these are imported ---
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// ---

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.pm.auth_service.dto.LoginRequestDTO;
import com.pm.auth_service.model.User;
import com.pm.auth_service.util.Jwtutil;

import io.jsonwebtoken.JwtException;

@Service
public class AuthService {
    
    // 1. Add a logger
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final Jwtutil jwtUtil;

    public AuthService(UserService userService , PasswordEncoder passwordEncoder, Jwtutil jwtUtil){
        this.userService= userService;
        this.passwordEncoder=passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public Optional<String> authenticate(LoginRequestDTO loginRequestDTO) {
        
        // 2. Find the user first
        Optional<User> userOptional = userService.findByEmail(loginRequestDTO.getEmail());

        // 3. If the user doesn't even exist, return empty immediately
        if (userOptional.isEmpty()) {
            log.warn("Login attempt failed: User not found for email {}", loginRequestDTO.getEmail());
            return Optional.empty();
        }

        // 4. The user exists! Let's get them.
        User user = userOptional.get();
        String providedPassword = loginRequestDTO.getPassword();
        String storedPasswordHash = user.getPassword();

        // 5. Let's run the check
        boolean passwordMatches = passwordEncoder.matches(providedPassword, storedPasswordHash);

        // 6. --- THIS IS THE SPY LOG ---
        log.info("--- AUTHENTICATION ATTEMPT ---");
        log.info("Email from Postman: {}", loginRequestDTO.getEmail());
        log.info("Password from Postman: {}", providedPassword);
        log.info("Password Hash from DB: {}", storedPasswordHash);
        log.info("Password check result: {}", passwordMatches);
        log.info("------------------------------");

        // 7. Now, we return the token only if the password matches
        if (passwordMatches) {
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
            return Optional.of(token);
        } else {
            return Optional.empty(); // This is what's causing the 401
        }
    }

    public boolean validateToken(String token){
        try{
            log.info("Validating Token at AuthService...");
            jwtUtil.validateToken(token);
            log.info("Token is Validated.");
            return true;
        }catch(JwtException e){
            log.error("Exception at AuthService.java ----- > " , e);
            return false;
        }
    }
}

