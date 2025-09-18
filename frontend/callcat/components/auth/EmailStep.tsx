import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Mail } from 'lucide-react'

interface EmailStepProps {
  email: string
  loading: boolean
  onEmailChange: (email: string) => void
  onSubmit: (e: React.FormEvent) => void
}

export function EmailStep({ email, loading, onEmailChange, onSubmit }: EmailStepProps) {
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
            className="pl-10 bg-white/5 border-white/40 !text-white !placeholder-white focus:ring-amber-400 focus:border-amber-400 focus:bg-white/10"
            required
          />
        </div>
      </div>
      <Button type="submit" className="w-full bg-amber-600 hover:bg-amber-700 text-white" disabled={loading}>
        {loading ? (
          <div className="flex items-center gap-2">
            <div className="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent" />
            Sending...
          </div>
        ) : 'Send Verification Code'}
      </Button>
    </form>
  )
}
