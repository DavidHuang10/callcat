export interface CallTranscript {
  speaker: "business" | "ai"
  text: string
}

export interface CallRecord {
  id: string
  business: string
  phone: string
  purpose: string
  status: "successful" | "failed" | "scheduled" | "needs_attention"
  timestamp: string
  duration: string | null
  language: string
  transcript: CallTranscript[]
  result: string
  icon: any // Lucide icon component
}

export interface StatusConfig {
  label: string
  color: string
  bgGradient: string
  icon: any
}

export interface DashboardState {
  activeSection: string
  isMobile: boolean
  sidebarOpen: boolean
  selectedLanguage: string
  expandedCall: number | null
  scheduledPage: number
  completedPage: number
  searchQuery: string
}
