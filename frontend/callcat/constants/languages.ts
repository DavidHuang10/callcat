export const SUPPORTED_LANGUAGES = [
  { code: 'en', name: 'English' },
  { code: 'es', name: 'Spanish' },
  { code: 'fr', name: 'French' },
  { code: 'pt', name: 'Portuguese' },
  { code: 'it', name: 'Italian' },
  { code: 'de', name: 'German' },
] as const

export const DEFAULT_LANGUAGE = 'en'

export const LANGUAGE_MAP: Record<string, string> = {
  en: 'English',
  es: 'Spanish', 
  fr: 'French',
  pt: 'Portuguese',
  it: 'Italian',
  de: 'German',
}

export const getLanguageName = (code: string): string => {
  return LANGUAGE_MAP[code] || 'English'
}