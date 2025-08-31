import { PHONE_NUMBER_REGEX, DEFAULT_CALL_SETTINGS } from '@/constants';

export interface CreateCallFormData {
  phoneNumber: string;
  aiPrompt: string;
  scheduledFor?: Date;
  aiLanguage?: string;
}

export const validatePhoneNumber = (phone: string): string | null => {
  if (!phone) return 'Phone number is required';
  if (!PHONE_NUMBER_REGEX.test(phone)) {
    return 'Phone number must be in format +1XXXXXXXXXX';
  }
  return null;
};

export const validateAiPrompt = (prompt: string): string | null => {
  if (!prompt) return 'AI prompt is required';
  if (prompt.length < 10) return 'AI prompt must be at least 10 characters';
  if (prompt.length > 1000) return 'AI prompt must be less than 1000 characters';
  return null;
};

export const validateScheduledTime = (scheduledFor?: Date): string | null => {
  if (!scheduledFor) return null; // Optional field
  
  const now = new Date();
  if (scheduledFor <= now) {
    return 'Scheduled time must be in the future';
  }
  
  // Don't allow scheduling more than 30 days in advance
  const thirtyDaysFromNow = new Date(now.getTime() + (30 * 24 * 60 * 60 * 1000));
  if (scheduledFor > thirtyDaysFromNow) {
    return 'Cannot schedule calls more than 30 days in advance';
  }
  
  return null;
};

export const formatPhoneNumber = (phone: string): string => {
  const cleaned = phone.replace(/\D/g, '');
  if (cleaned.length === 11 && cleaned.startsWith('1')) {
    return `+${cleaned}`;
  }
  if (cleaned.length === 10) {
    return `+1${cleaned}`;
  }
  return phone; // Return original if doesn't match expected format
};