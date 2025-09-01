import { FORM_VALIDATION } from '@/constants';

export interface LoginFormData {
  email: string;
  password: string;
}

export interface RegisterFormData {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}

export interface VerificationFormData {
  email: string;
  code: string;
}

export const validateEmail = (email: string): string | null => {
  if (!email) return 'Email is required';
  if (!FORM_VALIDATION.EMAIL_REGEX.test(email)) return 'Invalid email format';
  return null;
};

export const validatePassword = (password: string): string | null => {
  if (!password) return 'Password is required';
  if (password.length < FORM_VALIDATION.PASSWORD_MIN_LENGTH) {
    return `Password must be at least ${FORM_VALIDATION.PASSWORD_MIN_LENGTH} characters`;
  }
  if (!/(?=.*[a-z])/.test(password)) return 'Password must contain at least one lowercase letter';
  if (!/(?=.*[A-Z])/.test(password)) return 'Password must contain at least one uppercase letter';
  if (!/(?=.*\d)/.test(password)) return 'Password must contain at least one number';
  return null;
};

export const validateName = (name: string, fieldName: string): string | null => {
  if (!name) return `${fieldName} is required`;
  if (name.length < 2) return `${fieldName} must be at least 2 characters`;
  return null;
};

export const validateVerificationCode = (code: string): string | null => {
  if (!code) return 'Verification code is required';
  if (!/^\d{6}$/.test(code)) return 'Verification code must be 6 digits';
  return null;
};

export interface ChangePasswordFormData {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

export interface ProfileFormData {
  firstName: string;
  lastName: string;
}

export interface PreferencesFormData {
  systemPrompt: string;
  timezone: string;
  emailNotifications: boolean;
  voiceId: string;
  defaultLanguage: string;
}

export const validateSystemPrompt = (prompt: string): string | null => {
  if (prompt && prompt.length > 1000) {
    return 'System prompt cannot exceed 1000 characters';
  }
  return null;
};

export const validatePasswordMatch = (password: string, confirmPassword: string): string | null => {
  if (password !== confirmPassword) return 'Passwords do not match';
  return null;
};