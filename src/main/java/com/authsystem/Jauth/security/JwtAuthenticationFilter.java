package main.java.com.authsystem.Jauth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * This filter runs BEFORE the controller
 * It checks if request has a valid JWT token
 * If yes, it authenticates the user in Spring Security
 * If no, request continues (will be blocked by @PreAuthorize or endpoint rules)
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtProvider jwtProvider;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            // Get token from header: "Authorization: Bearer <token>"
            String token = extractToken(request);
            
            if (token != null && jwtProvider.validateToken(token)) {
                // Token is valid, extract email
                String email = jwtProvider.getEmailFromToken(token);
                
                // Create authentication object (tells Spring Security this user is logged in)
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(email, null, new ArrayList<>());
                
                // Store in SecurityContext (accessible in controller via @AuthenticationPrincipal)
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            // Invalid token, but let the request continue (endpoints will decide if auth is required)
            logger.error("Cannot set user authentication: " + e.getMessage());
        }
        
        // Continue to next filter/controller
        filterChain.doFilter(request, response);
    }
    
    /**
     * Extract token from "Authorization: Bearer <token>" header
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);  // remove "Bearer " prefix
        }
        return null;
    }
}
