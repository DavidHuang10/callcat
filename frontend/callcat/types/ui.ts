export type DashboardSection = 'home' | 'make-call' | 'history' | 'active' | 'profile';

export type LoadingState = 'idle' | 'loading' | 'success' | 'error';

export type CallStatus = "successful" | "failed" | "scheduled" | "needs_attention";

export interface FormState<T = any> {
  data: T;
  errors: Record<string, string>;
  isSubmitting: boolean;
  isValid: boolean;
}

export interface CallCardProps {
  call: import('./api').Call;
  onEdit?: (callId: string) => void;
  onDelete?: (callId: string) => void;
  onView?: (callId: string) => void;
}

export interface SectionProps {
  className?: string;
  children?: React.ReactNode;
}

// Reschedule data interface
export interface RescheduleData {
  callId: string;
  calleeName: string;
  phoneNumber: string;
  subject: string;
  prompt: string;
}

// Dashboard state management
export interface DashboardState {
  activeSection: string;
  isMobile: boolean;
  sidebarOpen: boolean;
  sidebarCollapsed: boolean;
  selectedLanguage: string;
  expandedTranscripts: Set<string>;
  scheduledPage: number;
  completedPage: number;
  searchQuery: string;
  rescheduleData: RescheduleData | null;
}

// Legacy interface for mock data (can be removed later)
export interface CallRecord {
  id: string;
  business: string;
  phone: string;
  purpose: string;
  status: CallStatus;
  timestamp: string;
  duration: string | null;
  language: string;
  transcript: import('./api').CallTranscript[];
  result: string;
  icon: any; // Lucide icon component
}

export interface StatusConfig {
  label: string;
  color: string;
  bgGradient: string;
  icon: any;
}