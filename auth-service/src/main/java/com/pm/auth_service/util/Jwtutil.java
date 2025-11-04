package com.pm.auth_service.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys; // Make sure this is imported
import io.jsonwebtoken.security.SignatureException;

@Component
public class Jwtutil {
    
    private static final Logger log = LoggerFactory.getLogger(Jwtutil.class);
    private final SecretKey secretkey; // This is correct
    
    public Jwtutil(@Value("${jwt.secret}") String secret){
        byte[] keyBytes = Base64.getDecoder().decode(secret.getBytes(StandardCharsets.UTF_8));
        this.secretkey = Keys.hmacShaKeyFor(keyBytes); // This is correct
    }
    
    public String generateToken(String email, String role) {
        // This is perfect, no changes
        return Jwts.builder()
            .subject(email)
            .claim("role", role)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours
            .signWith(secretkey)
            .compact();
    }

    // --- THIS IS THE UPDATED VALIDATION METHOD ---
    public void validateToken(String token) {
        try {
            log.info("----- Validating Token at Jwtutil.java -----");
            
            // Use the "classic" parser that works on all versions
            Jwts.parser()
                .setSigningKey(secretkey) // Use setSigningKey instead of secretKey
                .build()
                .parseSignedClaims(token);

            log.info("............ Token is validated ..........");     
        } catch (SignatureException e) {
            log.error("This is the SignatureException --------> ", e);
            throw new JwtException("Invalid JWT signature");
        } catch (JwtException e) {
            log.error("This is the JwtException --------> ", e);
            throw new JwtException("Invalid JWT");
        }
    }
}