package com.callcat.backend.service;

import com.callcat.backend.dto.AuthResponse;
import com.callcat.backend.entity.User;
import com.callcat.backend.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Service
public class AuthenticationService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final VerificationService verificationService;
    
    // Password pattern: 8+ chars, at least one uppercase, one lowercase, one digit
    private static final String PASSWORD_PATTERN = 
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";
    
    // Email pattern: basic format with common TLDs
    private static final String EMAIL_PATTERN = 
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.(com|org|net|edu|gov|mil|int|co|io|info|biz|me|app|dev|tech|ai|us|uk|ca|au|de|fr|jp|in|br|mx|es|it|nl|se|no|dk|fi|pl|cz|hu|gr|tr|il|za|ng|ke|eg|ma|tn|dz|ly|sd|et|gh|ug|zw|mw|zm|bw|sz|ls|mz|ao|cd|cf|cm|ga|gq|st|td|ne|ml|bf|ci|sn|gm|gw|sl|lr|gn|mr|cv)$";
    
    private static final Pattern passwordPattern = Pattern.compile(PASSWORD_PATTERN);
    private static final Pattern emailPattern = Pattern.compile(EMAIL_PATTERN, Pattern.CASE_INSENSITIVE);
    
    public AuthenticationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            EmailService emailService,
            VerificationService verificationService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
        this.verificationService = verificationService;
    }
    
    public AuthResponse register(String email, String password, String firstName, String lastName) {
        // Validate email format
        if (!isValidEmail(email)) {
            throw new RuntimeException("Invalid email format. Email must contain @ and a valid domain (e.g., .com, .org, .net)");
        }
        
        // Check if email is verified through verification service
        if (!verificationService.isEmailVerified(email)) {
            throw new RuntimeException("Email must be verified before registration. Please verify your email first.");
        }
        
        // Check if user already exists
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }
        
        // Validate password strength
        if (!isValidPassword(password)) {
            throw new RuntimeException("Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, and one number");
        }
        
        // Create new user with verified email
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmailVerified(true); // Email already verified through verification service
        user.setIsActive(true); // Account is active upon registration
        
        User savedUser = userRepository.save(user);
        
        // Generate JWT token and create response
        String token = jwtService.generateToken(
                savedUser.getEmail(),
                savedUser.getId(),
                savedUser.getFullName()
        );
        
        return new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                jwtService.getExpirationTime()
        );
    }
    
    public AuthResponse authenticate(String email, String password) {
        try {
            // Authenticate user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            
            // Find user
            User user = userRepository.findByEmailAndIsActive(email, true)
                    .orElseThrow(() -> new RuntimeException("User not found or inactive"));
            
            // Generate JWT token and create response
            String token = jwtService.generateToken(
                    user.getEmail(),
                    user.getId(),
                    user.getFullName()
            );
            
            return new AuthResponse(
                    token,
                    user.getId(),
                    user.getEmail(),
                    user.getFullName(),
                    jwtService.getExpirationTime()
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
        return passwordPattern.matcher(password).matches();
    }
    
    private boolean isValidEmail(String email) {
        return emailPattern.matcher(email).matches();
    }
    
    
    /**
     * Initiates password reset process by sending reset token to user's email
     * Now uses shorter, secure tokens with proper expiration
     */
    public void forgotPassword(String email) {
        // Find user by email
        User user = userRepository.findByEmailAndIsActive(email, true)
                .orElseThrow(() -> new RuntimeException("No account found with this email address"));
        
        // Generate secure reset token and expiration (1 hour)
        String resetToken = emailService.generateResetToken();
        LocalDateTime expires = LocalDateTime.now().plusHours(1);
        
        // Save reset token to user
        user.setPasswordResetToken(resetToken);
        user.setResetTokenExpires(expires);
        userRepository.save(user);
        
        // Send reset email
        emailService.sendPasswordResetEmail(email, resetToken);
    }
    
    /**
     * Resets user's password using the reset token
     * Now with improved token validation and cleanup
     */
    public void resetPassword(String token, String newPassword) {
        // Validate password strength
        if (!isValidPassword(newPassword)) {
            throw new RuntimeException("Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, and one number");
        }
        
        // Find user by reset token
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));
        
        // Check if token hasn't expired
        if (user.getResetTokenExpires() == null || user.getResetTokenExpires().isBefore(LocalDateTime.now())) {
            // Clear expired token
            user.setPasswordResetToken(null);
            user.setResetTokenExpires(null);
            userRepository.save(user);
            throw new RuntimeException("Reset token has expired");
        }
        
        // Update password and clear reset token
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setResetTokenExpires(null);
        userRepository.save(user);
    }
}