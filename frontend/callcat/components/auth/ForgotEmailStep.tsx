import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Mail } from 'lucide-react'

interface ForgotEmailStepProps {
  email: string
  loading: boolean
  onEmailChange: (email: string) => void
  onSubmit: (e: React.FormEvent) => void
}

export function ForgotEmailStep({ email, loading, onEmailChange, onSubmit }: ForgotEmailStepProps) {
  return (
    <form onSubmit={onSubmit} className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="email" className="!text-white">Email Address</Label>
        <div className="relative">
          <Mail className="absolute left-3 top-3 h-4 w-4 text-white/60" />
          <Input
            id="email"
            type="email"
            placeholder="Enter your email address"
            value={email}
            onChange={(e) => onEmailChange(e.target.value)}
            className="pl-10 bg-white/5 border-white/40 !text-white !placeholder-white focus:ring-blue-400 focus:border-blue-400 focus:bg-white/10"
            required
          />
        </div>
        <p className="text-sm !text-white/70">
          We&apos;ll send you a password reset token
        </p>
      </div>
      <Button type="submit" className="w-full bg-blue-800 hover:bg-blue-900 text-white" disabled={loading}>
        {loading ? 'Sending...' : 'Send Reset Instructions'}
      </Button>
    </form>
  )
}
