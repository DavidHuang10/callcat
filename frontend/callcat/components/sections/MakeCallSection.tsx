"use client"

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Phone, Clock, CheckCircle, AlertCircle, Calendar } from "lucide-react"
import CallFormFields from "@/components/forms/CallFormFields"
import CallSchedulingForm from "@/components/forms/CallSchedulingForm"
import TimezoneSelector from "@/components/forms/TimezoneSelector"
import { useCallFormState } from "@/hooks/useCallFormState"
import { RescheduleData } from "@/types"

interface MakeCallSectionProps {
  onCallCreated?: () => void
  rescheduleData?: RescheduleData | null
  clearRescheduleData?: () => void
}

export default function MakeCallSection({ onCallCreated, rescheduleData, clearRescheduleData }: MakeCallSectionProps) {
  const {
    formData,
    selectedTimezone,
    dateValue,
    timeValue,
    isLoadingPreferences,
    isSubmitting,
    errors,
    success,
    minDate,
    minTime,
    handleInputChange,
    handleDateChange,
    handleTimeChange,
    handleTimezoneChange,
    clearDateTimeError,
    handleSubmit
  } = useCallFormState(onCallCreated, rescheduleData, clearRescheduleData)

  return (
    <div className="space-y-6 p-4 lg:p-6">
      <div className="text-center mb-8">
        <h1 className="text-3xl lg:text-4xl font-bold bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent mb-3">
          {rescheduleData ? 'Reschedule Call' : 'Make a Call'}
        </h1>
        <p className="text-gray-600 text-lg">
          {rescheduleData 
            ? 'Update the details and set a new time for your call'
            : 'Create a new AI-powered phone call - call now or schedule for later'
          }
        </p>
      </div>

      {/* Success Message */}
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

      {/* Error Message */}
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
            {/* Basic Form Fields */}
            <CallFormFields
              formData={formData}
              errors={errors}
              onChange={handleInputChange}
            />

            {/* Scheduling Section */}
            <div className="space-y-4 pt-4 border-t">
              <div className="space-y-4 p-4 border rounded-lg bg-gray-50">
                {/* Timezone Selector */}
                {isLoadingPreferences ? (
                  <div className="space-y-2">
                    <div className="h-5 bg-gray-200 rounded animate-pulse w-20" />
                    <div className="h-10 bg-gray-200 rounded animate-pulse" />
                  </div>
                ) : (
                  <TimezoneSelector
                    value={selectedTimezone}
                    onChange={handleTimezoneChange}
                  />
                )}

                {/* Date/Time Scheduling */}
                <CallSchedulingForm
                  dateValue={dateValue}
                  timeValue={timeValue}
                  onDateChange={handleDateChange}
                  onTimeChange={handleTimeChange}
                  selectedTimezone={selectedTimezone}
                  minDate={minDate}
                  minTime={minTime}
                  errors={errors}
                  onClearDateTimeError={clearDateTimeError}
                />
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
                    {rescheduleData ? 'Update Call' : 'Schedule Call'}
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