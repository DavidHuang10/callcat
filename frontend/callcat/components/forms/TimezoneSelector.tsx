"use client"

import { Globe } from "lucide-react"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { generateTimezoneOptions, getTimezoneDisplayName } from "@/utils/timezone"

// Generate comprehensive timezone options
const TIMEZONE_OPTIONS = generateTimezoneOptions()

interface TimezoneSelectorProps {
  value: string
  onChange: (timezone: string) => void
  disabled?: boolean
  className?: string
}

export default function TimezoneSelector({ 
  value, 
  onChange, 
  disabled = false,
  className = "" 
}: TimezoneSelectorProps) {
  return (
    <div className={className}>
      <Label htmlFor="timezone" className="text-sm font-medium text-gray-700 flex items-center gap-2">
        <Globe className="h-4 w-4" />
        Timezone
      </Label>
      <Select 
        value={value} 
        onValueChange={onChange}
        disabled={disabled}
      >
        <SelectTrigger id="timezone" className="w-full">
          <SelectValue />
        </SelectTrigger>
        <SelectContent className="max-h-60">
          {TIMEZONE_OPTIONS.map((tz) => (
            <SelectItem key={tz.value} value={tz.value}>
              {tz.label}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
      <p className="text-xs text-gray-500 mt-1">
        Schedule times shown in: {getTimezoneDisplayName(value)}
      </p>
    </div>
  )
}