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
} from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from "@/components/ui/collapsible"
import { CallRecord } from "@/types"
import { getStatusConfig, formatTimestamp } from "@/data/calls"

interface CallCardProps {
  call: CallRecord
  expandedCall: string | null
  setExpandedCall: (id: string | null) => void
  setActiveSection: (section: string) => void
}

export default function CallCard({ call, expandedCall, setExpandedCall, setActiveSection }: CallCardProps) {
  const statusConfig = getStatusConfig(call.status)
  const isExpanded = expandedCall === call.id
  const { date, time } = formatTimestamp(call.timestamp)
  const CallIcon = call.icon

  return (
    <Card
      className={`bg-gradient-to-br ${statusConfig.bgGradient} border-2 hover:shadow-lg transition-all duration-300 hover:scale-[1.02] group h-fit`}
    >
      <CardContent className="p-4">
        {/* Header Section */}
        <div className="flex items-start justify-between mb-3">
          <div className="flex items-center gap-3 min-w-0 flex-1">
            <div className="w-10 h-10 bg-white/60 rounded-lg flex items-center justify-center group-hover:scale-110 transition-transform duration-200 flex-shrink-0">
              <CallIcon className="w-5 h-5 text-gray-600" />
            </div>
            <div className="min-w-0 flex-1">
              <h3 className="font-semibold text-gray-800 text-base truncate">{call.business}</h3>
              <p className="text-xs text-gray-600 truncate">{call.id}</p>
            </div>
          </div>
          <Badge className={`${statusConfig.color} font-medium px-2 py-1 text-xs flex-shrink-0 ml-2`}>
            <span className="mr-1">{statusConfig.icon}</span>
            {statusConfig.label}
          </Badge>
        </div>

        {/* Purpose Section */}
        <div className="mb-3">
          <p className="text-gray-700 text-sm leading-relaxed line-clamp-2 mb-2">{call.purpose}</p>
          <div className="bg-white/60 rounded-lg p-2">
            <p className="text-xs text-gray-800 font-medium line-clamp-2">{call.result}</p>
          </div>
        </div>

        {/* Metadata Section */}
        <div className="grid grid-cols-2 gap-2 text-xs text-gray-600 mb-3">
          <div className="flex items-center gap-1 truncate">
            <Calendar className="w-3 h-3 flex-shrink-0" />
            <span className="truncate">{date}</span>
          </div>
          <div className="flex items-center gap-1 truncate">
            <Clock className="w-3 h-3 flex-shrink-0" />
            <span className="truncate">{time}</span>
          </div>
          <div className="flex items-center gap-1 truncate">
            <Timer className="w-3 h-3 flex-shrink-0" />
            <span className="truncate">{call.duration || "00:00"}</span>
          </div>
          <div className="flex items-center gap-1 truncate">
            <Globe className="w-3 h-3 flex-shrink-0" />
            <span className="truncate">{call.language.toUpperCase()}</span>
          </div>
        </div>

        {/* Actions Section */}
        <div className="flex items-center justify-between gap-2">
          <div className="flex items-center gap-1">
            <Collapsible open={isExpanded} onOpenChange={() => setExpandedCall(isExpanded ? null : call.id)}>
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
            {(call.status === "successful" || call.status === "failed" || call.status === "needs_attention") && (
              <Button
                variant="ghost"
                size="sm"
                className="text-purple-600 hover:text-purple-700 hover:bg-purple-50 transition-all duration-200 h-7 px-2 text-xs"
                onClick={() => {
                  setActiveSection("make-call")
                  console.log(`Setting up a new call to ${call.business}`)
                }}
              >
                <RotateCcw className="w-3 h-3 mr-1" />
                <span className="hidden sm:inline">Reschedule</span>
              </Button>
            )}
          </div>
        </div>

        {/* Transcript Section */}
        <Collapsible open={isExpanded} onOpenChange={() => setExpandedCall(isExpanded ? null : call.id)}>
          <CollapsibleContent className="mt-3 animate-in slide-in-from-top-2 duration-300">
            <div className="bg-white/80 rounded-xl p-3 border border-white/50 backdrop-blur-sm">
              <div className="flex items-center gap-2 mb-2">
                <MessageCircle className="w-4 h-4 text-blue-500" />
                <span className="font-medium text-gray-800 text-sm">Call Transcript</span>
              </div>
              <div className="space-y-2 max-h-48 overflow-y-auto">
                {call.transcript.map((line, index) => (
                  <div key={index} className="flex gap-2">
                    <div
                      className={`w-2 h-2 rounded-full mt-1.5 flex-shrink-0 ${
                        line.speaker === "ai" ? "bg-blue-400" : "bg-green-400"
                      }`}
                    />
                    <div className="flex-1">
                      <span className="text-xs font-medium text-gray-600 capitalize">
                        {line.speaker === "ai" ? "AI Assistant" : "Business"}
                      </span>
                      <p className="text-sm text-gray-800 leading-relaxed">{line.text}</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </CollapsibleContent>
        </Collapsible>
      </CardContent>
    </Card>
  )
}
