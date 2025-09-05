"use client"

import { useState, useEffect } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Phone, Clock, CheckCircle, AlertCircle, Calendar, Globe } from "lucide-react"
import { CallRequest, UserPreferencesResponse } from "@/types"
import apiService from "@/lib/api"
import { getUserTimezone, getTimezoneDisplayName, convertLocalToUTC, convertUTCToLocal, isDateTimeInFuture, getMinimumDateTime, generateTimezoneOptions, getTimezoneAwareDefaultTime } from "@/utils/timezone"

// Generate comprehensive timezone options
const TIMEZONE_OPTIONS = generateTimezoneOptions()

interface MakeCallSectionProps {
  onCallCreated?: () => void
}

export default function MakeCallSection({ onCallCreated }: MakeCallSectionProps) {
  const [formData, setFormData] = useState<CallRequest>({
    calleeName: "",
    phoneNumber: "",
    subject: "",
    prompt: ""
  })

  const [selectedTimezone, setSelectedTimezone] = useState(getUserTimezone())
  const [, setUserPreferences] = useState<UserPreferencesResponse | null>(null)
  const [isLoadingPreferences, setIsLoadingPreferences] = useState(true)
  
  // Initialize with timezone-aware default date/time
  const [dateValue, setDateValue] = useState("")
  const [timeValue, setTimeValue] = useState("")

  const [isSubmitting, setIsSubmitting] = useState(false)
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [success, setSuccess] = useState(false)
  
  // Get minimum date/time for validation based on selected timezone
  const { minDate, minTime } = getMinimumDateTime(selectedTimezone)

  const handleInputChange = (field: keyof CallRequest, value: string | number) => {
    setFormData(prev => ({ ...prev, [field]: value }))
    // Clear error when user starts typing
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: "" }))
    }
  }

  // Load user preferences and initialize default time on mount
  useEffect(() => {
    const loadUserPreferences = async () => {
      let finalTimezone = getUserTimezone()
      
      try {
        const preferences = await apiService.getUserPreferences()
        setUserPreferences(preferences)
        if (preferences.timezone) {
          finalTimezone = preferences.timezone
          setSelectedTimezone(preferences.timezone)
        }
      } catch (error) {
        console.warn('Failed to load user preferences:', error)
        // Fallback to browser timezone
        setSelectedTimezone(getUserTimezone())
      } finally {
        // Set initial default time based on final timezone
        const defaultTime = getTimezoneAwareDefaultTime(finalTimezone)
        setDateValue(defaultTime.dateValue)
        setTimeValue(defaultTime.timeValue)
        setIsLoadingPreferences(false)
      }
    }

    loadUserPreferences()
  }, [])

  // Handle timezone change with intelligent time adjustment
  const handleTimezoneChange = async (newTimezone: string) => {
    const oldTimezone = selectedTimezone
    
    try {
      // Update selected timezone immediately for UI responsiveness
      setSelectedTimezone(newTimezone)
      
      // Convert current date/time to new timezone OR set new default if time would be in past
      if (dateValue && timeValue) {
        // First convert current local time to UTC
        const utcTimestamp = convertLocalToUTC(dateValue, timeValue, oldTimezone)
        // Then convert UTC to new timezone
        const newLocal = convertUTCToLocal(utcTimestamp, newTimezone)
        
        // Check if the converted time is still in the future
        if (isDateTimeInFuture(newLocal.dateValue, newLocal.timeValue, newTimezone)) {
          setDateValue(newLocal.dateValue)
          setTimeValue(newLocal.timeValue)
        } else {
          // If converted time is in the past, set a new default time
          const newDefault = getTimezoneAwareDefaultTime(newTimezone)
          setDateValue(newDefault.dateValue)
          setTimeValue(newDefault.timeValue)
        }
      } else {
        // If no time set, use default for new timezone
        const newDefault = getTimezoneAwareDefaultTime(newTimezone)
        setDateValue(newDefault.dateValue)
        setTimeValue(newDefault.timeValue)
      }
      
      // Update user preferences
      await apiService.updateUserPreferences({ timezone: newTimezone })
    } catch (error) {
      console.error('Failed to update timezone:', error)
      // Revert on error
      setSelectedTimezone(oldTimezone)
    }
  }

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {}

    if (!formData.calleeName.trim()) {
      newErrors.calleeName = "Callee name is required"
    }

    if (!formData.phoneNumber.trim()) {
      newErrors.phoneNumber = "Phone number is required"
    } else if (!/^\+1[0-9]{10}$/.test(formData.phoneNumber)) {
      newErrors.phoneNumber = "Phone number must be in E.164 format (+1XXXXXXXXXX)"
    }

    if (!formData.subject.trim()) {
      newErrors.subject = "Subject is required"
    }

    if (!formData.prompt.trim()) {
      newErrors.prompt = "Prompt is required"
    }

    // Validate date and time (always required since we always show the picker)
    if (!dateValue.trim()) {
      newErrors.date = "Date is required"
    } else if (!timeValue.trim()) {
      newErrors.time = "Time is required"
    } else if (!isDateTimeInFuture(dateValue, timeValue, selectedTimezone)) {
      newErrors.datetime = "Scheduled time must be in the future"
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!validateForm()) {
      return
    }

    setIsSubmitting(true)
    setErrors({})

    try {
      // Always schedule for the selected date/time
      const callData = { ...formData }
      
      if (dateValue && timeValue) {
        callData.scheduledFor = convertLocalToUTC(dateValue, timeValue, selectedTimezone)
      }
      
      await apiService.createCall(callData)
      setSuccess(true)
      setFormData({
        calleeName: "",
        phoneNumber: "",
        subject: "",
        prompt: ""
      })
      
      // Reset to timezone-aware default date/time
      const newDefaultDateTime = getTimezoneAwareDefaultTime(selectedTimezone)
      setDateValue(newDefaultDateTime.dateValue)
      setTimeValue(newDefaultDateTime.timeValue)
      
      // Call callback if provided
      if (onCallCreated) {
        onCallCreated()
      }
      
      // Reset success message after 3 seconds
      setTimeout(() => setSuccess(false), 3000)
    } catch (error) {
      console.error("Failed to create call:", error)
      setErrors({ submit: error instanceof Error ? error.message : "Failed to create call" })
    } finally {
      setIsSubmitting(false)
    }
  }


  return (
    <div className="space-y-6 p-4 lg:p-6">
      <div className="text-center mb-8">
        <h1 className="text-3xl lg:text-4xl font-bold bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent mb-3">
          Make a Call
        </h1>
        <p className="text-gray-600 text-lg">
          Create a new AI-powered phone call - call now or schedule for later
        </p>
      </div>

      {success && (
        <Card className="border-green-200 bg-green-50">
          <CardContent className="p-4">
            <div className="flex items-center gap-2 text-green-700">
              <CheckCircle className="h-5 w-5" />
              <span className="font-medium">
                Call scheduled successfully!
              </span>
            </div>
          </CardContent>
        </Card>
      )}

      {errors.submit && (
        <Card className="border-red-200 bg-red-50">
          <CardContent className="p-4">
            <div className="flex items-center gap-2 text-red-700">
              <AlertCircle className="h-5 w-5" />
              <span className="font-medium">{errors.submit}</span>
            </div>
          </CardContent>
        </Card>
      )}

      <Card className="max-w-4xl mx-auto">
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-purple-700">
            <Phone className="h-5 w-5" />
            Call Details
          </CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Basic Information */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="space-y-2">
                <Label htmlFor="calleeName" className="text-sm font-medium text-gray-700">
                  Callee Name *
                </Label>
                <Input
                  id="calleeName"
                  value={formData.calleeName}
                  onChange={(e) => handleInputChange("calleeName", e.target.value)}
                  placeholder="Enter the person's name"
                  className={errors.calleeName ? "border-red-500" : ""}
                />
                {errors.calleeName && (
                  <p className="text-sm text-red-600">{errors.calleeName}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="phoneNumber" className="text-sm font-medium text-gray-700">
                  Phone Number *
                </Label>
                <Input
                  id="phoneNumber"
                  value={formData.phoneNumber}
                  onChange={(e) => handleInputChange("phoneNumber", e.target.value)}
                  placeholder="+1XXXXXXXXXX"
                  className={errors.phoneNumber ? "border-red-500" : ""}
                />
                {errors.phoneNumber && (
                  <p className="text-sm text-red-600">{errors.phoneNumber}</p>
                )}
              </div>
            </div>

            {/* Subject */}
            <div className="space-y-2">
              <Label htmlFor="subject" className="text-sm font-medium text-gray-700">
                Subject *
              </Label>
              <Input
                id="subject"
                value={formData.subject}
                onChange={(e) => handleInputChange("subject", e.target.value)}
                placeholder="Brief description of the call purpose"
                className={errors.subject ? "border-red-500" : ""}
              />
              {errors.subject && (
                <p className="text-sm text-red-600">{errors.subject}</p>
              )}
            </div>

            {/* AI Prompt */}
            <div className="space-y-2">
              <Label htmlFor="prompt" className="text-sm font-medium text-gray-700">
                AI Instructions *
              </Label>
              <Textarea
                id="prompt"
                value={formData.prompt}
                onChange={(e) => handleInputChange("prompt", e.target.value)}
                placeholder="Detailed instructions for the AI agent. What should they say? What questions should they ask? What information should they collect?"
                rows={6}
                className={errors.prompt ? "border-red-500" : ""}
              />
              {errors.prompt && (
                <p className="text-sm text-red-600">{errors.prompt}</p>
              )}
              <p className="text-xs text-gray-500">
                Be specific about the call objectives, tone, and any key points to cover.
              </p>
            </div>

            {/* Scheduling Section */}
            <div className="space-y-4 pt-4 border-t">
              <div className="space-y-4 p-4 border rounded-lg bg-gray-50">
                  {/* Timezone Selector */}
                  <div className="space-y-2">
                    <Label className="text-sm font-medium text-gray-700 flex items-center gap-2">
                      <Globe className="h-4 w-4" />
                      Timezone
                    </Label>
                    {isLoadingPreferences ? (
                      <div className="h-10 bg-gray-200 rounded-md animate-pulse" />
                    ) : (
                      <Select value={selectedTimezone} onValueChange={handleTimezoneChange}>
                        <SelectTrigger>
                          <SelectValue />
                        </SelectTrigger>
                        <SelectContent className="max-h-60 overflow-y-auto">
                          {TIMEZONE_OPTIONS.map(option => (
                            <SelectItem key={option.value} value={option.value}>
                              {option.label}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    )}
                    <p className="text-xs text-gray-500">
                      Schedule times shown in: {getTimezoneDisplayName(selectedTimezone)}
                    </p>
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label htmlFor="date" className="text-sm font-medium text-gray-700">
                        Date *
                      </Label>
                      <div className="relative">
                        <Calendar className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
                        <Input
                          id="date"
                          type="date"
                          value={dateValue}
                          onChange={(e) => {
                            setDateValue(e.target.value)
                            if (errors.date || errors.datetime) {
                              setErrors(prev => ({ ...prev, date: "", datetime: "" }))
                            }
                          }}
                          min={minDate}
                          className={`pl-10 ${errors.date || errors.datetime ? "border-red-500" : ""}`}
                        />
                      </div>
                      {errors.date && (
                        <p className="text-sm text-red-600">{errors.date}</p>
                      )}
                    </div>

                    <div className="space-y-2">
                      <Label htmlFor="time" className="text-sm font-medium text-gray-700">
                        Time *
                      </Label>
                      <div className="relative">
                        <Clock className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
                        <Input
                          id="time"
                          type="time"
                          value={timeValue}
                          onChange={(e) => {
                            setTimeValue(e.target.value)
                            if (errors.time || errors.datetime) {
                              setErrors(prev => ({ ...prev, time: "", datetime: "" }))
                            }
                          }}
                          min={dateValue === minDate ? minTime : undefined}
                          className={`pl-10 ${errors.time || errors.datetime ? "border-red-500" : ""}`}
                        />
                      </div>
                      {errors.time && (
                        <p className="text-sm text-red-600">{errors.time}</p>
                      )}
                    </div>
                  </div>

                  {errors.datetime && (
                    <p className="text-sm text-red-600">{errors.datetime}</p>
                  )}

                  {dateValue && timeValue && (
                    <div className="p-3 bg-blue-50 border border-blue-200 rounded-md">
                      <p className="text-sm text-blue-700">
                        <Clock className="inline h-4 w-4 mr-1" />
                        Call will be scheduled for: {new Date(`${dateValue}T${timeValue}`).toLocaleDateString('en-US', {
                          timeZone: selectedTimezone,
                          weekday: 'long',
                          year: 'numeric',
                          month: 'long',
                          day: 'numeric',
                          hour: '2-digit',
                          minute: '2-digit',
                          timeZoneName: 'short'
                        })}
                      </p>
                    </div>
                  )}
                </div>
            </div>

            {/* Submit Button */}
            <div className="flex justify-end pt-4">
              <Button
                type="submit"
                disabled={isSubmitting}
                className="bg-gradient-to-r from-purple-600 to-pink-600 hover:from-purple-700 hover:to-pink-700 text-white px-8 py-3"
              >
                {isSubmitting ? (
                  <div className="flex items-center gap-2">
                    <Clock className="h-4 w-4 animate-spin" />
                    Scheduling...
                  </div>
                ) : (
                  <div className="flex items-center gap-2">
                    <Calendar className="h-4 w-4" />
                    Schedule Call
                  </div>
                )}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
