export const DASHBOARD_SECTIONS = {
  HOME: 'home',
  MAKE_CALL: 'make-call', 
  HISTORY: 'history',
  SETTINGS: 'settings',
} as const;

export const CALL_STATUS = {
  SCHEDULED: 'SCHEDULED',
  COMPLETED: 'COMPLETED',
} as const;

export const PHONE_NUMBER_REGEX = /^\+1[0-9]{10}$/;

export const FORM_VALIDATION = {
  EMAIL_REGEX: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
  PASSWORD_MIN_LENGTH: 8,
  PHONE_FORMAT: '+1XXXXXXXXXX',
} as const;

export const DEFAULT_CALL_SETTINGS = {
  AI_LANGUAGE: 'en',
  MAX_CALL_DURATION: 300, // 5 minutes in seconds
} as const;