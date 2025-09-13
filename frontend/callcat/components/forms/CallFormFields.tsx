"use client"

import { useState } from "react"
import { Label } from "@/components/ui/label"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Volume2 } from "lucide-react"
import { CallRequest } from "@/types"
import { formatPhoneAsUserTypes, formatPhoneToE164 } from "@/utils/phone"
import { SUPPORTED_LANGUAGES, DEFAULT_LANGUAGE } from "@/constants"
import { SUPPORTED_VOICES, DEFAULT_VOICE } from "@/constants/voices"

interface CallFormFieldsProps {
  formData: CallRequest
  errors: Record<string, string>
  onChange: (field: keyof CallRequest, value: string) => void
}

export default function CallFormFields({
  formData,
  errors,
  onChange
}: CallFormFieldsProps) {
  const [displayPhone, setDisplayPhone] = useState(formData.phoneNumber)

  const handlePhoneChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const inputValue = e.target.value
    const previousValue = displayPhone
    
    // Format for display as user types
    const formatted = formatPhoneAsUserTypes(inputValue, previousValue)
    setDisplayPhone(formatted)
    
    // Convert to E.164 format for storage
    const e164 = formatPhoneToE164(inputValue)
    onChange("phoneNumber", e164)
  }

  return (
    <div className="space-y-6">
      {/* Basic Information */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="space-y-2">
          <Label htmlFor="calleeName" className="text-sm font-medium text-gray-700">
            Callee Name *
          </Label>
          <Input
            id="calleeName"
            value={formData.calleeName}
            onChange={(e) => onChange("calleeName", e.target.value)}
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
            type="tel"
            value={displayPhone}
            onChange={handlePhoneChange}
            placeholder="(555) 123-4567"
            className={errors.phoneNumber ? "border-red-500" : ""}
          />
          {errors.phoneNumber && (
            <p className="text-sm text-red-600">{errors.phoneNumber}</p>
          )}
          <p className="text-xs text-gray-500">
            Enter any US phone number format
          </p>
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
          onChange={(e) => onChange("subject", e.target.value)}
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
          onChange={(e) => onChange("prompt", e.target.value)}
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

      {/* AI Language and Voice */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="space-y-2">
          <Label htmlFor="aiLanguage" className="text-sm font-medium text-gray-700">
            AI Language *
          </Label>
          <Select 
            value={formData.aiLanguage || DEFAULT_LANGUAGE} 
            onValueChange={(value) => onChange("aiLanguage", value)}
          >
            <SelectTrigger className={errors.aiLanguage ? "border-red-500" : ""}>
              <SelectValue placeholder="Select language" />
            </SelectTrigger>
            <SelectContent>
              {SUPPORTED_LANGUAGES.map((language) => (
                <SelectItem key={language.code} value={language.code}>
                  {language.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          {errors.aiLanguage && (
            <p className="text-sm text-red-600">{errors.aiLanguage}</p>
          )}
          <p className="text-xs text-gray-500">
            Select the language the AI will speak during the call.
          </p>
        </div>

        <div className="space-y-2">
          <Label htmlFor="voiceId" className="text-sm font-medium text-gray-700 flex items-center gap-2">
            <Volume2 className="h-4 w-4" />
            AI Voice Tone
          </Label>
          <Select 
            value={formData.voiceId || DEFAULT_VOICE} 
            onValueChange={(value) => onChange("voiceId", value)}
          >
            <SelectTrigger className={errors.voiceId ? "border-red-500" : ""}>
              <SelectValue placeholder="Select voice tone" />
            </SelectTrigger>
            <SelectContent>
              {SUPPORTED_VOICES.map((voice) => (
                <SelectItem key={voice.value} value={voice.value}>
                  {voice.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          {errors.voiceId && (
            <p className="text-sm text-red-600">{errors.voiceId}</p>
          )}
          <p className="text-xs text-gray-500">
            Choose the tone and style for the AI voice.
          </p>
        </div>
      </div>
    </div>
  )
}