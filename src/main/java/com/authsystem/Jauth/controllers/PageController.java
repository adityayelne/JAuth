package com.authsystem.JAuth.controllers;

import com.authsystem.Jauth.dto.AuthResponse;
import com.authsystem.Jauth.dto.LoginRequest;
import com.authsystem.Jauth.dto.RegisterRequest;
import com.authsystem.Jauth.entity.User;
import com.authsystem.Jauth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

@Controller
@RequestMapping
public class PageController {
    
    @Autowired
    private AuthService authService;
    
    // ===== PAGE ROUTES =====
    
    /**
     * Landing page: shows login form
     */
    @GetMapping("/")
    public String showLoginPage() {
        return "login";
    }
    
    /**
     * Register page
     */
    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }
    
    /**
     * Protected page: only accessible if user is logged in (has valid JWT token)
     */
    @GetMapping("/dashboard")
    public String showDashboard() {
        return "dashboard";
    }
    
    // ===== REST API ENDPOINTS =====
    
    /**
     * POST /api/auth/register
     * Request body: { "email": "user@example.com", "password": "pass123", "fullName": "John Doe" }
     * Response: { "token": "jwt_token_here", "email": "user@example.com", "fullName": "John Doe", "message": "User registered successfully" }
     * 
     * WHERE IT'S CALLED FROM: register.html (JavaScript fetch)
     * WHERE DATA COMES FROM: Frontend form submission
    * WHERE DATA GOES: Saved to MySQL database via UserRepository
     */
    @PostMapping("/api/auth/register")
    @ResponseBody
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        
        if (response.getToken() == null) {
            return ResponseEntity.badRequest().body(response);  // 400 error
        }
        return ResponseEntity.ok(response);  // 200 success
    }
    
    /**
     * POST /api/auth/login
     * Request body: { "email": "user@example.com", "password": "pass123" }
     * Response: { "token": "jwt_token_here", "email": "user@example.com", "fullName": "John Doe", "message": "Login successful" }
     * 
     * WHERE IT'S CALLED FROM: login.html (JavaScript fetch)
    * WHERE DATA COMES FROM: Frontend form submission, verified against MySQL database
     * WHERE DATA GOES: Response sent back to frontend, token stored in localStorage
     */
    @PostMapping("/api/auth/login")
    @ResponseBody
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        
        if (response.getToken() == null) {
            return ResponseEntity.badRequest().body(response);  // 400 error
        }
        return ResponseEntity.ok(response);  // 200 success
    }
    
    /**
     * GET /api/protected/profile
     * PROTECTED: Requires valid JWT token in Authorization header
     * Response: { "email": "user@example.com", "fullName": "John Doe" }
     * 
     * HOW IT WORKS:
     * 1. Frontend sends request with header: "Authorization: Bearer <jwt_token>"
     * 2. JwtAuthenticationFilter validates token
     * 3. If valid, @AuthenticationPrincipal extracts email from SecurityContext
     * 4. AuthService fetches user from database
     * 5. Return user data
     */
    @GetMapping("/api/protected/profile")
    @ResponseBody
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal String email) {
        if (email == null) {
            return ResponseEntity.status(401).body("Unauthorized: No valid token");
        }
        
        Optional<User> user = authService.getUserByEmail(email);
        
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(user.get());
    }
}

