package com.callcat.backend.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class PhoneNumberValidatorTest {

    @Test
    void isValidE164_WithValidUSPhoneNumber_ShouldReturnTrue() {
        // Valid US phone numbers in E.164 format
        assertTrue(PhoneNumberValidator.isValidE164("+12125551234"));
        assertTrue(PhoneNumberValidator.isValidE164("+15551234567"));
        assertTrue(PhoneNumberValidator.isValidE164("+19999999999"));
    }

    @Test
    void isValidE164_WithInvalidFormats_ShouldReturnFalse() {
        // Missing country code
        assertFalse(PhoneNumberValidator.isValidE164("2125551234"));
        
        // Wrong country code
        assertFalse(PhoneNumberValidator.isValidE164("+442071234567"));
        
        // Too short
        assertFalse(PhoneNumberValidator.isValidE164("+1212555123"));
        
        // Too long
        assertFalse(PhoneNumberValidator.isValidE164("+121255512345"));
        
        // Contains letters
        assertFalse(PhoneNumberValidator.isValidE164("+1212555ABCD"));
        
        // Contains special characters
        assertFalse(PhoneNumberValidator.isValidE164("+1-212-555-1234"));
        assertFalse(PhoneNumberValidator.isValidE164("+1 (212) 555-1234"));
    }

    @Test
    void isValidE164_WithNullOrEmpty_ShouldReturnFalse() {
        assertFalse(PhoneNumberValidator.isValidE164(null));
        assertFalse(PhoneNumberValidator.isValidE164(""));
        assertFalse(PhoneNumberValidator.isValidE164("   "));
    }

    @Test
    void normalizePhoneNumber_WithVariousFormats_ShouldReturnE164Format() {
        // Test 10-digit number without country code
        assertEquals("+15551234567", PhoneNumberValidator.normalizePhoneNumber("5551234567"));
        
        // Test 11-digit number starting with 1
        assertEquals("+15551234567", PhoneNumberValidator.normalizePhoneNumber("15551234567"));
        
        // Test number with formatting
        assertEquals("+15551234567", PhoneNumberValidator.normalizePhoneNumber("(555) 123-4567"));
        assertEquals("+15551234567", PhoneNumberValidator.normalizePhoneNumber("555-123-4567"));
        assertEquals("+15551234567", PhoneNumberValidator.normalizePhoneNumber("555.123.4567"));
        
        // Test already formatted E.164
        assertEquals("+15551234567", PhoneNumberValidator.normalizePhoneNumber("+15551234567"));
        
        // Test with spaces
        assertEquals("+15551234567", PhoneNumberValidator.normalizePhoneNumber("+1 555 123 4567"));
    }

    @Test
    void normalizePhoneNumber_WithNull_ShouldReturnNull() {
        assertNull(PhoneNumberValidator.normalizePhoneNumber(null));
    }

    @Test
    void validatePhoneNumber_WithValidNumber_ShouldNotThrow() {
        assertDoesNotThrow(() -> PhoneNumberValidator.validatePhoneNumber("+15551234567"));
        assertDoesNotThrow(() -> PhoneNumberValidator.validatePhoneNumber("+19999999999"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "5551234567",           // Missing +1
        "+442071234567",        // Wrong country code
        "+1555123456",          // Too short
        "+155512345678",        // Too long
        "+1555123ABCD",         // Contains letters
        "+1-555-123-4567",      // Contains dashes
        ""                      // Empty
    })
    void validatePhoneNumber_WithInvalidNumbers_ShouldThrowException(String invalidNumber) {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> PhoneNumberValidator.validatePhoneNumber(invalidNumber)
        );
        
        assertEquals("Phone number must be in E.164 format (+1XXXXXXXXXX)", exception.getMessage());
    }

    @Test
    void validatePhoneNumber_WithNull_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> PhoneNumberValidator.validatePhoneNumber(null)
        );
        
        assertEquals("Phone number must be in E.164 format (+1XXXXXXXXXX)", exception.getMessage());
    }
}