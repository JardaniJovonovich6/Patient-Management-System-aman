package com.pm.auth_service.config;

import org.slf4j.LoggerFactory;
import  org.slf4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.pm.auth_service.service.UserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // This is the "glue" bean that fixes your BUILD FAILURE
    // It tells Spring to use YOUR UserService as the official "guest list"
    @Bean
    public AuthenticationProvider authenticationProvider(UserService userService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService); // This line requires UserService to be a UserDetailsService
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        try {
            log.info("running authentication manager ");
            return config.getAuthenticationManager();
        } catch (Exception e) {
            log.error("--------- authenticationManager is giving error ----------->>>>" , e);
            return config.getAuthenticationManager();
        }
        
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationProvider authenticationProvider) throws Exception {

        log.info("----------Running securityfilterchain---------");

        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider) // Use the "glue"
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/login").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .anyRequest().permitAll() // We'll secure /validate later
            )
            .headers(headers -> 
                headers.frameOptions(frameOptions -> frameOptions.sameOrigin())
            );

        return http.build();
    }
}

