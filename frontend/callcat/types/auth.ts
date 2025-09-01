export type AuthStep = 'email' | 'verification' | 'registration' | 'forgot-email' | 'reset-password'
export type AuthMode = 'register' | 'reset'

export interface AuthState {
  email: string
  verificationCode: string
  firstName: string
  lastName: string
  password: string
  confirmPassword: string
  resetToken: string
}
