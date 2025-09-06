"use client"

import { Label } from "@/components/ui/label"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import { CallRequest } from "@/types"

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
            value={formData.phoneNumber}
            onChange={(e) => onChange("phoneNumber", e.target.value)}
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
    </div>
  )
}