import { useState, useEffect } from 'react'
import { apiService } from '@/lib/api'
import { CallResponse } from '@/types'
import { extractCallDuration, formatDuration } from '@/utils/duration'

export interface DashboardStats {
  totalCallsThisMonth: number
  successRate: number
  avgCallDuration: string
  loading: boolean
  error: string | null
}

export function useDashboardStats() {
  const [stats, setStats] = useState<DashboardStats>({
    totalCallsThisMonth: 0,
    successRate: 0,
    avgCallDuration: '0m 0s',
    loading: true,
    error: null
  })

  useEffect(() => {
    const fetchStats = async () => {
      try {
        setStats(prev => ({ ...prev, loading: true, error: null }))

        // Get current date boundaries for this month
        const now = new Date()
        const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1)
        const startOfMonthTimestamp = startOfMonth.getTime()

        // Fetch both scheduled and completed calls (we'll filter by date)
        const [scheduledResponse, completedResponse] = await Promise.all([
          apiService.getCalls('SCHEDULED', 100), // Increased limit to get more data
          apiService.getCalls('COMPLETED', 100)
        ])

        const scheduledCalls = scheduledResponse.calls || []
        const completedCalls = completedResponse.calls || []

        // Filter calls from this month
        const thisMonthScheduled = scheduledCalls.filter(call => 
          call.createdAt >= startOfMonthTimestamp
        )
        const thisMonthCompleted = completedCalls.filter(call => 
          call.createdAt >= startOfMonthTimestamp
        )

        // Calculate total calls this month
        const totalCallsThisMonth = thisMonthScheduled.length + thisMonthCompleted.length

        // Calculate success rate (completed calls with dialSuccessful: true)
        const successfulCalls = thisMonthCompleted.filter(call => call.dialSuccessful === true)
        const totalAttemptedCalls = thisMonthCompleted.length
        const successRate = totalAttemptedCalls > 0 
          ? Math.round((successfulCalls.length / totalAttemptedCalls) * 100)
          : 0

        // Calculate average call duration from real data
        const durations = thisMonthCompleted
          .map(call => {
            // Use scheduledFor as fallback for callAt (same logic as CallCard)
            const actualCallAt = call.callAt || call.scheduledFor
            return extractCallDuration(call.durationSec, call.retellCallData, actualCallAt, call.completedAt)
          })
          .filter(duration => duration > 0)
        
        const avgCallDuration = durations.length > 0
          ? formatDuration(Math.round(durations.reduce((sum, duration) => sum + duration, 0) / durations.length))
          : '0m 0s'

        setStats({
          totalCallsThisMonth,
          successRate,
          avgCallDuration,
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