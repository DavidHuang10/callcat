"use client"

import {
  Clock,
  Timer,
  Globe,
  Calendar,
  MessageCircle,
  ChevronDown,
  ChevronRight,
  RotateCcw,
  Phone,
  PhoneMissed,
  Edit3,
  Trash2,
  User,
} from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from "@/components/ui/collapsible"
import { CallResponse } from "@/types"
import { useCallDetails } from "@/hooks/useCallDetails"
import { hasAvailableTranscript, TranscriptMessage } from "@/utils/transcript"
import { useState } from "react"

interface CallCardProps {
  call: CallResponse
  expandedTranscripts: Set<string>
  toggleExpandedTranscript: (id: string) => void
  setActiveSection: (section: string) => void
  onEdit?: (callId: string) => void
  onDelete?: (callId: string) => void
}

// Helper functions for formatting and status
const getStatusConfig = (status: string, dialSuccessful?: boolean | null) => {
  const configs: Record<string, any> = {
    SCHEDULED: {
      label: "Scheduled",
      color: "bg-blue-100 text-blue-800 border-blue-200",
      bgGradient: "from-blue-50 via-indigo-50 to-purple-50",
      icon: "⏰",
    },
    COMPLETED: {
      // Default completed status (when dialSuccessful is true)
      label: "Connected", 
      color: "bg-green-100 text-green-800 border-green-200",
      bgGradient: "from-green-50 via-emerald-50 to-teal-50",
      icon: "✓",
    },
    NO_ANSWER: {
      label: "No Answer",
      color: "bg-yellow-100 text-yellow-800 border-yellow-200",
      bgGradient: "from-yellow-50 via-amber-50 to-orange-50",
      icon: "!",
    },
    FAILED: {
      label: "Failed",
      color: "bg-red-100 text-red-800 border-red-200",
      bgGradient: "from-red-50 via-pink-50 to-rose-50",
      icon: "⚠️",
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
  onEdit,
  onDelete 
}: CallCardProps) {
  const [promptExpanded, setPromptExpanded] = useState(false)
  const statusConfig = getStatusConfig(call.status, call.dialSuccessful)
  const isExpanded = expandedTranscripts.has(call.callId)
  const createdDate = formatTimestamp(call.createdAt)
  const scheduledDate = call.scheduledFor ? formatTimestamp(call.scheduledFor) : null
  
  // Check if prompt is long enough to need expansion (more than ~100 chars or 2 lines)
  const isPromptLong = call.prompt && call.prompt.length > 100
  
  // Use the call details hook for transcript data (only when expanded)
  const { transcript, loading: transcriptLoading } = useCallDetails({
    callId: isExpanded ? call.callId : null,
  })
  
  // Check if transcript is available for this call
  const transcriptAvailable = hasAvailableTranscript(call)

  const handleToggleExpanded = () => {
    toggleExpandedTranscript(call.callId)
  }

  const handleEdit = () => {
    if (onEdit) {
      onEdit(call.callId)
    } else {
      setActiveSection("make-call")
    }
  }

  const handleDelete = () => {
    if (onDelete) {
      onDelete(call.callId)
    }
  }

  return (
    <Card
      className={`bg-gradient-to-br ${statusConfig.bgGradient} border-0 shadow-md hover:shadow-xl transition-all duration-300 hover:scale-[1.02] group h-fit rounded-xl`}
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
          <Badge className={`${statusConfig.color} font-medium px-3 py-1.5 text-xs flex-shrink-0 ml-2 rounded-full shadow-sm`}>
            <span className="mr-1">{statusConfig.icon}</span>
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
        <div className="grid grid-cols-2 gap-2 text-xs text-gray-600 mb-3">
          <div className="flex items-center gap-1 truncate">
            <Calendar className="w-3 h-3 flex-shrink-0" />
            <span className="truncate">{scheduledDate ? scheduledDate.date : createdDate.date}</span>
          </div>
          <div className="flex items-center gap-1 truncate">
            <Clock className="w-3 h-3 flex-shrink-0" />
            <span className="truncate">{scheduledDate ? scheduledDate.time : createdDate.time}</span>
          </div>
        </div>

        {/* Actions Section */}
        <div className="flex items-center justify-between gap-2">
          {/* Left side - Edit/Delete for scheduled calls */}
          <div className="flex items-center gap-1">
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
          </div>

          {/* Center/Right side - Transcript and Reschedule */}
          <div className="flex items-center gap-2">
            {/* Transcript Button - only show for completed calls with available transcripts */}
            {transcriptAvailable && (
              <Collapsible open={isExpanded} onOpenChange={handleToggleExpanded}>
                <CollapsibleTrigger asChild>
                  <Button
                    variant="ghost"
                    size="sm"
                    className="text-blue-600 hover:text-blue-700 hover:bg-blue-50 transition-all duration-300 h-8 px-3 text-xs rounded-lg hover:scale-105"
                  >
                    {isExpanded ? <ChevronDown className="w-3 h-3" /> : <ChevronRight className="w-3 h-3" />}
                    <MessageCircle className="w-3 h-3 ml-1" />
                    <span className="ml-1 hidden sm:inline">Transcript</span>
                  </Button>
                </CollapsibleTrigger>
              </Collapsible>
            )}

            {/* Reschedule button for completed calls */}
            {call.status === "COMPLETED" && (
              <Button
                variant="ghost"
                size="sm"
                className="text-purple-600 hover:text-purple-700 hover:bg-purple-50 transition-all duration-300 h-8 px-3 text-xs rounded-lg hover:scale-105"
                onClick={() => setActiveSection("make-call")}
              >
                <RotateCcw className="w-3 h-3 mr-1" />
                <span className="hidden sm:inline">Reschedule</span>
              </Button>
            )}
          </div>
        </div>

        {/* Transcript Section */}
        {transcriptAvailable && (
          <Collapsible open={isExpanded} onOpenChange={handleToggleExpanded}>
            <CollapsibleContent className="mt-3 animate-in slide-in-from-top-2 duration-300">
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
            </CollapsibleContent>
          </Collapsible>
        )}
      </CardContent>
    </Card>
  )
}
