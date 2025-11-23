package com.callcat.backend.service;

import com.callcat.backend.dto.AuthResponse;
import com.callcat.backend.entity.User;
import com.callcat.backend.entity.dynamo.UserDynamoDb;
import com.callcat.backend.repository.dynamo.UserRepositoryDynamoDb;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class AuthenticationService {
    
    private final UserRepositoryDynamoDb userRepository;
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
            UserRepositoryDynamoDb userRepository,
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
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already registered");
        }
        
        // Validate password strength
        if (!isValidPassword(password)) {
            throw new RuntimeException("Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, and one number");
        }
        
        // Create new user with verified email
        UserDynamoDb userDynamo = new UserDynamoDb();
        userDynamo.setEmail(email);
        userDynamo.setPassword(passwordEncoder.encode(password));
        userDynamo.setFirstName(firstName);
        userDynamo.setLastName(lastName);
        userDynamo.setIsActive(true); // Account is active upon registration
        userDynamo.setCreatedAt(java.time.LocalDateTime.now().toString());
        userDynamo.setUpdatedAt(java.time.LocalDateTime.now().toString());
        
        userRepository.save(userDynamo);
        
        // Generate JWT token and create response
        String token = jwtService.generateToken(
                userDynamo.getEmail(),
                null, // ID is null for DynamoDB users
                userDynamo.getFullName()
        );
        
        return new AuthResponse(
                token,
                null, // ID
                userDynamo.getEmail(),
                userDynamo.getFullName(),
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
            UserDynamoDb userDynamo = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (!Boolean.TRUE.equals(userDynamo.getIsActive())) {
                throw new RuntimeException("User is inactive");
            }
            
            // Generate JWT token and create response
            String token = jwtService.generateToken(
                    userDynamo.getEmail(),
                    null,
                    userDynamo.getFullName()
            );
            
            return new AuthResponse(
                    token,
                    null,
                    userDynamo.getEmail(),
                    userDynamo.getFullName(),
                    jwtService.getExpirationTime()
            );
            
        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid email or password");
        }
    }
    
    public User getCurrentUser(String email) {
        UserDynamoDb userDynamo = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!Boolean.TRUE.equals(userDynamo.getIsActive())) {
             throw new RuntimeException("User is inactive");
        }

        // Map to User DTO
        User user = new User();
        user.setEmail(userDynamo.getEmail());
        user.setPassword(userDynamo.getPassword());
        user.setFirstName(userDynamo.getFirstName());
        user.setLastName(userDynamo.getLastName());
        user.setRole(userDynamo.getRole());
        user.setIsActive(userDynamo.getIsActive());
        
        return user;
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
        UserDynamoDb userDynamo = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No account found with this email address"));
        
        if (!Boolean.TRUE.equals(userDynamo.getIsActive())) {
             throw new RuntimeException("User is inactive");
        }
        
        // Generate secure reset token and expiration (1 hour)
        String resetToken = emailService.generateResetToken();
        Long expires = System.currentTimeMillis() / 1000 + 3600;
        
        // Save reset token to user
        userDynamo.setPasswordResetToken(resetToken);
        userDynamo.setResetTokenExpires(expires);
        userRepository.save(userDynamo);
        
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
        UserDynamoDb userDynamo = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));
        
        // Check if token hasn't expired
        if (userDynamo.getResetTokenExpires() == null || userDynamo.getResetTokenExpires() < System.currentTimeMillis() / 1000) {
            // Clear expired token
            userDynamo.setPasswordResetToken(null);
            userDynamo.setResetTokenExpires(null);
            userRepository.save(userDynamo);
            throw new RuntimeException("Reset token has expired");
        }
        
        // Update password and clear reset token
        userDynamo.setPassword(passwordEncoder.encode(newPassword));
        userDynamo.setPasswordResetToken(null);
        userDynamo.setResetTokenExpires(null);
        userRepository.save(userDynamo);
    }
}