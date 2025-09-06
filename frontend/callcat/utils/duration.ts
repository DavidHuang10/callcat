/**
 * Duration formatting utilities for call display
 */

/**
 * Format duration in seconds to human-readable format
 * @param seconds Duration in seconds
 * @returns Formatted duration string (e.g., "3m 45s", "1h 5m", "45s")
 */
export function formatDuration(seconds: number): string {
  if (seconds <= 0) return '0s'
  
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const remainingSeconds = seconds % 60
  
  const parts: string[] = []
  
  if (hours > 0) {
    parts.push(`${hours}h`)
  }
  
  if (minutes > 0) {
    parts.push(`${minutes}m`)
  }
  
  if (remainingSeconds > 0 || parts.length === 0) {
    parts.push(`${remainingSeconds}s`)
  }
  
  return parts.join(' ')
}

/**
 * Format duration in seconds to a more compact format for small spaces
 * @param seconds Duration in seconds
 * @returns Compact duration string (e.g., "3:45", "1:05:30")
 */
export function formatDurationCompact(seconds: number): string {
  if (seconds <= 0) return '0:00'
  
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const remainingSeconds = seconds % 60
  
  if (hours > 0) {
    return `${hours}:${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`
  }
  
  return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
}

/**
 * Calculate duration from start and end timestamps
 * @param startTimestamp Start time in milliseconds
 * @param endTimestamp End time in milliseconds
 * @returns Duration in seconds
 */
export function calculateDurationFromTimestamps(startTimestamp: number, endTimestamp: number): number {
  if (endTimestamp <= startTimestamp) return 0
  return Math.round((endTimestamp - startTimestamp) / 1000)
}

/**
 * Extract call duration from multiple sources
 * @param durationSec Duration in seconds from API
 * @param retellCallData Retell call data JSON string
 * @param callAt Call start timestamp (fallback)
 * @param completedAt Call end timestamp (fallback)
 * @returns Duration in seconds
 */
export function extractCallDuration(
  durationSec?: number | null,
  retellCallData?: string | null,
  callAt?: number | null,
  completedAt?: number | null
): number {
  // Use API duration if available
  if (durationSec && durationSec > 0) return durationSec
  
  // Extract from retell data if available
  if (retellCallData) {
    const retellData = JSON.parse(retellCallData)
    if (retellData.duration_ms) return Math.round(retellData.duration_ms / 1000)
  }
  
  // Fallback to timestamp calculation
  if (callAt && completedAt) {
    return calculateDurationFromTimestamps(callAt, completedAt)
  }
  
  return 0
}

/**
 * Get a human-readable description of call timing
 * @param callAt When call started (timestamp in milliseconds) - may be null
 * @param completedAt When call ended (timestamp in milliseconds)
 * @param durationSec Duration in seconds (if available)
 * @param retellCallData Optional retell call data JSON string to extract duration from
 * @param scheduledFor When call was scheduled to start (fallback for callAt)
 * @returns Object with formatted timing information
 */
export function formatCallTiming(
  callAt?: number | null,
  completedAt?: number | null,
  durationSec?: number | null,
  retellCallData?: string | null,
  scheduledFor?: number | null
) {
  // Use scheduledFor as fallback for callAt if callAt is not available
  const actualCallAt = callAt || scheduledFor
  
  // Extract duration first to see if we have any timing data
  const duration = extractCallDuration(durationSec, retellCallData, actualCallAt, completedAt)
  
  // If we have no duration and no timestamps, return no timing data
  if (duration === 0 && (!actualCallAt || !completedAt)) {
    return {
      startTime: null,
      endTime: null,
      duration: null,
      hasTimingData: false
    }
  }
  
  return {
    startTime: actualCallAt ? new Date(actualCallAt).toLocaleTimeString("en-US", { 
      hour: "numeric", 
      minute: "2-digit" 
    }) : null,
    endTime: completedAt ? new Date(completedAt).toLocaleTimeString("en-US", { 
      hour: "numeric", 
      minute: "2-digit" 
    }) : null,
    duration: duration > 0 ? formatDuration(duration) : null,
    durationSeconds: duration,
    hasTimingData: duration > 0 || (actualCallAt && completedAt)
  }
}