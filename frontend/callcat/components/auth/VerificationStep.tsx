import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'

interface VerificationStepProps {
  verificationCode: string
  email: string
  loading: boolean
  onVerificationCodeChange: (code: string) => void
  onSubmit: (e: React.FormEvent) => void
  onBack: () => void
}

export function VerificationStep({ 
  verificationCode, 
  email, 
  loading, 
  onVerificationCodeChange, 
  onSubmit, 
  onBack 
}: VerificationStepProps) {
  return (
    <form onSubmit={onSubmit} className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="verificationCode" className="!text-white">Verification Code</Label>
        <Input
          id="verificationCode"
          type="text"
          placeholder="Enter 6-digit code"
          value={verificationCode}
          onChange={(e) => onVerificationCodeChange(e.target.value)}
          maxLength={6}
          className="bg-white/5 border-white/40 !text-white !placeholder-white focus:ring-amber-400 focus:border-amber-400 focus:bg-white/10"
          required
        />
        <p className="text-sm !text-white/70">
          We&apos;ve sent a verification code to {email}
        </p>
      </div>
      <Button type="submit" className="w-full bg-amber-600 hover:bg-amber-700 text-white" disabled={loading}>
        {loading ? (
          <div className="flex items-center gap-2">
            <div className="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent" />
            Verifying...
          </div>
        ) : 'Verify Code'}
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
