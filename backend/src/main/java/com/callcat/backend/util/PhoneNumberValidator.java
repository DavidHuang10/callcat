package com.callcat.backend.util;

import java.util.regex.Pattern;

public class PhoneNumberValidator {

    private static final Pattern E164_PATTERN = Pattern.compile("^\\+1[0-9]{10}$");
    
    public static boolean isValidE164(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        return E164_PATTERN.matcher(phoneNumber.trim()).matches();
    }
    
    public static String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        
        String cleaned = phoneNumber.replaceAll("[^0-9+]", "");
        
        if (cleaned.startsWith("1") && cleaned.length() == 11) {
            cleaned = "+" + cleaned;
        } else if (cleaned.startsWith("0") && cleaned.length() == 10) {
            cleaned = "+1" + cleaned;
        } else if (!cleaned.startsWith("+1") && cleaned.length() == 10) {
            cleaned = "+1" + cleaned;
        }
        
        return cleaned;
    }
    
    public static void validatePhoneNumber(String phoneNumber) {
        if (!isValidE164(phoneNumber)) {
            throw new IllegalArgumentException("Phone number must be in E.164 format (+1XXXXXXXXXX)");
        }
    }
}