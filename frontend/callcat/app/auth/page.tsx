'use client'

import { useState, useEffect, Suspense } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { useAuth } from '@/contexts/AuthContext'
import { apiService } from '@/lib/api'
import { getUserTimezone } from '@/utils/timezone'
import Image from 'next/image'
import { ArrowLeft } from 'lucide-react'
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
      
      try {
        const userTimezone = getUserTimezone()
        await apiService.updateUserPreferences({ timezone: userTimezone })
      } catch (timezoneError) {
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

  useEffect(() => {
    // Lock body scroll and overscroll
    document.body.style.overflow = 'hidden'
    document.body.style.overscrollBehavior = 'none'
    
    return () => {
      // Restore body scroll and overscroll
      document.body.style.overflow = ''
      document.body.style.overscrollBehavior = ''
    }
  }, [])

  return (
    <div className="fixed inset-0 z-50 bg-purple-grid overflow-hidden overscroll-none font-sans text-white">
      {/* Header */}

      {/* Header */}
      <header className="sticky top-0 z-50 w-full border-b border-white/10 bg-black/20 backdrop-blur-md">
        <div className="container mx-auto px-6 h-20 flex items-center justify-between">
          <button
            onClick={() => router.push('/')}
            className="flex items-center gap-3 group"
          >
            <div className="bg-white/10 p-2 rounded-xl backdrop-blur-sm border border-white/10 group-hover:bg-white/20 transition-colors">
              <Image
                src="/logo.png"
                alt="CallCat Logo"
                width={32}
                height={32}
                className="w-8 h-8 drop-shadow-md"
              />
            </div>
            <span className="text-2xl font-bold tracking-tight text-white">CallCat</span>
          </button>
          
          <Button
            variant="ghost"
            className="text-white/80 hover:text-white hover:bg-white/10 gap-2"
            onClick={() => router.push('/')}
          >
            <ArrowLeft className="w-4 h-4" />
            Back to Home
          </Button>
        </div>
      </header>

      <div className="relative z-10 min-h-[calc(100vh-80px)] flex items-center justify-center p-4">
        <div className="w-full max-w-md animate-in fade-in zoom-in-95 duration-500">
          <Card className="border-white/10 bg-black/40 backdrop-blur-xl shadow-2xl">
            <CardHeader className="space-y-1 pb-6">
              <CardTitle className="text-2xl font-bold text-center text-white">
                {authMode === 'reset' ? 'Reset Password' : 'Create Account'}
              </CardTitle>
              <CardDescription className="text-center text-white/60">
                {authMode === 'reset'
                  ? "We'll help you get back in"
                  : "Join CallCat to start automating your calls"
                }
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
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

              <div className="relative pt-2">
                <div className="absolute inset-0 flex items-center">
                  <span className="w-full border-t border-white/10" />
                </div>
                <div className="relative flex justify-center text-xs uppercase">
                  <span className="bg-transparent px-2 text-white/40 backdrop-blur-xl">
                    Or
                  </span>
                </div>
              </div>

              <div className="text-center">
                <p className="text-sm text-white/60">
                  {authMode === 'reset' ? (
                    <>
                      Remember your password?{' '}
                      <button
                        onClick={() => router.push('/login')}
                        className="text-indigo-300 hover:text-indigo-200 font-medium transition-colors hover:underline"
                      >
                        Log in
                      </button>
                    </>
                  ) : (
                    <>
                      Already have an account?{' '}
                      <button
                        onClick={() => router.push('/login')}
                        className="text-indigo-300 hover:text-indigo-200 font-medium transition-colors hover:underline"
                      >
                        Log in
                      </button>
                    </>
                  )}
                </p>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
}

export default function AuthPage() {
  return (
    <Suspense fallback={
      <div className="min-h-screen relative bg-black">
        <div className="absolute inset-0 flex items-center justify-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-500"></div>
        </div>
      </div>
    }>
      <AuthPageContent />
    </Suspense>
  )
}
