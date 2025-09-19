"use client"

import { useState } from "react"
import {
  Phone,
  Clock,
  CheckCircle,
  Coffee,
  Plus,
  TrendingUp,
} from "lucide-react"

import { Card, CardContent } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import CallCard from "@/components/CallCard"
import DeleteConfirmationDialog from "@/components/DeleteConfirmationDialog"

import { useCallsForDashboard } from "@/hooks/useCallsForDashboard"
import { useDashboardStats } from "@/hooks/useDashboardStats"
import { usePagination } from "@/hooks/usePagination"
import { useAuth } from "@/contexts/AuthContext"

import { apiService } from "@/lib/api"
import { CallResponse } from "@/types"
import { GRID_LAYOUTS } from "@/constants/ui"

interface HomeSectionProps {
  searchQuery: string
  expandedTranscripts: Set<string>
  toggleExpandedTranscript: (id: string) => void
  setActiveSection: (section: string) => void
}

export default function HomeSection({ 
  searchQuery, 
  expandedTranscripts, 
  toggleExpandedTranscript, 
  setActiveSection
}: HomeSectionProps) {
  const { user } = useAuth()
  const {
    scheduledCalls,
    completedCalls,
    scheduledLoading,
    completedLoading,
    scheduledError,
    completedError,
    scheduledPage,
    completedPage,
    scheduledTotal,
    completedTotal,
    refreshAll,
    setScheduledPage,
    setCompletedPage,
  } = useCallsForDashboard()

  const {
    totalCallsThisMonth,
    successRate,
    totalTimeSaved,
    loading: statsLoading,
    error: statsError
  } = useDashboardStats()

  // Delete confirmation dialog state
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)
  const [callToDelete, setCallToDelete] = useState<CallResponse | null>(null)
  const [isDeleting, setIsDeleting] = useState(false)

  // Pagination simplified: no transition/fade state

  // Filter calls by search query if provided
  const allFilteredScheduledCalls = scheduledCalls.filter(call =>
    !searchQuery ||
    call.calleeName.toLowerCase().includes(searchQuery.toLowerCase()) ||
    call.subject.toLowerCase().includes(searchQuery.toLowerCase()) ||
    call.callId.toLowerCase().includes(searchQuery.toLowerCase())
  )

  const allFilteredCompletedCalls = completedCalls.filter(call =>
    !searchQuery ||
    call.calleeName.toLowerCase().includes(searchQuery.toLowerCase()) ||
    call.subject.toLowerCase().includes(searchQuery.toLowerCase()) ||
    call.callId.toLowerCase().includes(searchQuery.toLowerCase())
  )

  // Apply pagination using custom hook
  const scheduledPagination = usePagination({
    items: allFilteredScheduledCalls,
    currentPage: scheduledPage,
  })

  const completedPagination = usePagination({
    items: allFilteredCompletedCalls,
    currentPage: completedPage,
  })

  // No transition buffering of previous calls

  // Handle pagination (no fade/transition)
  const handleScheduledPageChange = (newPage: number) => {
    setScheduledPage(newPage)
  }

  const handleCompletedPageChange = (newPage: number) => {
    setCompletedPage(newPage)
  }

  // Handle opening delete confirmation dialog
  const handleDeleteClick = (call: CallResponse) => {
    setCallToDelete(call)
    setDeleteDialogOpen(true)
  }

  // Handle confirmed deletion
  const handleDeleteConfirm = async () => {
    if (!callToDelete) return
    
    setIsDeleting(true)
    try {
      await apiService.deleteCall(callToDelete.callId)
      refreshAll()
      setDeleteDialogOpen(false)
      setCallToDelete(null)
    } catch (error) {
      console.error('Failed to delete call:', error)
      // Error will be handled by the user - you might want to add toast notification here
    } finally {
      setIsDeleting(false)
    }
  }

  // Handle edit call - extract call data and navigate to edit form
  const handleEditCall = (callId: string) => {
    const call = scheduledCalls.find(c => c.callId === callId)
    if (call) {
      const editData = {
        originalCallId: call.callId,
        calleeName: call.calleeName,
        phoneNumber: call.phoneNumber,
        subject: call.subject,
        prompt: call.prompt,
        scheduledFor: call.scheduledFor,
        aiLanguage: call.aiLanguage || 'en'
      }
      localStorage.setItem('editData', JSON.stringify(editData))
      setActiveSection("make-call")
    }
  }

  // Generate dynamic stats based on real data (3 cards instead of 4)
  const stats = [
    {
      title: "Calls This Month",
      value: statsLoading ? "..." : totalCallsThisMonth.toString(),
      subtitle: "Total scheduled & completed",
      icon: Phone,
      color: "text-blue-600",
      bgColor: "bg-blue-50",
    },
    {
      title: "Success Rate",
      value: statsLoading ? "..." : `${successRate}%`,
      subtitle: "Successfully completed calls",
      icon: CheckCircle,
      color: "text-green-600",
      bgColor: "bg-green-50",
    },
    {
      title: "Time Saved",
      value: statsLoading ? "..." : totalTimeSaved,
      subtitle: "Total time from successful calls",
      icon: Clock,
      color: "text-purple-600",
      bgColor: "bg-purple-50",
    },
  ]

  return (
    <div className="space-y-6 p-4 lg:p-6">
      {/* Welcome Section */}
      <div className="text-center mb-8">
        <h1 className="text-3xl lg:text-4xl font-bold bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent mb-3">
          Welcome back{user?.fullName ? `, ${user.fullName.split(' ')[0]}` : ''}
        </h1>
        <p className="text-gray-600 text-lg">
          Here&apos;s what&apos;s happening with your calls today
        </p>
      </div>

      {/* Stats Grid */}
      <div className={GRID_LAYOUTS.STATS_GRID}>
        {stats.map((stat, index) => {
          const Icon = stat.icon
          return (
            <Card key={index} className="hover:shadow-lg transition-all duration-300">
              <CardContent className="p-4">
                <div className="flex items-center justify-between">
                  <div className="flex-1">
                    <p className="text-sm font-medium text-gray-600">{stat.title}</p>
                    <p className="text-2xl font-bold text-gray-900 mt-1">{stat.value}</p>
                    <p className="text-xs text-gray-500 mt-1">{stat.subtitle}</p>
                  </div>
                  <div className={`p-3 rounded-full ${stat.bgColor}`}>
                    <Icon className={`h-6 w-6 ${stat.color}`} />
                  </div>
                </div>
              </CardContent>
            </Card>
          )
        })}
      </div>
      
      {/* Stats Error Display */}
      {statsError && (
        <Card className="border-yellow-200 bg-yellow-50">
          <CardContent className="p-4">
            <div className="flex items-center gap-2 text-yellow-700">
              <TrendingUp className="h-5 w-5" />
              <span className="text-sm">Using cached statistics: {statsError}</span>
            </div>
          </CardContent>
        </Card>
      )}


      {/* Scheduled Calls Section */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-xl font-semibold text-gray-800">Scheduled Calls</h2>
          <div className="flex items-center gap-3">
            <Badge variant="secondary" className="text-xs">
              {scheduledTotal} total
            </Badge>
            {/* Scheduled Calls Pagination */}
            {scheduledPagination.showPagination && (
              <div className="flex items-center gap-2">
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => handleScheduledPageChange(scheduledPage - 1)}
                  disabled={!scheduledPagination.hasPrevPage || scheduledLoading}
                  className="h-8 w-8 p-0 hover:bg-gray-100"
                >
                  <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                  </svg>
                </Button>
                <span className="text-xs text-gray-500 min-w-[60px] text-center">
                  {scheduledPage + 1} of {scheduledPagination.totalPages}
                </span>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => handleScheduledPageChange(scheduledPage + 1)}
                  disabled={!scheduledPagination.hasNextPage || scheduledLoading}
                  className="h-8 w-8 p-0 hover:bg-gray-100"
                >
                  <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                  </svg>
                </Button>
              </div>
            )}
            <Button
              size="sm"
              onClick={() => setActiveSection("make-call")}
              className="bg-gradient-to-r from-purple-600 to-pink-600 hover:from-purple-700 hover:to-pink-700 text-white shadow-md hover:shadow-lg transition-all duration-300 rounded-lg px-4 py-2 text-xs font-medium"
            >
              <Plus className="w-3 h-3 mr-1" />
              Add Call
            </Button>
            {scheduledLoading && (
              <div className="text-xs text-gray-500">Loading...</div>
            )}
          </div>
        </div>

        {scheduledError && (
          <Card className="border-red-200 bg-red-50">
            <CardContent className="p-4">
              <div className="flex items-center gap-2 text-red-700">
                <span className="text-sm">Failed to load scheduled calls: {scheduledError}</span>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={refreshAll}
                  className="text-xs"
                >
                  Retry
                </Button>
              </div>
            </CardContent>
          </Card>
        )}

        {!scheduledError && (
            <>
              {(scheduledPagination.paginatedItems.length > 0) ? (
                <>
                  <div className={`${GRID_LAYOUTS.CALL_CARDS} transition-all duration-300`}>
                    {scheduledPagination.paginatedItems.map((call) => (
                      <CallCard
                        key={call.callId}
                        call={call}
                        expandedTranscripts={expandedTranscripts}
                        toggleExpandedTranscript={toggleExpandedTranscript}
                        setActiveSection={setActiveSection}
                        onEdit={handleEditCall}
                        onDeleteClick={handleDeleteClick}
                      />
                    ))}
                  </div>

                </>
              ) : (
                <Card className="text-center py-12">
                  <CardContent>
                    <Clock className="h-12 w-12 text-gray-400 mx-auto mb-4" />
                    <h3 className="text-lg font-medium text-gray-600 mb-2">No scheduled calls</h3>
                    <p className="text-gray-500 mb-4">Schedule your first call to get started!</p>
                    <Button
                      onClick={() => setActiveSection("make-call")}
                      className="bg-gradient-to-r from-purple-600 to-pink-600"
                    >
                      Schedule a Call
                    </Button>
                  </CardContent>
                </Card>
              )}
            </>
        )}
      </div>

      {/* Completed Calls Section */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-xl font-semibold text-gray-800">Recent Calls</h2>
          <div className="flex items-center gap-3">
            <Badge variant="secondary" className="text-xs">
              {completedTotal} total
            </Badge>
            {/* Completed Calls Pagination */}
            {completedPagination.showPagination && (
              <div className="flex items-center gap-2">
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => handleCompletedPageChange(completedPage - 1)}
                  disabled={!completedPagination.hasPrevPage || completedLoading}
                  className="h-8 w-8 p-0 hover:bg-gray-100"
                >
                  <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                  </svg>
                </Button>
                <span className="text-xs text-gray-500 min-w-[60px] text-center">
                  {completedPage + 1} of {completedPagination.totalPages}
                </span>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => handleCompletedPageChange(completedPage + 1)}
                  disabled={!completedPagination.hasNextPage || completedLoading}
                  className="h-8 w-8 p-0 hover:bg-gray-100"
                >
                  <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                  </svg>
                </Button>
              </div>
            )}
            {completedLoading && (
              <div className="text-xs text-gray-500">Loading...</div>
            )}
          </div>
        </div>

        {completedError && (
          <Card className="border-red-200 bg-red-50">
            <CardContent className="p-4">
              <div className="flex items-center gap-2 text-red-700">
                <span className="text-sm">Failed to load completed calls: {completedError}</span>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={refreshAll}
                  className="text-xs"
                >
                  Retry
                </Button>
              </div>
            </CardContent>
          </Card>
        )}

        {!completedError && (
            <>
              {(completedPagination.paginatedItems.length > 0) ? (
                <>
                  <div className={`${GRID_LAYOUTS.CALL_CARDS} transition-all duration-300`}>
                    {completedPagination.paginatedItems.map((call) => (
                      <CallCard
                        key={call.callId}
                        call={call}
                        expandedTranscripts={expandedTranscripts}
                        toggleExpandedTranscript={toggleExpandedTranscript}
                        setActiveSection={setActiveSection}
                        onEdit={handleEditCall}
                        onDeleteClick={handleDeleteClick}
                      />
                    ))}
                  </div>

                </>
              ) : (
                <Card className="text-center py-12">
                  <CardContent>
                    <Coffee className="h-12 w-12 text-gray-400 mx-auto mb-4" />
                    <h3 className="text-lg font-medium text-gray-600 mb-2">No calls yet</h3>
                    <p className="text-gray-500 mb-4">Start by making your first call!</p>
                    <Button
                      onClick={() => setActiveSection("make-call")}
                      className="bg-gradient-to-r from-purple-600 to-pink-600"
                    >
                      Make a Call
                    </Button>
                  </CardContent>
                </Card>
              )}
            </>
        )}
      </div>

      {/* Delete Confirmation Dialog */}
      <DeleteConfirmationDialog
        open={deleteDialogOpen}
        onOpenChange={setDeleteDialogOpen}
        call={callToDelete}
        onConfirm={handleDeleteConfirm}
        isDeleting={isDeleting}
      />
    </div>
  )
}
