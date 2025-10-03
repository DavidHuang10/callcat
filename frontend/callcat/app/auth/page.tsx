'use client'

import { useState, useEffect, Suspense } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { useAuth } from '@/contexts/AuthContext'
import { apiService } from '@/lib/api'
import { getUserTimezone } from '@/utils/timezone'
import Image from 'next/image'
import {
  AuthStepIndicator,
  AuthAlerts,
  EmailStep,
  VerificationStep,
  RegistrationStep,
  ForgotEmailStep,
  ResetPasswordStep
} from '@/components/auth'
import type { AuthStep, AuthMode, AuthState } from '@/types/auth'

function AuthPageContent() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const { login } = useAuth()
  const [authMode, setAuthMode] = useState<AuthMode>('register')
  const [currentStep, setCurrentStep] = useState<AuthStep>('email')
  const [authState, setAuthState] = useState<AuthState>({
    email: '',
    verificationCode: '',
    firstName: '',
    lastName: '',
    password: '',
    confirmPassword: '',
    resetToken: ''
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    const mode = searchParams.get('mode')
    if (mode === 'reset') {
      setAuthMode('reset')
      setCurrentStep('forgot-email')
    } else {
      setAuthMode('register')
      setCurrentStep('email')
    }
  }, [searchParams])

  const handleEmailSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError('')
    
    try {
      if (authMode === 'register') {
        await apiService.sendVerification(authState.email)
        setSuccess('Verification code sent to your email!')
        setCurrentStep('verification')
      }
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Failed to send verification code')
    } finally {
      setLoading(false)
    }
  }

  const handleForgotPasswordSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError('')
    
    try {
      await apiService.forgotPassword(authState.email)
      setSuccess('Password reset instructions sent to your email!')
      setCurrentStep('reset-password')
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Failed to send password reset email')
    } finally {
      setLoading(false)
    }
  }

  const handleResetPasswordSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError('')
    
    if (authState.password !== authState.confirmPassword) {
      setError('Passwords do not match')
      setLoading(false)
      return
    }
    
    try {
      await apiService.resetPassword(authState.resetToken, authState.password)
      router.push('/login?message=password-reset')
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Password reset failed')
    } finally {
      setLoading(false)
    }
  }

  const handleVerificationSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError('')
    
    try {
      await apiService.verifyEmail(authState.email, authState.verificationCode)
      setSuccess('Email verified successfully!')
      setCurrentStep('registration')
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Invalid verification code')
    } finally {
      setLoading(false)
    }
  }

  const handleRegistrationSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError('')
    
    if (authState.password !== authState.confirmPassword) {
      setError('Passwords do not match')
      setLoading(false)
      return
    }
    
    try {
      const data = await apiService.register(
        authState.email,
        authState.password,
        authState.firstName,
        authState.lastName
      )
      login({
        id: data.userId,
        email: data.email,
        fullName: data.fullName
      }, data.token)
      
      // Auto-set user timezone after successful registration
      try {
        const userTimezone = getUserTimezone()
        await apiService.updateUserPreferences({ timezone: userTimezone })
      } catch (timezoneError) {
        // Don't block registration flow if timezone update fails
        console.warn('Failed to set user timezone:', timezoneError)
      }
      
      router.push('/')
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Registration failed')
    } finally {
      setLoading(false)
    }
  }

  const updateAuthState = (field: keyof AuthState, value: string) => {
    setAuthState(prev => ({ ...prev, [field]: value }))
  }













  return (
    <div className="min-h-screen relative">
      <div
        className="fixed inset-0 bg-cover bg-center bg-no-repeat"
        style={{
          backgroundImage: "url('/business-cats.jpeg')",
        }}
      />

      <div className="fixed inset-0 bg-black/40" />

      {/* Header */}
      <header className="relative z-20 backdrop-blur-sm bg-white/5 border-b border-white/20 sticky top-0">
        <div className="container mx-auto px-4 py-4 flex items-center justify-between">
          <button
            onClick={() => router.push('/')}
            className="flex items-center gap-2 hover:opacity-80 transition-opacity cursor-pointer"
          >
            <Image
              src="/logo.png"
              alt="CallCat Logo"
              width={40}
              height={40}
              className="drop-shadow-lg"
            />
            <span className="text-xl font-bold text-white">CallCat</span>
          </button>
          <div className="flex items-center gap-3">
            <Button
              variant="ghost"
              className="text-white hover:bg-white/20 hover:text-white"
              onClick={() => router.push('/login')}
            >
              Log In
            </Button>
            <Button
              className="bg-amber-600 hover:bg-amber-700 text-white"
              onClick={() => router.push('/auth')}
            >
              Sign Up
            </Button>
          </div>
        </div>
      </header>

      <div className="relative z-10 min-h-[calc(100vh-73px)] flex items-center justify-center p-4">
        <div className="w-full max-w-md">
          <Card className="border border-white/20 shadow-lg bg-white/3 backdrop-blur-sm">
          <CardHeader>
            <CardTitle className="text-center text-white">
              {authMode === 'reset' ? 'Reset Password' : 'Create Your Account'}
            </CardTitle>
            <CardDescription className="text-center text-white/80">
              {authMode === 'reset'
                ? "We'll help you get back in"
                : "Just a few quick steps and you'll be ready to go"
              }
            </CardDescription>
          </CardHeader>
          <CardContent>
            <AuthStepIndicator currentStep={currentStep} authMode={authMode} />
            
            <AuthAlerts error={error} success={success} />

            {currentStep === 'email' && (
              <EmailStep
                email={authState.email}
                loading={loading}
                onEmailChange={(email) => updateAuthState('email', email)}
                onSubmit={handleEmailSubmit}
              />
            )}
            {currentStep === 'verification' && (
              <VerificationStep
                verificationCode={authState.verificationCode}
                email={authState.email}
                loading={loading}
                onVerificationCodeChange={(code) => updateAuthState('verificationCode', code)}
                onSubmit={handleVerificationSubmit}
                onBack={() => setCurrentStep('email')}
              />
            )}
            {currentStep === 'registration' && (
              <RegistrationStep
                firstName={authState.firstName}
                lastName={authState.lastName}
                password={authState.password}
                confirmPassword={authState.confirmPassword}
                loading={loading}
                onFirstNameChange={(firstName) => updateAuthState('firstName', firstName)}
                onLastNameChange={(lastName) => updateAuthState('lastName', lastName)}
                onPasswordChange={(password) => updateAuthState('password', password)}
                onConfirmPasswordChange={(confirmPassword) => updateAuthState('confirmPassword', confirmPassword)}
                onSubmit={handleRegistrationSubmit}
                onBack={() => setCurrentStep('verification')}
              />
            )}
            {currentStep === 'forgot-email' && (
              <ForgotEmailStep
                email={authState.email}
                loading={loading}
                onEmailChange={(email) => updateAuthState('email', email)}
                onSubmit={handleForgotPasswordSubmit}
              />
            )}
            {currentStep === 'reset-password' && (
              <ResetPasswordStep
                resetToken={authState.resetToken}
                password={authState.password}
                confirmPassword={authState.confirmPassword}
                loading={loading}
                onResetTokenChange={(token) => updateAuthState('resetToken', token)}
                onPasswordChange={(password) => updateAuthState('password', password)}
                onConfirmPasswordChange={(confirmPassword) => updateAuthState('confirmPassword', confirmPassword)}
                onSubmit={handleResetPasswordSubmit}
                onBack={() => setCurrentStep('forgot-email')}
              />
            )}
          </CardContent>
        </Card>

          <div className="text-center mt-6">
            <p className="text-sm text-white/80">
              {authMode === 'reset' ? (
                <>
                  Remember your password?{' '}
                  <button
                    onClick={() => router.push('/login')}
                    className="text-amber-400 hover:text-amber-300 font-medium transition-colors"
                  >
                    Log in
                  </button>
                </>
              ) : (
                <>
                  Already have an account?{' '}
                  <button
                    onClick={() => router.push('/login')}
                    className="text-amber-400 hover:text-amber-300 font-medium transition-colors"
                  >
                    Log in
                  </button>
                </>
              )}
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}

export default function AuthPage() {
  return (
    <Suspense fallback={
      <div className="min-h-screen relative">
        <div className="fixed inset-0 bg-cover bg-center bg-no-repeat" style={{ backgroundImage: "url('/business-cats.jpeg')" }} />
        <div className="fixed inset-0 bg-black/40" />
        <header className="relative z-20 backdrop-blur-sm bg-white/5 border-b border-white/20 sticky top-0">
          <div className="container mx-auto px-4 py-4 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <div className="w-10 h-10 bg-white/20 rounded-lg animate-pulse"></div>
              <div className="h-6 w-24 bg-white/20 rounded animate-pulse"></div>
            </div>
          </div>
        </header>
        <div className="relative z-10 min-h-[calc(100vh-73px)] flex items-center justify-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-amber-400"></div>
        </div>
      </div>
    }>
      <AuthPageContent />
    </Suspense>
  )
}
