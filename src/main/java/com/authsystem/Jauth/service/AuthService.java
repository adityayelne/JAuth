package com.authsystem.Jauth.service;

import com.authsystem.Jauth.dto.AuthResponse;
import com.authsystem.Jauth.dto.LoginRequest;
import com.authsystem.Jauth.dto.RegisterRequest;
import com.authsystem.Jauth.entity.User;
import com.authsystem.Jauth.repository.UserRepository;
import com.authsystem.Jauth.security.JwtProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtProvider jwtProvider;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Register a new user
     * 1. Check if email already exists
     * 2. Hash the password
     * 3. Save user to database
     * 4. Generate JWT token
     */
    public AuthResponse register(RegisterRequest request) {
        // Check if email already registered
        if (userRepository.existsByEmail(request.getEmail())) {
            return new AuthResponse(null, null, null, "Email already registered");
        }
        
        // Create new user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));  // Hash password
        user.setFullName(request.getFullName());
        
        // Save to database
        User savedUser = userRepository.save(user);
        
        // Generate token
        String token = jwtProvider.generateToken(savedUser.getEmail());
        
        return new AuthResponse(token, savedUser.getEmail(), savedUser.getFullName(), "User registered successfully");
    }
    
    /**
     * Login user
     * 1. Find user by email
     * 2. Verify password matches
     * 3. Generate JWT token
     */
    public AuthResponse login(LoginRequest request) {
        // Find user
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        
        if (userOpt.isEmpty()) {
            return new AuthResponse(null, null, null, "Email not found");
        }
        
        User user = userOpt.get();
        
        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return new AuthResponse(null, null, null, "Invalid password");
        }
        
        // Generate token
        String token = jwtProvider.generateToken(user.getEmail());
        
        return new AuthResponse(token, user.getEmail(), user.getFullName(), "Login successful");
    }
    
    /**
     * Get user by email (used for protected endpoints)
     */
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
