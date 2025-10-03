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
        <Label htmlFor="resetToken" className="!text-white">Reset Token</Label>
        <Input
          id="resetToken"
          type="text"
          placeholder="Enter the token from your email"
          value={resetToken}
          onChange={(e) => onResetTokenChange(e.target.value)}
          className="bg-white/5 border-white/40 !text-white !placeholder-white focus:ring-blue-400 focus:border-blue-400 focus:bg-white/10"
          required
        />
        <p className="text-sm !text-white/70">
          Check your email for the reset token
        </p>
      </div>
      <div className="space-y-2">
        <Label htmlFor="password" className="!text-white">New Password</Label>
        <div className="relative">
          <Lock className="absolute left-3 top-3 h-4 w-4 text-white/60" />
          <Input
            id="password"
            type="password"
            placeholder="Enter your new password"
            value={password}
            onChange={(e) => onPasswordChange(e.target.value)}
            className="pl-10 bg-white/5 border-white/40 !text-white !placeholder-white focus:ring-blue-400 focus:border-blue-400 focus:bg-white/10"
            required
          />
        </div>
        <p className="text-xs !text-white/70">
          Must be at least 8 characters with uppercase, lowercase, and number
        </p>
      </div>
      <div className="space-y-2">
        <Label htmlFor="confirmPassword" className="!text-white">Confirm New Password</Label>
        <Input
          id="confirmPassword"
          type="password"
          placeholder="Confirm your new password"
          value={confirmPassword}
          onChange={(e) => onConfirmPasswordChange(e.target.value)}
          className="bg-white/5 border-white/40 !text-white !placeholder-white focus:ring-blue-400 focus:border-blue-400 focus:bg-white/10"
          required
        />
      </div>
      <Button type="submit" className="w-full bg-blue-800 hover:bg-blue-900 text-white" disabled={loading}>
        {loading ? 'Resetting Password...' : 'Reset Password'}
      </Button>
      <Button 
        type="button" 
        variant="outline"
        className="w-full border-white/30 text-white hover:bg-white/10"
        onClick={onBack}
      >
        Back to Email
      </Button>
    </form>
  )
}
