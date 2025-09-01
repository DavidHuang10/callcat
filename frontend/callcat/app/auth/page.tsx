'use client'

import { useState, useEffect, Suspense } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { useAuth } from '@/contexts/AuthContext'
import { apiService } from '@/lib/api'
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
      setSuccess('Password reset successful! Redirecting to login...')
      setTimeout(() => router.push('/login?message=password-reset'), 1500)
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
      setSuccess('Registration successful! Redirecting...')
      setTimeout(() => router.push('/'), 1500)
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
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">
            {authMode === 'reset' ? 'Reset Your Password' : 'Welcome to CallCat'}
          </h1>
          <p className="text-gray-600">
            {authMode === 'reset' ? 'Enter your email to reset your password' : 'Create your account to get started'}
          </p>
        </div>

        <Card>
          <CardHeader>
            <CardTitle className="text-center">
              {authMode === 'reset' ? 'Password Reset' : 'Account Setup'}
            </CardTitle>
            <CardDescription className="text-center">
              {authMode === 'reset' 
                ? 'Follow the steps below to reset your password'
                : 'Follow the steps below to create your account'
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
          <p className="text-sm text-gray-600">
            {authMode === 'reset' ? (
              <>
                Remember your password?{' '}
                <button 
                  onClick={() => router.push('/login')}
                  className="text-blue-600 hover:text-blue-800 font-medium"
                >
                  Sign in
                </button>
              </>
            ) : (
              <>
                Already have an account?{' '}
                <button 
                  onClick={() => router.push('/login')}
                  className="text-blue-600 hover:text-blue-800 font-medium"
                >
                  Sign in
                </button>
              </>
            )}
          </p>
        </div>
      </div>
    </div>
  )
}

export default function AuthPage() {
  return (
    <Suspense fallback={<div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center"><div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div></div>}>
      <AuthPageContent />
    </Suspense>
  )
}
