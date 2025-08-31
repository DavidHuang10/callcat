export interface CallTranscript {
  speaker: "business" | "ai"
  text: string
}

// Backend DTO interfaces
export interface CallRequest {
  calleeName: string
  phoneNumber: string
  subject: string
  prompt: string
  scheduledFor?: number
  aiLanguage?: string
  voiceId?: string
}

export interface CallResponse {
  callId: string
  calleeName: string
  phoneNumber: string
  callerNumber?: string
  subject: string
  prompt: string
  status: string
  scheduledFor?: number
  providerId?: string
  aiLanguage?: string
  voiceId?: string
  createdAt: number
  updatedAt: number
  completedAt?: number
  dialSuccessful?: boolean
  callAnalyzed?: boolean
}

export interface CallListResponse {
  calls: CallResponse[]
  total: number
}

export interface UpdateCallRequest {
  calleeName?: string
  phoneNumber?: string
  subject?: string
  prompt?: string
  scheduledFor?: number
  aiLanguage?: string
  voiceId?: string
}

export interface ApiResponse {
  message: string
  success: boolean
}

export interface UserResponse {
  id: string
  email: string
  firstName: string
  lastName: string
  isActive: boolean
  createdAt: number
  updatedAt: number
}

export interface UserPreferencesResponse {
  userId: string
  systemPrompt?: string
  defaultLanguage?: string
  defaultVoiceId?: string
  emailNotifications: boolean
  smsNotifications: boolean
  createdAt: number
  updatedAt: number
}

export interface UpdatePreferencesRequest {
  systemPrompt?: string
  defaultLanguage?: string
  defaultVoiceId?: string
  emailNotifications?: boolean
  smsNotifications?: boolean
}

// Legacy interface for backward compatibility (can be removed later)
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
  expandedCall: string | null
  scheduledPage: number
  completedPage: number
  searchQuery: string
}
