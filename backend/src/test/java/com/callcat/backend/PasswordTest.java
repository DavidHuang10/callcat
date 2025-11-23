package com.callcat.backend;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        
        String storedHash = "$2a$12$illxS0SEYVIHEqL48aaTNOAyBOPMYvTmYc0idEK67z7a9BYj4rxmy";
        String testPassword = "123456789aA";
        
        boolean matches = encoder.matches(testPassword, storedHash);
        
        System.out.println("Password: " + testPassword);
        System.out.println("Stored hash: " + storedHash);
        System.out.println("Matches: " + matches);
        
        if (!matches) {
            System.out.println("\n❌ Password does NOT match the stored hash!");
            System.out.println("The password you're trying is incorrect.");
        } else {
            System.out.println("\n✅ Password matches!");
        }
    }
}
