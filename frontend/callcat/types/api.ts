// User interfaces
export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  fullName?: string;
  isActive: boolean;
  createdAt?: number;
  updatedAt?: number;
}

// Backward compatibility alias
export interface UserResponse extends User {}

// User preferences
export interface UserPreferences {
  timezone?: string;
  emailNotifications: boolean;
  smsNotifications?: boolean;
  voiceId?: string;
  systemPrompt?: string;
  defaultLanguage?: string;
  defaultVoiceId?: string;
  userId?: string;
  createdAt?: number;
  updatedAt?: number;
}

// Backward compatibility alias
export interface UserPreferencesResponse extends UserPreferences {}

export interface UpdatePreferencesRequest {
  systemPrompt?: string;
  defaultLanguage?: string;
  defaultVoiceId?: string;
  emailNotifications?: boolean;
  smsNotifications?: boolean;
  timezone?: string;
}

// Call interfaces
export interface Call {
  callId: string;
  phoneNumber: string;
  status: 'SCHEDULED' | 'COMPLETED';
  aiPrompt: string;
  aiLanguage?: string;
  scheduledFor: number;
  createdAt: number;
  updatedAt: number;
  completedAt?: number | null;
  dialSuccessful?: boolean | null;
  providerId?: string | null;
}

// API request/response interfaces
export interface CallRequest {
  calleeName: string;
  phoneNumber: string;
  subject: string;
  prompt: string;
  scheduledFor?: number;
  aiLanguage: string;
  voiceId?: string;
}

export interface CallResponse {
  callId: string;
  calleeName: string;
  phoneNumber: string;
  callerNumber?: string;
  subject: string;
  prompt: string;
  status: 'SCHEDULED' | 'COMPLETED';
  scheduledFor: number;
  providerId?: string | null;
  aiLanguage?: string;
  voiceId?: string;
  createdAt: number;
  updatedAt: number;
  completedAt?: number | null;
  callAt?: number | null; // When call actually started (available for completed calls)
  /**
   * Call success status for determining UI display:
   * - null: Call was triggered but callee didn't answer (shows "No Answer" - yellow)
   * - true: Call connected successfully (shows "Connected" - green)  
   * - false: Call failed due to system/network error (shows "Failed" - yellow)
   */
  dialSuccessful?: boolean | null;
  callAnalyzed?: boolean;
  durationSec?: number | null; // Call duration in seconds (available for completed calls)
  retellCallData?: string; // JSON string containing Retell AI response data with transcript
}

export interface CallListResponse {
  calls: CallResponse[];
  total?: number;
}

// UI-specific interfaces for call display
export interface PaginatedCallsResponse {
  scheduledCalls: CallResponse[];
  completedCalls: CallResponse[];
  scheduledTotal: number;
  completedTotal: number;
  scheduledPage: number;
  completedPage: number;
  hasMoreScheduled: boolean;
  hasMoreCompleted: boolean;
}

// Transcript response from API
export interface TranscriptResponse {
  providerId: string;
  transcriptText: string;
}

export interface UpdateCallRequest {
  calleeName?: string;
  phoneNumber?: string;
  subject?: string;
  prompt?: string;
  scheduledFor?: number;
  aiLanguage?: string;
  voiceId?: string;
}

export interface CreateCallRequest {
  phoneNumber: string;
  aiPrompt: string;
  aiLanguage?: string;
  scheduledFor?: number;
  maxCallDuration?: number;
}

// Transcript interfaces
export interface CallTranscript {
  speaker: "business" | "ai";
  text: string;
}

// Authentication interfaces
export interface AuthResponse {
  token?: string;
  userId: string;
  email: string;
  fullName: string;
}

// General API interfaces
export interface ApiResponse {
  message: string;
  success: boolean;
}

export interface ApiError {
  message: string;
  success: false;
}