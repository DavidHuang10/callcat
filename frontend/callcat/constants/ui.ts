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

export const PAGINATION = {
  CALLS_PER_PAGE: 6,
  DEFAULT_LIMIT: 20,
} as const;

export const GRID_LAYOUTS = {
  CALL_CARDS: 'grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-4',
  RESPONSIVE_2_3: 'grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-4',
  STATS_GRID: 'grid grid-cols-1 md:grid-cols-3 gap-4',
} as const;