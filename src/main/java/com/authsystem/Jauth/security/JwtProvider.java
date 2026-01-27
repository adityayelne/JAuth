package com.authsystem.Jauth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtProvider {
    
    @Value("${jwt.secret:mySecretKeyForJWTTokenGenerationAndValidationPurposeOnlyDontShareThisWithAnyone}")
    private String jwtSecret;
    
    @Value("${jwt.expiration:3600000}") // 1 hour in milliseconds
    private long jwtExpiration;
    
    /**
     * Generate JWT token for a user email
     * TOKEN STRUCTURE: header.payload.signature
     * PAYLOAD contains: email, iat (issued at), exp (expiration)
     */
    public String generateToken(String email) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        
        return Jwts.builder()
            .setSubject(email)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
            .signWith(key)
            .compact();
    }
    
    /**
     * Validate token and extract email from it
     * Returns email if token is valid, null if invalid/expired
     */
    public String getEmailFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Check if token is valid
     */
    public boolean validateToken(String token) {
        return getEmailFromToken(token) != null;
    }
}
