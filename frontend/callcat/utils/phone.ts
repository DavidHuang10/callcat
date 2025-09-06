/**
 * Phone number formatting and validation utilities
 * Handles various phone number input formats and converts to E.164 format
 */

// E.164 format regex for validation (+1XXXXXXXXXX)
export const PHONE_E164_REGEX = /^\+1[0-9]{10}$/;

/**
 * Clean phone number by removing all non-digit characters
 */
export function cleanPhoneNumber(phone: string): string {
  return phone.replace(/\D/g, '');
}

/**
 * Format phone number for display as (XXX) XXX-XXXX
 */
export function formatPhoneForDisplay(phone: string): string {
  const cleaned = cleanPhoneNumber(phone);
  
  // Handle various lengths
  if (cleaned.length === 11 && cleaned.startsWith('1')) {
    // Remove leading 1 for display formatting
    const digits = cleaned.slice(1);
    return `(${digits.slice(0, 3)}) ${digits.slice(3, 6)}-${digits.slice(6, 10)}`;
  }
  
  if (cleaned.length === 10) {
    return `(${cleaned.slice(0, 3)}) ${cleaned.slice(3, 6)}-${cleaned.slice(6, 10)}`;
  }
  
  // Return partially formatted for shorter numbers
  if (cleaned.length >= 6) {
    return `(${cleaned.slice(0, 3)}) ${cleaned.slice(3, 6)}${cleaned.length > 6 ? '-' + cleaned.slice(6) : ''}`;
  }
  
  if (cleaned.length >= 3) {
    return `(${cleaned.slice(0, 3)}) ${cleaned.slice(3)}`;
  }
  
  return cleaned;
}

/**
 * Convert various phone number formats to E.164 format (+1XXXXXXXXXX)
 * Accepts formats like:
 * - 5551234567
 * - (555) 123-4567
 * - 555-123-4567
 * - 1-555-123-4567
 * - +1 555 123 4567
 */
export function formatPhoneToE164(phone: string): string {
  const cleaned = cleanPhoneNumber(phone);
  
  // Handle 11-digit numbers starting with 1
  if (cleaned.length === 11 && cleaned.startsWith('1')) {
    return `+${cleaned}`;
  }
  
  // Handle 10-digit numbers (add +1 prefix)
  if (cleaned.length === 10) {
    return `+1${cleaned}`;
  }
  
  // If already in E.164 format, return as is
  if (phone.startsWith('+1') && cleaned.length === 11) {
    return `+${cleaned}`;
  }
  
  // Return original if we can't format it properly
  return phone;
}

/**
 * Validate phone number - accepts various formats but ensures it can be converted to valid E.164
 * This validates the input AND verifies the final E.164 format is correct for backend
 */
export function validatePhoneNumber(phone: string): string | null {
  if (!phone) {
    return 'Phone number is required';
  }
  
  const cleaned = cleanPhoneNumber(phone);
  
  // Check if it's a valid 10 or 11 digit US number
  if (cleaned.length === 10) {
    // Convert to E.164 and validate final format
    const e164 = formatPhoneToE164(phone);
    if (!isValidE164(e164)) {
      return 'Invalid phone number format';
    }
    return null; // Valid 10-digit number
  }
  
  if (cleaned.length === 11 && cleaned.startsWith('1')) {
    // Convert to E.164 and validate final format
    const e164 = formatPhoneToE164(phone);
    if (!isValidE164(e164)) {
      return 'Invalid phone number format';
    }
    return null; // Valid 11-digit number with country code
  }
  
  // Check various common invalid formats
  if (cleaned.length < 10) {
    return 'Phone number is too short';
  }
  
  if (cleaned.length > 11) {
    return 'Phone number is too long';
  }
  
  if (cleaned.length === 11 && !cleaned.startsWith('1')) {
    return 'For 11-digit numbers, must start with 1';
  }
  
  return 'Please enter a valid US phone number';
}

/**
 * Check if phone number is in valid E.164 format
 */
export function isValidE164(phone: string): boolean {
  return PHONE_E164_REGEX.test(phone);
}

/**
 * Format phone number as user types (for live input formatting)
 */
export function formatPhoneAsUserTypes(value: string, previousValue: string = ''): string {
  const cleaned = cleanPhoneNumber(value);
  
  // Don't format if user is deleting (length decreased)
  if (value.length < previousValue.length) {
    return value;
  }
  
  // Format based on length
  if (cleaned.length <= 3) {
    return cleaned;
  }
  
  if (cleaned.length <= 6) {
    return `(${cleaned.slice(0, 3)}) ${cleaned.slice(3)}`;
  }
  
  if (cleaned.length <= 10) {
    return `(${cleaned.slice(0, 3)}) ${cleaned.slice(3, 6)}-${cleaned.slice(6)}`;
  }
  
  // Handle 11 digits (with country code)
  if (cleaned.length === 11 && cleaned.startsWith('1')) {
    const digits = cleaned.slice(1);
    return `+1 (${digits.slice(0, 3)}) ${digits.slice(3, 6)}-${digits.slice(6)}`;
  }
  
  // Truncate if too long
  return formatPhoneAsUserTypes(cleaned.slice(0, 10), previousValue);
}