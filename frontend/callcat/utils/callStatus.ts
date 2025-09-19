import {
  Clock,
  CheckCircle,
  AlertCircle,
  PhoneMissed,
  LucideIcon,
} from "lucide-react"

export interface StatusConfig {
  label: string
  color: string
  bgGradient: string
  IconComponent: LucideIcon
}

export const CALL_STATUS_CONFIG: Record<string, StatusConfig> = {
  SCHEDULED: {
    label: "Scheduled",
    color: "bg-blue-100 text-blue-800 border-blue-200",
    bgGradient: "from-blue-50 via-indigo-50 to-purple-50",
    IconComponent: Clock,
  },
  COMPLETED: {
    label: "Connected",
    color: "bg-green-100 text-green-800 border-green-200",
    bgGradient: "from-green-50 via-emerald-50 to-teal-50",
    IconComponent: CheckCircle,
  },
  NO_ANSWER: {
    label: "No Answer",
    color: "bg-yellow-100 text-yellow-800 border-yellow-200",
    bgGradient: "from-yellow-50 via-amber-50 to-orange-50",
    IconComponent: PhoneMissed,
  },
  FAILED: {
    label: "Failed",
    color: "bg-red-100 text-red-800 border-red-200",
    bgGradient: "from-red-50 via-pink-50 to-rose-50",
    IconComponent: AlertCircle,
  },
}

/**
 * Get status configuration based on call status and dial success
 * Handles the 3-tier status logic for completed calls
 */
export function getStatusConfig(status: string, dialSuccessful?: boolean | null): StatusConfig {
  // Handle the 3-tier status logic based on dialSuccessful
  if (status === "COMPLETED") {
    if (dialSuccessful === true) {
      return CALL_STATUS_CONFIG.COMPLETED
    } else if (dialSuccessful === null) {
      return CALL_STATUS_CONFIG.NO_ANSWER
    } else if (dialSuccessful === false) {
      return CALL_STATUS_CONFIG.FAILED
    }
  }

  return CALL_STATUS_CONFIG[status] || CALL_STATUS_CONFIG.COMPLETED
}