package com.pm.auth_service.repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pm.auth_service.model.User;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String Email) ;
}
