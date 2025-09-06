"use client"

import { Calendar, Clock } from "lucide-react"
import { Label } from "@/components/ui/label"
import { Input } from "@/components/ui/input"

interface CallSchedulingFormProps {
  dateValue: string
  timeValue: string
  onDateChange: (date: string) => void
  onTimeChange: (time: string) => void
  selectedTimezone: string
  minDate: string
  minTime?: string
  errors: {
    date?: string
    time?: string
    datetime?: string
  }
  onClearDateTimeError: () => void
}

export default function CallSchedulingForm({
  dateValue,
  timeValue,
  onDateChange,
  onTimeChange,
  selectedTimezone,
  minDate,
  minTime,
  errors,
  onClearDateTimeError
}: CallSchedulingFormProps) {
  const handleDateChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    onDateChange(e.target.value)
    if (errors.date || errors.datetime) {
      onClearDateTimeError()
    }
  }

  const handleTimeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    onTimeChange(e.target.value)
    if (errors.time || errors.datetime) {
      onClearDateTimeError()
    }
  }

  return (
    <div className="space-y-4">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Date Input */}
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
              onChange={handleDateChange}
              min={minDate}
              className={`pl-10 ${errors.date || errors.datetime ? "border-red-500" : ""}`}
            />
          </div>
          {errors.date && (
            <p className="text-sm text-red-600">{errors.date}</p>
          )}
        </div>

        {/* Time Input */}
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
              onChange={handleTimeChange}
              min={dateValue === minDate ? minTime : undefined}
              className={`pl-10 ${errors.time || errors.datetime ? "border-red-500" : ""}`}
            />
          </div>
          {errors.time && (
            <p className="text-sm text-red-600">{errors.time}</p>
          )}
        </div>
      </div>

      {/* General datetime error */}
      {errors.datetime && (
        <p className="text-sm text-red-600">{errors.datetime}</p>
      )}

      {/* Schedule preview */}
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
  )
}