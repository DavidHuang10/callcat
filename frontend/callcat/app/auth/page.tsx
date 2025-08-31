'use client'

import { useState, useEffect, Suspense } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Mail, Lock, User, CheckCircle, AlertCircle } from 'lucide-react'
import { useAuth } from '@/contexts/AuthContext'
import { apiService } from '@/lib/api'

type AuthStep = 'email' | 'verification' | 'registration' | 'forgot-email' | 'reset-password'
type AuthMode = 'register' | 'reset'

interface AuthState {
  email: string
  verificationCode: string
  firstName: string
  lastName: string
  password: string
  confirmPassword: string
  resetToken: string
}

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
      })
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

  const renderStepIndicator = () => {
    if (authMode === 'reset') {
      return (
        <div className="flex items-center justify-center space-x-4 mb-8">
          <div className="flex items-center space-x-2">
            <Badge variant={currentStep === 'forgot-email' ? 'default' : 'secondary'}>1</Badge>
            <span className="text-sm">Enter Email</span>
          </div>
          <div className="w-8 h-px bg-gray-300" />
          <div className="flex items-center space-x-2">
            <Badge variant={currentStep === 'reset-password' ? 'default' : 'secondary'}>2</Badge>
            <span className="text-sm">Reset Password</span>
          </div>
        </div>
      )
    }
    
    return (
      <div className="flex items-center justify-center space-x-4 mb-8">
        <div className="flex items-center space-x-2">
          <Badge variant={currentStep === 'email' ? 'default' : 'secondary'}>1</Badge>
          <span className="text-sm">Email Verification</span>
        </div>
        <div className="w-8 h-px bg-gray-300" />
        <div className="flex items-center space-x-2">
          <Badge variant={currentStep === 'verification' ? 'default' : 'secondary'}>2</Badge>
          <span className="text-sm">Verify Code</span>
        </div>
        <div className="w-8 h-px bg-gray-300" />
        <div className="flex items-center space-x-2">
          <Badge variant={currentStep === 'registration' ? 'default' : 'secondary'}>3</Badge>
          <span className="text-sm">Registration</span>
        </div>
      </div>
    )
  }

  const renderEmailStep = () => (
    <form onSubmit={handleEmailSubmit} className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="email">Email Address</Label>
        <div className="relative">
          <Mail className="absolute left-3 top-3 h-4 w-4 text-gray-400" />
          <Input
            id="email"
            type="email"
            placeholder="Enter your email address"
            value={authState.email}
            onChange={(e) => updateAuthState('email', e.target.value)}
            className="pl-10"
            required
          />
        </div>
      </div>
      <Button type="submit" className="w-full" disabled={loading}>
        {loading ? 'Sending...' : 'Send Verification Code'}
      </Button>
    </form>
  )

  const renderForgotEmailStep = () => (
    <form onSubmit={handleForgotPasswordSubmit} className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="email">Email Address</Label>
        <div className="relative">
          <Mail className="absolute left-3 top-3 h-4 w-4 text-gray-400" />
          <Input
            id="email"
            type="email"
            placeholder="Enter your email address"
            value={authState.email}
            onChange={(e) => updateAuthState('email', e.target.value)}
            className="pl-10"
            required
          />
        </div>
        <p className="text-sm text-gray-500">
          We&apos;ll send you a password reset token
        </p>
      </div>
      <Button type="submit" className="w-full" disabled={loading}>
        {loading ? 'Sending...' : 'Send Reset Instructions'}
      </Button>
    </form>
  )

  const renderResetPasswordStep = () => (
    <form onSubmit={handleResetPasswordSubmit} className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="resetToken">Reset Token</Label>
        <Input
          id="resetToken"
          type="text"
          placeholder="Enter the token from your email"
          value={authState.resetToken}
          onChange={(e) => updateAuthState('resetToken', e.target.value)}
          required
        />
        <p className="text-sm text-gray-500">
          Check your email for the reset token
        </p>
      </div>
      <div className="space-y-2">
        <Label htmlFor="password">New Password</Label>
        <div className="relative">
          <Lock className="absolute left-3 top-3 h-4 w-4 text-gray-400" />
          <Input
            id="password"
            type="password"
            placeholder="Enter your new password"
            value={authState.password}
            onChange={(e) => updateAuthState('password', e.target.value)}
            className="pl-10"
            required
          />
        </div>
        <p className="text-xs text-gray-500">
          Must be at least 8 characters with uppercase, lowercase, and number
        </p>
      </div>
      <div className="space-y-2">
        <Label htmlFor="confirmPassword">Confirm New Password</Label>
        <Input
          id="confirmPassword"
          type="password"
          placeholder="Confirm your new password"
          value={authState.confirmPassword}
          onChange={(e) => updateAuthState('confirmPassword', e.target.value)}
          required
        />
      </div>
      <Button type="submit" className="w-full" disabled={loading}>
        {loading ? 'Resetting Password...' : 'Reset Password'}
      </Button>
      <Button 
        type="button" 
        variant="outline" 
        className="w-full"
        onClick={() => setCurrentStep('forgot-email')}
      >
        Back to Email
      </Button>
    </form>
  )

  const renderVerificationStep = () => (
    <form onSubmit={handleVerificationSubmit} className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="verificationCode">Verification Code</Label>
        <Input
          id="verificationCode"
          type="text"
          placeholder="Enter 6-digit code"
          value={authState.verificationCode}
          onChange={(e) => updateAuthState('verificationCode', e.target.value)}
          maxLength={6}
          required
        />
        <p className="text-sm text-gray-500">
          We&apos;ve sent a verification code to {authState.email}
        </p>
      </div>
      <Button type="submit" className="w-full" disabled={loading}>
        {loading ? 'Verifying...' : 'Verify Code'}
      </Button>
      <Button 
        type="button" 
        variant="outline" 
        className="w-full"
        onClick={() => setCurrentStep('email')}
      >
        Back to Email
      </Button>
    </form>
  )

  const renderRegistrationStep = () => (
    <form onSubmit={handleRegistrationSubmit} className="space-y-4">
      <div className="grid grid-cols-2 gap-4">
        <div className="space-y-2">
          <Label htmlFor="firstName">First Name</Label>
          <div className="relative">
            <User className="absolute left-3 top-3 h-4 w-4 text-gray-400" />
            <Input
              id="firstName"
              type="text"
              placeholder="First name"
              value={authState.firstName}
              onChange={(e) => updateAuthState('firstName', e.target.value)}
              className="pl-10"
              required
            />
          </div>
        </div>
        <div className="space-y-2">
          <Label htmlFor="lastName">Last Name</Label>
          <Input
            id="lastName"
            type="text"
            placeholder="Last name"
            value={authState.lastName}
            onChange={(e) => updateAuthState('lastName', e.target.value)}
            required
          />
        </div>
      </div>
      <div className="space-y-2">
        <Label htmlFor="password">Password</Label>
        <div className="relative">
          <Lock className="absolute left-3 top-3 h-4 w-4 text-gray-400" />
          <Input
            id="password"
            type="password"
            placeholder="Create a strong password"
            value={authState.password}
            onChange={(e) => updateAuthState('password', e.target.value)}
            className="pl-10"
            required
          />
        </div>
        <p className="text-xs text-gray-500">
          Must be at least 8 characters with uppercase, lowercase, and number
        </p>
      </div>
      <div className="space-y-2">
        <Label htmlFor="confirmPassword">Confirm Password</Label>
        <Input
          id="confirmPassword"
          type="password"
          placeholder="Confirm your password"
          value={authState.confirmPassword}
          onChange={(e) => updateAuthState('confirmPassword', e.target.value)}
          required
        />
      </div>
      <Button type="submit" className="w-full" disabled={loading}>
        {loading ? 'Creating Account...' : 'Create Account'}
      </Button>
      <Button 
        type="button" 
        variant="outline" 
        className="w-full"
        onClick={() => setCurrentStep('verification')}
      >
        Back to Verification
      </Button>
    </form>
  )

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
            {renderStepIndicator()}
            
            {error && (
              <div className="flex items-center space-x-2 p-3 mb-4 bg-red-50 border border-red-200 rounded-md">
                <AlertCircle className="h-4 w-4 text-red-500" />
                <span className="text-sm text-red-700">{error}</span>
              </div>
            )}
            
            {success && (
              <div className="flex items-center space-x-2 p-3 mb-4 bg-green-50 border border-green-200 rounded-md">
                <CheckCircle className="h-4 w-4 text-green-500" />
                <span className="text-sm text-green-700">{success}</span>
              </div>
            )}

            {currentStep === 'email' && renderEmailStep()}
            {currentStep === 'verification' && renderVerificationStep()}
            {currentStep === 'registration' && renderRegistrationStep()}
            {currentStep === 'forgot-email' && renderForgotEmailStep()}
            {currentStep === 'reset-password' && renderResetPasswordStep()}
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
