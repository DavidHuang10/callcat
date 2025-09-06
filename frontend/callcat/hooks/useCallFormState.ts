"use client"

import { useState, useEffect } from "react"
import { CallRequest, UserPreferencesResponse, RescheduleData } from "@/types"
import apiService from "@/lib/api"
import { 
  getUserTimezone, 
  convertLocalToUTC, 
  isDateTimeInFuture, 
  getMinimumDateTime,
  getTimezoneAwareDefaultTime,
  convertUTCToLocal
} from "@/utils/timezone"

interface CallFormState {
  formData: CallRequest
  selectedTimezone: string
  dateValue: string
  timeValue: string
  isLoadingPreferences: boolean
  isSubmitting: boolean
  errors: Record<string, string>
  success: boolean
}

export function useCallFormState(onCallCreated?: () => void, rescheduleData?: RescheduleData | null, clearRescheduleData?: () => void) {
  const [state, setState] = useState<CallFormState>({
    formData: rescheduleData ? {
      calleeName: rescheduleData.calleeName,
      phoneNumber: rescheduleData.phoneNumber,
      subject: rescheduleData.subject,
      prompt: rescheduleData.prompt
    } : {
      calleeName: "",
      phoneNumber: "",
      subject: "",
      prompt: ""
    },
    selectedTimezone: getUserTimezone(),
    dateValue: "",
    timeValue: "",
    isLoadingPreferences: true,
    isSubmitting: false,
    errors: {},
    success: false
  })

  // Get minimum date/time for validation based on selected timezone
  const { minDate, minTime } = getMinimumDateTime(state.selectedTimezone)

  // Clear reschedule data after it has been used for initialization
  useEffect(() => {
    if (rescheduleData && clearRescheduleData) {
      clearRescheduleData()
    }
  }, [rescheduleData, clearRescheduleData])

  // Load user preferences and initialize default time on mount
  useEffect(() => {
    const loadUserPreferences = async () => {
      let finalTimezone = getUserTimezone()
      
      try {
        const preferences = await apiService.getUserPreferences()
        if (preferences.timezone) {
          finalTimezone = preferences.timezone
          setState(prev => ({ ...prev, selectedTimezone: preferences.timezone }))
        }
      } catch (error) {
        console.warn('Failed to load user preferences:', error)
        // Fallback to browser timezone
        setState(prev => ({ ...prev, selectedTimezone: getUserTimezone() }))
      } finally {
        // Set initial default time based on final timezone
        const defaultTime = getTimezoneAwareDefaultTime(finalTimezone)
        setState(prev => ({
          ...prev,
          dateValue: defaultTime.dateValue,
          timeValue: defaultTime.timeValue,
          isLoadingPreferences: false
        }))
      }
    }

    loadUserPreferences()
  }, [])

  const handleInputChange = (field: keyof CallRequest, value: string) => {
    setState(prev => ({
      ...prev,
      formData: { ...prev.formData, [field]: value },
      errors: { ...prev.errors, [field]: "" }
    }))
  }

  const handleDateChange = (date: string) => {
    setState(prev => ({ ...prev, dateValue: date }))
  }

  const handleTimeChange = (time: string) => {
    setState(prev => ({ ...prev, timeValue: time }))
  }

  const clearDateTimeError = () => {
    setState(prev => ({
      ...prev,
      errors: {
        ...prev.errors,
        date: "",
        time: "",
        datetime: ""
      }
    }))
  }

  // Handle timezone change with intelligent time adjustment
  const handleTimezoneChange = async (newTimezone: string) => {
    const oldTimezone = state.selectedTimezone
    
    try {
      // Update selected timezone immediately for UI responsiveness
      setState(prev => ({ ...prev, selectedTimezone: newTimezone }))
      
      // Convert current date/time to new timezone OR set new default if time would be in past
      if (state.dateValue && state.timeValue) {
        // First convert current local time to UTC
        const utcTimestamp = convertLocalToUTC(state.dateValue, state.timeValue, oldTimezone)
        // Then convert UTC to new timezone
        const newLocal = convertUTCToLocal(utcTimestamp, newTimezone)
        
        // Check if the converted time is still in the future
        if (isDateTimeInFuture(newLocal.dateValue, newLocal.timeValue, newTimezone)) {
          setState(prev => ({
            ...prev,
            dateValue: newLocal.dateValue,
            timeValue: newLocal.timeValue
          }))
        } else {
          // Time would be in past - set new default for this timezone
          const newDefault = getTimezoneAwareDefaultTime(newTimezone)
          setState(prev => ({
            ...prev,
            dateValue: newDefault.dateValue,
            timeValue: newDefault.timeValue
          }))
        }
      } else {
        // If no time set, use default for new timezone
        const newDefault = getTimezoneAwareDefaultTime(newTimezone)
        setState(prev => ({
          ...prev,
          dateValue: newDefault.dateValue,
          timeValue: newDefault.timeValue
        }))
      }
      
      // Save timezone preference to backend
      await apiService.updateUserPreferences({ timezone: newTimezone })
    } catch (error) {
      console.error('Failed to update timezone:', error)
      // Revert timezone change on error
      setState(prev => ({ ...prev, selectedTimezone: oldTimezone }))
    }
  }

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {}

    if (!state.formData.calleeName.trim()) {
      newErrors.calleeName = "Callee name is required"
    }

    if (!state.formData.phoneNumber.trim()) {
      newErrors.phoneNumber = "Phone number is required"
    } else if (!/^\+1\d{10}$/.test(state.formData.phoneNumber)) {
      newErrors.phoneNumber = "Phone number must be in format +1XXXXXXXXXX"
    }

    if (!state.formData.subject.trim()) {
      newErrors.subject = "Subject is required"
    }

    if (!state.formData.prompt.trim()) {
      newErrors.prompt = "AI instructions are required"
    }

    if (!state.dateValue) {
      newErrors.date = "Date is required"
    }

    if (!state.timeValue) {
      newErrors.time = "Time is required"
    }

    if (state.dateValue && state.timeValue && !isDateTimeInFuture(state.dateValue, state.timeValue, state.selectedTimezone)) {
      newErrors.datetime = "Scheduled time must be in the future"
    }

    setState(prev => ({ ...prev, errors: newErrors }))
    return Object.keys(newErrors).length === 0
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setState(prev => ({ ...prev, success: false }))

    if (!validateForm()) {
      return
    }

    setState(prev => ({ ...prev, isSubmitting: true }))

    try {
      const callData = { ...state.formData }

      if (state.dateValue && state.timeValue) {
        callData.scheduledFor = convertLocalToUTC(state.dateValue, state.timeValue, state.selectedTimezone)
      }

      await apiService.createCall(callData)
      
      setState(prev => ({ ...prev, success: true }))
      
      // Reset form after successful submission
      const newDefaultDateTime = getTimezoneAwareDefaultTime(state.selectedTimezone)
      setState(prev => ({
        ...prev,
        formData: {
          calleeName: "",
          phoneNumber: "",
          subject: "",
          prompt: ""
        },
        dateValue: newDefaultDateTime.dateValue,
        timeValue: newDefaultDateTime.timeValue,
        errors: {}
      }))

      if (onCallCreated) {
        onCallCreated()
      }
    } catch (error) {
      console.error("Failed to create call:", error)
      setState(prev => ({
        ...prev,
        errors: { submit: "Failed to create call. Please try again." }
      }))
    } finally {
      setState(prev => ({ ...prev, isSubmitting: false }))
    }
  }

  return {
    ...state,
    minDate,
    minTime,
    handleInputChange,
    handleDateChange,
    handleTimeChange,
    handleTimezoneChange,
    clearDateTimeError,
    handleSubmit
  }
}