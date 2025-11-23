package com.callcat.backend;

import com.callcat.backend.entity.dynamo.UserDynamoDb;
import com.callcat.backend.repository.dynamo.UserRepositoryDynamoDb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class AuthDebugTest {

    public static void main(String[] args) {
        SpringApplication.run(AuthDebugTest.class, args);
    }

    @Bean
    CommandLineRunner testAuth(UserRepositoryDynamoDb userRepo, PasswordEncoder passwordEncoder) {
        return args -> {
            String email = "david.huang@duke.edu";
            String password = "123456789aA";
            
            System.out.println("\n=== Testing Authentication ===");
            System.out.println("Email: " + email);
            System.out.println("Password: " + password);
            
            // Test lowercase
            String lowerEmail = email.toLowerCase();
            System.out.println("Lowercase email: " + lowerEmail);
            
            // Try to find user
            var userOpt = userRepo.findByEmail(lowerEmail);
            if (userOpt.isEmpty()) {
                System.out.println("❌ User NOT found with lowercase email!");
                
                // Try original case
                userOpt = userRepo.findByEmail(email);
                if (userOpt.isEmpty()) {
                    System.out.println("❌ User NOT found with original email either!");
                } else {
                    System.out.println("✅ User found with original case: " + email);
                }
            } else {
                System.out.println("✅ User found!");
                UserDynamoDb user = userOpt.get();
                System.out.println("Stored email: " + user.getEmail());
                System.out.println("Stored hash: " + user.getPassword());
                
                boolean matches = passwordEncoder.matches(password, user.getPassword());
                System.out.println("Password matches: " + matches);
                
                if (!matches) {
                    System.out.println("❌ PASSWORD DOES NOT MATCH!");
                } else {
                    System.out.println("✅ Authentication should work!");
                }
            }
            
            System.exit(0);
        };
    }
}
