package com.pm.auth_service.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.pm.auth_service.model.User;
import com.pm.auth_service.repo.UserRepository;

@Service
public class UserService implements UserDetailsService{ // This interface is the key
    private static final Logger log = LoggerFactory.getLogger(UserService.class);    
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository=userRepository;
    }
    
    @Override
    public UserDetails loadUserByUsername(String Email) throws UsernameNotFoundException {
        log.info("___________Into loadUserByUsername-------------");
        // Find your user from database
        User user = userRepository.findByEmail(Email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found ----->: " + Email));
        
        // Convert your User entity to Spring Security's UserDetails
        return org.springframework.security.core.userdetails.User
            .withUsername(user.getEmail())
            .password(user.getPassword())
            .authorities("USER") // or user.getRoles() if you have roles
            .accountExpired(false)
            .accountLocked(false)
            .credentialsExpired(false)
            .disabled(false)
            .build();
    }

    public Optional<User> findByEmail(String Email){
        log.info("--------findbyEmail method is called by UserService and the email is ------->" , Email);
        return userRepository.findByEmail(Email);
    }
}