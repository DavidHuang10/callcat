package com.callcat.backend.service;

import com.callcat.backend.entity.User;
import com.callcat.backend.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class AuthenticationService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    
    // Password pattern: 8+ chars, at least one uppercase, one lowercase, one digit
    private static final String PASSWORD_PATTERN = 
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";
    
    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
    
    public AuthenticationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }
    
    public String register(String email, String password, String firstName, String lastName) {
        // Validate email doesn't exist
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }
        
        // Validate password strength
        if (!isValidPassword(password)) {
            throw new RuntimeException("Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, and one number");
        }
        
        // Create and save user
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setIsActive(true);
        
        User savedUser = userRepository.save(user);
        
        // Generate JWT token
        return jwtService.generateToken(
                savedUser.getEmail(),
                savedUser.getId(),
                savedUser.getFullName()
        );
    }
    
    public String authenticate(String email, String password) {
        try {
            // Authenticate user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            
            // Find user
            User user = userRepository.findByEmailAndIsActive(email, true)
                    .orElseThrow(() -> new RuntimeException("User not found or inactive"));
            
            // Generate JWT token
            return jwtService.generateToken(
                    user.getEmail(),
                    user.getId(),
                    user.getFullName()
            );
            
        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid email or password");
        }
    }
    
    public User getCurrentUser(String email) {
        return userRepository.findByEmailAndIsActive(email, true)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    private boolean isValidPassword(String password) {
        return pattern.matcher(password).matches();
    }
    
    public boolean validatePasswordStrength(String password) {
        return isValidPassword(password);
    }
}