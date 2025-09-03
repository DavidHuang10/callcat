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
  expandedCall: string | null
  setExpandedCall: (id: string | null) => void
  setActiveSection: (section: string) => void
  onEdit?: (callId: string) => void
  onDelete?: (callId: string) => void
}

// Helper functions for formatting and status
const getStatusConfig = (status: string) => {
  const configs: Record<string, any> = {
    SCHEDULED: {
      label: "Scheduled",
      color: "bg-blue-100 text-blue-800 border-blue-200",
      bgGradient: "from-blue-50 to-cyan-50",
      icon: "⏰",
    },
    COMPLETED: {
      label: "Completed", 
      color: "bg-green-100 text-green-800 border-green-200",
      bgGradient: "from-green-50 to-emerald-50",
      icon: "✓",
    },
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
  expandedCall, 
  setExpandedCall, 
  setActiveSection,
  onEdit,
  onDelete 
}: CallCardProps) {
  const statusConfig = getStatusConfig(call.status)
  const isExpanded = expandedCall === call.callId
  const createdDate = formatTimestamp(call.createdAt)
  const scheduledDate = call.scheduledFor ? formatTimestamp(call.scheduledFor) : null
  
  // Use the call details hook for transcript data (only when expanded)
  const { transcript, loading: transcriptLoading } = useCallDetails({
    callId: isExpanded ? call.callId : null,
  })
  
  // Check if transcript is available for this call
  const transcriptAvailable = hasAvailableTranscript(call)

  const handleToggleExpanded = () => {
    setExpandedCall(isExpanded ? null : call.callId)
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
      className={`bg-gradient-to-br ${statusConfig.bgGradient} border-2 hover:shadow-lg transition-all duration-300 hover:scale-[1.02] group h-fit`}
    >
      <CardContent className="p-4">
        {/* Header Section */}
        <div className="flex items-start justify-between mb-3">
          <div className="flex items-center gap-3 min-w-0 flex-1">
            <div className="w-10 h-10 bg-white/60 rounded-lg flex items-center justify-center group-hover:scale-110 transition-transform duration-200 flex-shrink-0">
              <User className="w-5 h-5 text-gray-600" />
            </div>
            <div className="min-w-0 flex-1">
              <h3 className="font-semibold text-gray-800 text-base truncate">{call.calleeName}</h3>
              <p className="text-xs text-gray-600 truncate">{formatPhoneNumber(call.phoneNumber)}</p>
            </div>
          </div>
          <Badge className={`${statusConfig.color} font-medium px-2 py-1 text-xs flex-shrink-0 ml-2`}>
            <span className="mr-1">{statusConfig.icon}</span>
            {statusConfig.label}
          </Badge>
        </div>

        {/* Subject Section */}
        <div className="mb-3">
          <p className="text-gray-700 text-sm leading-relaxed line-clamp-2 mb-2">{call.subject}</p>
          <div className="bg-white/60 rounded-lg p-2">
            <p className="text-xs text-gray-800 font-medium line-clamp-2">{call.prompt}</p>
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
          <div className="flex items-center gap-1 truncate">
            <Phone className="w-3 h-3 flex-shrink-0" />
            <span className="truncate">{call.callerNumber ? formatPhoneNumber(call.callerNumber) : 'System'}</span>
          </div>
          <div className="flex items-center gap-1 truncate">
            <Globe className="w-3 h-3 flex-shrink-0" />
            <span className="truncate">{(call.aiLanguage || 'en').toUpperCase()}</span>
          </div>
        </div>

        {/* Actions Section */}
        <div className="flex items-center justify-between gap-2">
          <div className="flex items-center gap-1">
            {/* Transcript Button - only show for completed calls with available transcripts */}
            {transcriptAvailable && (
              <Collapsible open={isExpanded} onOpenChange={handleToggleExpanded}>
                <CollapsibleTrigger asChild>
                  <Button
                    variant="ghost"
                    size="sm"
                    className="text-blue-600 hover:text-blue-700 hover:bg-blue-50 transition-all duration-200 h-7 px-2 text-xs"
                  >
                    {isExpanded ? <ChevronDown className="w-3 h-3" /> : <ChevronRight className="w-3 h-3" />}
                    <MessageCircle className="w-3 h-3 ml-1" />
                    <span className="ml-1 hidden sm:inline">Transcript</span>
                  </Button>
                </CollapsibleTrigger>
              </Collapsible>
            )}

            {/* Edit/Delete buttons for scheduled calls */}
            {call.status === "SCHEDULED" && (
              <>
                <Button
                  variant="ghost"
                  size="sm"
                  className="text-purple-600 hover:text-purple-700 hover:bg-purple-50 transition-all duration-200 h-7 px-2 text-xs"
                  onClick={handleEdit}
                >
                  <Edit3 className="w-3 h-3 mr-1" />
                  <span className="hidden sm:inline">Edit</span>
                </Button>
                <Button
                  variant="ghost"
                  size="sm"
                  className="text-red-600 hover:text-red-700 hover:bg-red-50 transition-all duration-200 h-7 px-2 text-xs"
                  onClick={handleDelete}
                >
                  <Trash2 className="w-3 h-3 mr-1" />
                  <span className="hidden sm:inline">Delete</span>
                </Button>
              </>
            )}

            {/* Reschedule button for completed calls */}
            {call.status === "COMPLETED" && (
              <Button
                variant="ghost"
                size="sm"
                className="text-purple-600 hover:text-purple-700 hover:bg-purple-50 transition-all duration-200 h-7 px-2 text-xs"
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
              <div className="bg-white/80 rounded-xl p-4 border border-white/50 backdrop-blur-sm">
                <div className="flex items-center gap-2 mb-4">
                  <MessageCircle className="w-4 h-4 text-blue-500" />
                  <span className="font-medium text-gray-800 text-sm">Call Transcript</span>
                </div>
                
                {/* Large Scrollable Conversation Container */}
                <div className="min-h-[400px] max-h-[600px] overflow-y-auto space-y-3 pr-2" style={{ scrollbarWidth: 'thin' }}>
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
