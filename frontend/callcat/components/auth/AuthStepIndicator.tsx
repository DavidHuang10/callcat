import { Badge } from '@/components/ui/badge'
import type { AuthStep, AuthMode } from '@/types/auth'

interface AuthStepIndicatorProps {
  currentStep: AuthStep
  authMode: AuthMode
}

export function AuthStepIndicator({ currentStep, authMode }: AuthStepIndicatorProps) {
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
