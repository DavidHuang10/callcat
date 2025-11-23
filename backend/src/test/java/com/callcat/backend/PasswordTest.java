package com.callcat.backend;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PasswordTest {

    @Test
    public void testPasswordMatch() {
        PasswordEncoder encoder = new BCryptPasswordEncoder(12);
        String password = "123456789aA";
        String hash = "$2a$12$illxS0SEYVIHEqL48aaTNOAyBOPMYvTmYc0idEK67z7a9BYj4rxmy";
        
        assertTrue(encoder.matches(password, hash), "Password should match the hash");
    }
}
