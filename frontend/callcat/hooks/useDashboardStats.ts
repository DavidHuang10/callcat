import { useState, useEffect } from 'react'
import { apiService } from '@/lib/api'
import { CallResponse } from '@/types'
import { extractCallDuration, formatDuration } from '@/utils/duration'

export interface DashboardStats {
  totalCallsPast30Days: number
  successRate: number
  totalTimeSaved: string
  loading: boolean
  error: string | null
}

export function useDashboardStats() {
  const [stats, setStats] = useState<DashboardStats>({
    totalCallsPast30Days: 0,
    successRate: 0,
    totalTimeSaved: '0m 0s',
    loading: true,
    error: null
  })

  useEffect(() => {
    const fetchStats = async () => {
      try {
        setStats(prev => ({ ...prev, loading: true, error: null }))

        // Get current date boundaries for past 30 days
        const now = new Date()
        const thirtyDaysAgo = new Date(now.getTime() - (30 * 24 * 60 * 60 * 1000))
        const thirtyDaysAgoTimestamp = thirtyDaysAgo.getTime()

        // Fetch both scheduled and completed calls
        const [scheduledResponse, completedResponse] = await Promise.all([
          apiService.getCalls('SCHEDULED', 100), // Increased limit to get more data
          apiService.getCalls('COMPLETED', 100)
        ])

        const scheduledCalls = scheduledResponse.calls || []
        const completedCalls = completedResponse.calls || []

        // Filter calls from past 30 days
        const past30DaysScheduled = scheduledCalls.filter(call =>
          call.createdAt >= thirtyDaysAgoTimestamp
        )
        const past30DaysCompleted = completedCalls.filter(call =>
          call.createdAt >= thirtyDaysAgoTimestamp
        )

        // Calculate total calls in past 30 days
        const totalCallsPast30Days = past30DaysScheduled.length + past30DaysCompleted.length

        // Calculate all-time success rate (completed calls with dialSuccessful: true)
        const successfulCalls = completedCalls.filter(call => call.dialSuccessful === true)
        const totalAttemptedCalls = completedCalls.length
        const successRate = totalAttemptedCalls > 0
          ? Math.round((successfulCalls.length / totalAttemptedCalls) * 100)
          : 0

        // Calculate all-time total time saved from successful calls
        const durations = completedCalls
          .filter(call => call.dialSuccessful === true)
          .map(call => {
            // Use scheduledFor as fallback for callAt (same logic as CallCard)
            const actualCallAt = call.callAt || call.scheduledFor
            return extractCallDuration(call.durationSec, call.retellCallData, actualCallAt, call.completedAt)
          })
          .filter(duration => duration > 0)

        const totalTimeSaved = durations.length > 0
          ? formatDuration(durations.reduce((sum, duration) => sum + duration, 0))
          : '0m 0s'

        setStats({
          totalCallsPast30Days,
          successRate,
          totalTimeSaved,
          loading: false,
          error: null
        })

      } catch (error) {
        console.error('Failed to fetch dashboard stats:', error)
        setStats(prev => ({
          ...prev,
          loading: false,
          error: 'Failed to load dashboard statistics'
        }))
      }
    }

    fetchStats()
  }, [])

  const refresh = () => {
    setStats(prev => ({ ...prev, loading: true, error: null }))
    // Re-run the effect by updating a dummy state if needed
    // For now, we can manually call the fetch logic
  }

  return {
    ...stats,
    refresh
  }
}