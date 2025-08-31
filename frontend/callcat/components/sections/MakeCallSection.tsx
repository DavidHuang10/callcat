"use client"

import { useState } from "react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Phone, Globe, Mic, Clock, CheckCircle, AlertCircle } from "lucide-react"
import { CallRequest } from "@/types"
import apiService from "@/lib/api"

interface MakeCallSectionProps {
  onCallCreated?: () => void
}

export default function MakeCallSection({ onCallCreated }: MakeCallSectionProps) {
  const [formData, setFormData] = useState<CallRequest>({
    calleeName: "",
    phoneNumber: "",
    subject: "",
    prompt: "",
    aiLanguage: "en",
    voiceId: ""
  })

  const [isSubmitting, setIsSubmitting] = useState(false)
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [success, setSuccess] = useState(false)

  const handleInputChange = (field: keyof CallRequest, value: string | number) => {
    setFormData(prev => ({ ...prev, [field]: value }))
    // Clear error when user starts typing
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: "" }))
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
      await apiService.createCall(formData)
      setSuccess(true)
      setFormData({
        calleeName: "",
        phoneNumber: "",
        subject: "",
        prompt: "",
        aiLanguage: "en",
        voiceId: ""
      })
      
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

  const languageOptions = [
    { value: "en", label: "English" },
    { value: "es", label: "Español" },
    { value: "fr", label: "Français" },
    { value: "de", label: "Deutsch" },
    { value: "ja", label: "日本語" },
    { value: "zh", label: "中文" }
  ]

  const voiceOptions = [
    { value: "voice_1", label: "Voice 1 (Default)" },
    { value: "voice_2", label: "Voice 2" },
    { value: "voice_3", label: "Voice 3" }
  ]

  return (
    <div className="space-y-6 p-4 lg:p-6">
      <div className="text-center mb-8">
        <h1 className="text-3xl lg:text-4xl font-bold bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent mb-3">
          Make a Call
        </h1>
        <p className="text-gray-600 text-lg">
          Schedule a new AI-powered phone call
        </p>
      </div>

      {success && (
        <Card className="border-green-200 bg-green-50">
          <CardContent className="p-4">
            <div className="flex items-center gap-2 text-green-700">
              <CheckCircle className="h-5 w-5" />
              <span className="font-medium">Call scheduled successfully!</span>
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

            {/* AI Configuration */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="space-y-2">
                <Label htmlFor="aiLanguage" className="text-sm font-medium text-gray-700">
                  AI Language
                </Label>
                <Select
                  value={formData.aiLanguage}
                  onValueChange={(value) => handleInputChange("aiLanguage", value)}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {languageOptions.map((lang) => (
                      <SelectItem key={lang.value} value={lang.value}>
                        <div className="flex items-center gap-2">
                          <Globe className="w-4 h-4" />
                          {lang.label}
                        </div>
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="voiceId" className="text-sm font-medium text-gray-700">
                  Voice Selection
                </Label>
                <Select
                  value={formData.voiceId}
                  onValueChange={(value) => handleInputChange("voiceId", value)}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Select a voice" />
                  </SelectTrigger>
                  <SelectContent>
                    {voiceOptions.map((voice) => (
                      <SelectItem key={voice.value} value={voice.value}>
                        <div className="flex items-center gap-2">
                          <Mic className="w-4 h-4" />
                          {voice.label}
                        </div>
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
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
                    <Phone className="h-4 w-4" />
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
