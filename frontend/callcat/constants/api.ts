export const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'https://api.call-cat.com';

export const API_ENDPOINTS = {
  AUTH: {
    LOGIN: '/api/auth/login',
    REGISTER: '/api/auth/register',
    LOGOUT: '/api/auth/logout',
    SEND_VERIFICATION: '/api/auth/send-verification',
    VERIFY_EMAIL: '/api/auth/verify-email',
    FORGOT_PASSWORD: '/api/auth/forgot-password',
    RESET_PASSWORD: '/api/auth/reset-password',
  },
  USER: {
    PROFILE: '/api/user/profile',
    PREFERENCES: '/api/user/preferences',
    CHANGE_PASSWORD: '/api/user/change-password',
  },
  CALLS: {
    BASE: '/api/calls',
    TRANSCRIPT: (callId: string) => `/api/calls/${callId}/transcript`,
  },
} as const;

export const HTTP_STATUS = {
  OK: 200,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  NOT_FOUND: 404,
  INTERNAL_SERVER_ERROR: 500,
} as const;