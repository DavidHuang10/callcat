import { CheckCircle, AlertCircle } from 'lucide-react'

interface AuthAlertsProps {
  error: string
  success: string
}

export function AuthAlerts({ error, success }: AuthAlertsProps) {
  return (
    <>
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
    </>
  )
}
