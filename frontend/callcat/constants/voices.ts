export const SUPPORTED_VOICES = [
  { value: 'default', label: 'Default' },
  { value: 'friendly', label: 'Friendly' },
  { value: 'professional', label: 'Professional' },
] as const

export const DEFAULT_VOICE = 'default'

export const VOICE_MAP: Record<string, string> = {
  default: 'Default',
  friendly: 'Friendly', 
  professional: 'Professional',
}

export const getVoiceName = (value: string): string => {
  return VOICE_MAP[value] || 'Default'
}