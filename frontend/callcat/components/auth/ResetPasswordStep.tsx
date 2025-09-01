import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Lock } from 'lucide-react'

interface ResetPasswordStepProps {
  resetToken: string
  password: string
  confirmPassword: string
  loading: boolean
  onResetTokenChange: (token: string) => void
  onPasswordChange: (password: string) => void
  onConfirmPasswordChange: (confirmPassword: string) => void
  onSubmit: (e: React.FormEvent) => void
  onBack: () => void
}

export function ResetPasswordStep({
  resetToken,
  password,
  confirmPassword,
  loading,
  onResetTokenChange,
  onPasswordChange,
  onConfirmPasswordChange,
  onSubmit,
  onBack
}: ResetPasswordStepProps) {
  return (
    <form onSubmit={onSubmit} className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="resetToken">Reset Token</Label>
        <Input
          id="resetToken"
          type="text"
          placeholder="Enter the token from your email"
          value={resetToken}
          onChange={(e) => onResetTokenChange(e.target.value)}
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
            value={password}
            onChange={(e) => onPasswordChange(e.target.value)}
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
          value={confirmPassword}
          onChange={(e) => onConfirmPasswordChange(e.target.value)}
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
        onClick={onBack}
      >
        Back to Email
      </Button>
    </form>
  )
}
