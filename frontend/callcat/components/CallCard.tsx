"use client"

import {
  Clock,
  Calendar,
  MessageCircle,
  ChevronDown,
  ChevronRight,
  RotateCcw,
  Edit3,
  Trash2,
  Timer,
  Languages,
  CheckCircle,
  AlertCircle,
  PhoneMissed,
  LucideIcon,
} from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { CallResponse, RescheduleData, EditData } from "@/types"
import { useCallDetails } from "@/hooks/useCallDetails"
import { hasAvailableTranscript, TranscriptMessage } from "@/utils/transcript"
import { formatCallTiming } from "@/utils/duration"
import { getLanguageName } from "@/constants"
import { useState } from "react"

interface CallCardProps {
  call: CallResponse
  expandedTranscripts: Set<string>
  toggleExpandedTranscript: (id: string) => void
  setActiveSection: (section: string) => void
  setRescheduleData?: (data: RescheduleData | null) => void
  onEdit?: (callId: string) => void
  onDelete?: (callId: string) => void
  onDeleteClick?: (call: CallResponse) => void
}

// Helper functions for formatting and status
const getStatusConfig = (status: string, dialSuccessful?: boolean | null) => {
  const configs: Record<string, {
    label: string;
    color: string;
    bgGradient: string;
    IconComponent: LucideIcon;
  }> = {
    SCHEDULED: {
      label: "Scheduled",
      color: "bg-blue-100 text-blue-800 border-blue-200",
      bgGradient: "from-blue-50 via-indigo-50 to-purple-50",
      IconComponent: Clock,
    },
    COMPLETED: {
      // Default completed status (when dialSuccessful is true)
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
  
  // Handle the 3-tier status logic based on dialSuccessful
  if (status === "COMPLETED") {
    if (dialSuccessful === true) {
      return configs.COMPLETED
    } else if (dialSuccessful === null) {
      return configs.NO_ANSWER
    } else if (dialSuccessful === false) {
      return configs.FAILED
    }
  }
  
  return configs[status] || configs.COMPLETED
}

const formatTimestamp = (timestamp: number) => {
  const date = new Date(timestamp)
  return {
    date: date.toLocaleDateString("en-US", { month: "short", day: "numeric" }),
    time: date.toLocaleTimeString("en-US", { hour: "numeric", minute: "2-digit" }),
    full: date.toLocaleDateString("en-US", { 
      year: "numeric", 
      month: "long", 
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit"
    }),
  }
}

const formatPhoneNumber = (phone: string) => {
  // Format +1XXXXXXXXXX to (XXX) XXX-XXXX
  const cleaned = phone.replace(/\D/g, '')
  if (cleaned.length === 11 && cleaned.startsWith('1')) {
    const areaCode = cleaned.slice(1, 4)
    const first = cleaned.slice(4, 7)
    const last = cleaned.slice(7, 11)
    return `(${areaCode}) ${first}-${last}`
  }
  return phone
}

export default function CallCard({ 
  call, 
  expandedTranscripts, 
  toggleExpandedTranscript, 
  setActiveSection,
  setRescheduleData,
  onEdit,
  onDelete,
  onDeleteClick
}: CallCardProps) {
  const [promptExpanded, setPromptExpanded] = useState(false)
  const statusConfig = getStatusConfig(call.status, call.dialSuccessful)
  const isExpanded = expandedTranscripts.has(call.callId)
  const createdDate = formatTimestamp(call.createdAt)
  const scheduledDate = call.scheduledFor ? formatTimestamp(call.scheduledFor) : null
  
  // Get call timing data for completed calls
  // If dial was unsuccessful (false), we do not want to show duration
  const callTiming = call.status === 'COMPLETED'
    ? formatCallTiming(
        call.callAt,
        call.completedAt,
        call.dialSuccessful === null ? 0 : call.durationSec,
        call.dialSuccessful === null ? undefined : call.retellCallData,
        call.scheduledFor
      )
    : null
  
  // Check if prompt is long enough to need expansion (more than ~100 chars or 2 lines)
  const isPromptLong = call.prompt && call.prompt.length > 100
  
  // Use the call details hook for transcript data (only when expanded)
  const { transcript, loading: transcriptLoading } = useCallDetails({
    callId: isExpanded ? call.callId : null,
  })
  
  // Check if transcript is available for this call
  const transcriptAvailable = hasAvailableTranscript(call)

  const handleToggleExpanded = () => {
    console.log('Transcript toggle clicked', { call: call.callId })
    toggleExpandedTranscript(call.callId)
  }

  const handleEdit = () => {
    console.log('Edit button clicked', { call: call.callId, onEdit: !!onEdit })
    if (onEdit) {
      onEdit(call.callId)
    } else {
      // Store edit data in localStorage for cross-route access
      const editData: EditData = {
        originalCallId: call.callId,
        calleeName: call.calleeName,
        phoneNumber: call.phoneNumber,
        subject: call.subject,
        prompt: call.prompt,
        scheduledFor: call.scheduledFor,
        aiLanguage: call.aiLanguage || 'en'
      }
      localStorage.setItem('editData', JSON.stringify(editData))
      
      // Navigate to make-call page
      window.location.href = '/make-call'
    }
  }

  const handleDelete = () => {
    console.log('Delete button clicked', { call: call.callId, onDeleteClick: !!onDeleteClick, onDelete: !!onDelete })
    if (onDeleteClick) {
      onDeleteClick(call)
    } else if (onDelete) {
      onDelete(call.callId)
    } else {
      console.warn('No delete handler provided')
    }
  }

  const handleReschedule = () => {
    if (setRescheduleData) {
      const rescheduleData: RescheduleData = {
        callId: call.callId,
        calleeName: call.calleeName,
        phoneNumber: call.phoneNumber,
        subject: call.subject,
        prompt: call.prompt,
        aiLanguage: call.aiLanguage || 'en'
      }
      setRescheduleData(rescheduleData)
    } else {
      // Store reschedule data in localStorage for cross-route access
      const rescheduleData: RescheduleData = {
        callId: call.callId,
        calleeName: call.calleeName,
        phoneNumber: call.phoneNumber,
        subject: call.subject,
        prompt: call.prompt,
        aiLanguage: call.aiLanguage || 'en'
      }
      localStorage.setItem('rescheduleData', JSON.stringify(rescheduleData))
    }
    
    // Navigate to make-call page
    if (!setRescheduleData) {
      window.location.href = '/make-call'
    } else {
      setActiveSection("make-call")
    }
  }

  return (
    <Card
      className={`bg-gradient-to-br ${statusConfig.bgGradient} border-0 shadow-md hover:shadow-xl transition-all duration-500 hover:scale-[1.03] hover:-translate-y-1 group h-fit rounded-xl overflow-hidden relative`}
    >
      <CardContent className="p-4">
        {/* Header Section */}
        <div className="flex items-start justify-between mb-3">
          <div className="flex items-center gap-3 min-w-0 flex-1">
            <div className="min-w-0 flex-1">
              <h3 className="font-semibold text-gray-800 text-base truncate">{call.calleeName}</h3>
              <p className="text-xs text-gray-600 truncate">{formatPhoneNumber(call.phoneNumber)}</p>
            </div>
          </div>
          <Badge variant="outline" className={`${statusConfig.color} font-medium px-3 py-1.5 text-xs flex-shrink-0 ml-2 rounded-full shadow-sm flex items-center gap-1.5`}>
            <statusConfig.IconComponent className="w-3 h-3" />
            {statusConfig.label}
          </Badge>
        </div>

        {/* Subject Section */}
        <div className="mb-3">
          <p className="text-gray-700 text-sm leading-relaxed line-clamp-2 mb-2">{call.subject}</p>
          <div className="bg-white/70 rounded-xl p-3 border border-white/30 backdrop-blur-sm">
            <p className={`text-xs text-gray-800 font-medium transition-all duration-200 ${
              promptExpanded ? '' : 'line-clamp-2'
            }`}>
              {call.prompt}
            </p>
            {isPromptLong && (
              <button
                onClick={() => setPromptExpanded(!promptExpanded)}
                className="text-xs text-blue-600 hover:text-blue-700 mt-1 font-medium transition-colors duration-200"
              >
                {promptExpanded ? 'Show Less' : 'Show More'}
              </button>
            )}
          </div>
        </div>

        {/* Metadata Section */}
        {call.status === 'COMPLETED' && callTiming && callTiming.hasTimingData && call.dialSuccessful === true ? (
          // 4-column layout for completed calls with duration
          <div className="grid grid-cols-4 gap-2 text-xs text-gray-600 mb-3">
            <div className="flex items-center gap-1 truncate">
              <Calendar className="w-3 h-3 flex-shrink-0" />
              <span className="truncate">{call.callAt ? formatTimestamp(call.callAt).date : createdDate.date}</span>
            </div>
            <div className="flex items-center gap-1 truncate">
              <Clock className="w-3 h-3 flex-shrink-0" />
              <span className="truncate">{callTiming.startTime}</span>
            </div>
            <div className="flex items-center gap-1 truncate">
              <Timer className="w-3 h-3 flex-shrink-0" />
              <span className="truncate">{callTiming.duration}</span>
            </div>
            <div className="flex items-center gap-1 truncate">
              <Languages className="w-3 h-3 flex-shrink-0" />
              <span className="truncate">{getLanguageName(call.aiLanguage || 'en')}</span>
            </div>
          </div>
        ) : (
          // 3-column layout for scheduled calls or completed calls without timing data
          <div className="grid grid-cols-3 gap-2 text-xs text-gray-600 mb-3">
            <div className="flex items-center gap-1 truncate">
              <Calendar className="w-3 h-3 flex-shrink-0" />
              <span className="truncate">{scheduledDate ? scheduledDate.date : createdDate.date}</span>
            </div>
            <div className="flex items-center gap-1 truncate">
              <Clock className="w-3 h-3 flex-shrink-0" />
              <span className="truncate">{scheduledDate ? scheduledDate.time : createdDate.time}</span>
            </div>
            <div className="flex items-center gap-1 truncate">
              <Languages className="w-3 h-3 flex-shrink-0" />
              <span className="truncate">{getLanguageName(call.aiLanguage || 'en')}</span>
            </div>
          </div>
        )}

        {/* Actions Section */}
        <div className="flex items-center justify-center gap-2 relative z-10">
          {/* Scheduled calls: Edit, Delete, and Transcript/Reschedule if available */}
          {call.status === "SCHEDULED" && (
            <>
              <Button
                variant="ghost"
                size="sm"
                className="text-purple-600 hover:text-purple-700 hover:bg-purple-50 transition-all duration-300 h-8 px-3 text-xs rounded-lg hover:scale-105"
                onClick={handleEdit}
              >
                <Edit3 className="w-3 h-3 mr-1" />
                <span className="hidden sm:inline">Edit</span>
              </Button>
              <Button
                variant="ghost"
                size="sm"
                className="text-red-600 hover:text-red-700 hover:bg-red-50 transition-all duration-300 h-8 px-3 text-xs rounded-lg hover:scale-105"
                onClick={handleDelete}
              >
                <Trash2 className="w-3 h-3 mr-1" />
                <span className="hidden sm:inline">Delete</span>
              </Button>
            </>
          )}

          {/* Transcript Button - show for calls with available transcripts */}
          {transcriptAvailable && (
            <Button
              variant="ghost"
              size="sm"
              className="text-blue-600 hover:text-blue-700 hover:bg-blue-50 transition-all duration-300 h-8 px-3 text-xs rounded-lg hover:scale-105"
              onClick={handleToggleExpanded}
            >
              {isExpanded ? <ChevronDown className="w-3 h-3" /> : <ChevronRight className="w-3 h-3" />}
              <MessageCircle className="w-3 h-3 ml-1" />
              <span className="ml-1 hidden sm:inline">Transcript</span>
            </Button>
          )}

          {/* Delete and Reschedule buttons for completed calls */}
          {call.status === "COMPLETED" && (
            <>
              <Button
                variant="ghost"
                size="sm"
                className="text-red-600 hover:text-red-700 hover:bg-red-50 transition-all duration-300 h-8 px-3 text-xs rounded-lg hover:scale-105"
                onClick={handleDelete}
              >
                <Trash2 className="w-3 h-3 mr-1" />
                <span className="hidden sm:inline">Delete</span>
              </Button>
              <Button
                variant="ghost"
                size="sm"
                className="text-purple-600 hover:text-purple-700 hover:bg-purple-50 transition-all duration-300 h-8 px-3 text-xs rounded-lg hover:scale-105"
                onClick={handleReschedule}
              >
                <RotateCcw className="w-3 h-3 mr-1" />
                <span className="hidden sm:inline">Reschedule</span>
              </Button>
            </>
          )}
        </div>

        {/* Transcript Section */}
        {transcriptAvailable && isExpanded && (
          <div className="mt-3 animate-in slide-in-from-top-2 duration-300">
            <div className="bg-white/80 rounded-2xl p-5 border border-white/60 backdrop-blur-sm shadow-lg">
              <div className="flex items-center gap-2 mb-4">
                <MessageCircle className="w-4 h-4 text-blue-500" />
                <span className="font-medium text-gray-800 text-sm">Call Transcript</span>
              </div>
              
              {/* Scrollable Conversation Container */}
              <div className="h-[300px] overflow-y-auto space-y-3 pr-2" style={{ scrollbarWidth: 'thin' }}>
                {transcriptLoading && (
                  <div className="text-center text-gray-500 text-sm py-8">
                    Loading transcript...
                  </div>
                )}
                
                {!transcriptLoading && transcript.length > 0 && (
                  <>
                    {transcript.map((message: TranscriptMessage, index: number) => (
                      <div key={index} className={`flex ${message.speaker === 'user' ? 'justify-end' : 'justify-start'}`}>
                        <div className={`max-w-[75%] rounded-lg px-3 py-2 ${
                          message.speaker === 'agent'
                            ? 'bg-blue-50 text-blue-800 border border-blue-100'
                            : 'bg-green-50 text-green-800 border border-green-100'
                        }`}>
                          <div className="text-xs font-medium mb-1 opacity-75">
                            {message.speaker === 'agent' ? 'AI Assistant' : 'Caller'}
                          </div>
                          <div className="text-sm leading-relaxed">
                            {message.text}
                          </div>
                        </div>
                      </div>
                    ))}
                  </>
                )}
                
                {!transcriptLoading && transcript.length === 0 && (
                  <div className="text-center text-gray-500 text-sm py-8">
                    Transcript unavailable
                  </div>
                )}
              </div>
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  )
}
