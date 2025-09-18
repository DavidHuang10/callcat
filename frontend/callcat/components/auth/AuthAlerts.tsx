import { CheckCircle, AlertCircle } from 'lucide-react'

interface AuthAlertsProps {
  error: string
  success: string
}

export function AuthAlerts({ error, success }: AuthAlertsProps) {
  return (
    <>
      {error && (
        <div className="flex items-center space-x-2 p-3 mb-4 bg-red-500/20 border border-red-400/30 rounded-md backdrop-blur-sm">
          <AlertCircle className="h-4 w-4 text-red-300" />
          <span className="text-sm text-red-200">{error}</span>
        </div>
      )}
      
      {success && (
        <div className="flex items-center space-x-2 p-3 mb-4 bg-green-500/20 border border-green-400/30 rounded-md backdrop-blur-sm">
          <CheckCircle className="h-4 w-4 text-green-300" />
          <span className="text-sm text-green-200">{success}</span>
        </div>
      )}
    </>
  )
}
