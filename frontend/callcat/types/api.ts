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
}

// Call interfaces
export interface Call {
  callId: string;
  phoneNumber: string;
  status: 'SCHEDULED' | 'COMPLETED';
  aiPrompt: string;
  aiLanguage?: string;
  scheduledFor?: number;
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
  aiLanguage?: string;
  voiceId?: string;
}

export interface CallResponse {
  callId: string;
  calleeName: string;
  phoneNumber: string;
  callerNumber?: string;
  subject: string;
  prompt: string;
  status: string;
  scheduledFor?: number;
  providerId?: string;
  aiLanguage?: string;
  voiceId?: string;
  createdAt: number;
  updatedAt: number;
  completedAt?: number;
  dialSuccessful?: boolean;
  callAnalyzed?: boolean;
}

export interface CallListResponse {
  calls: CallResponse[];
  total: number;
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