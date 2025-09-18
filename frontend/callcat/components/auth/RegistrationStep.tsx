import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Lock, User } from 'lucide-react'

interface RegistrationStepProps {
  firstName: string
  lastName: string
  password: string
  confirmPassword: string
  loading: boolean
  onFirstNameChange: (firstName: string) => void
  onLastNameChange: (lastName: string) => void
  onPasswordChange: (password: string) => void
  onConfirmPasswordChange: (confirmPassword: string) => void
  onSubmit: (e: React.FormEvent) => void
  onBack: () => void
}

export function RegistrationStep({
  firstName,
  lastName,
  password,
  confirmPassword,
  loading,
  onFirstNameChange,
  onLastNameChange,
  onPasswordChange,
  onConfirmPasswordChange,
  onSubmit,
  onBack
}: RegistrationStepProps) {
  return (
    <form onSubmit={onSubmit} className="space-y-4">
      <div className="grid grid-cols-2 gap-4">
        <div className="space-y-2">
          <Label htmlFor="firstName" className="!text-white">First Name</Label>
          <div className="relative">
            <User className="absolute left-3 top-3 h-4 w-4 text-white/60" />
            <Input
              id="firstName"
              type="text"
              placeholder="First name"
              value={firstName}
              onChange={(e) => onFirstNameChange(e.target.value)}
              className="pl-10 bg-white/5 border-white/40 !text-white !placeholder-white focus:ring-amber-400 focus:border-amber-400 focus:bg-white/10"
              required
            />
          </div>
        </div>
        <div className="space-y-2">
          <Label htmlFor="lastName" className="!text-white">Last Name</Label>
          <Input
            id="lastName"
            type="text"
            placeholder="Last name"
            value={lastName}
            onChange={(e) => onLastNameChange(e.target.value)}
            className="bg-white/5 border-white/40 !text-white !placeholder-white focus:ring-amber-400 focus:border-amber-400 focus:bg-white/10"
            required
          />
        </div>
      </div>
      <div className="space-y-2">
        <Label htmlFor="password" className="!text-white">Password</Label>
        <div className="relative">
          <Lock className="absolute left-3 top-3 h-4 w-4 text-white/60" />
          <Input
            id="password"
            type="password"
            placeholder="Create a strong password"
            value={password}
            onChange={(e) => onPasswordChange(e.target.value)}
            className="pl-10 bg-white/5 border-white/40 !text-white !placeholder-white focus:ring-amber-400 focus:border-amber-400 focus:bg-white/10"
            required
          />
        </div>
        <p className="text-xs !text-white">
          Must be at least 8 characters with uppercase, lowercase, and number
        </p>
      </div>
      <div className="space-y-2">
        <Label htmlFor="confirmPassword" className="!text-white">Confirm Password</Label>
        <Input
          id="confirmPassword"
          type="password"
          placeholder="Confirm your password"
          value={confirmPassword}
          onChange={(e) => onConfirmPasswordChange(e.target.value)}
          className="bg-white/5 border-white/40 !text-white !placeholder-white focus:ring-amber-400 focus:border-amber-400 focus:bg-white/10"
          required
        />
      </div>
      <Button type="submit" className="w-full bg-amber-600 hover:bg-amber-700 text-white" disabled={loading}>
        {loading ? (
          <div className="flex items-center gap-2">
            <div className="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent" />
            Creating Account...
          </div>
        ) : 'Create Account'}
      </Button>
      <Button 
        type="button" 
        variant="outline"
        className="w-full border-white/30 text-white hover:bg-white/10"
        onClick={onBack}
      >
        Back to Verification
      </Button>
    </form>
  )
}
