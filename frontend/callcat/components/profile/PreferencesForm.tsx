"use client"

import { useState, useEffect } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Save, Settings, Volume2, Globe, Bell } from "lucide-react"
import { apiService } from "@/lib/api"
import { validateSystemPrompt } from "@/schemas/auth"
import { generateTimezoneOptions } from "@/utils/timezone"
import type { PreferencesFormData } from "@/schemas/auth"

const TIMEZONE_OPTIONS = generateTimezoneOptions()

const LANGUAGE_OPTIONS = [
  { value: 'en', label: 'English' },
  { value: 'es', label: 'Spanish' },
  { value: 'fr', label: 'French' },
  { value: 'de', label: 'German' },
  { value: 'it', label: 'Italian' },
  { value: 'pt', label: 'Portuguese' },
  { value: 'ja', label: 'Japanese' },
  { value: 'zh', label: 'Chinese' },
]

const VOICE_OPTIONS = [
  { value: 'voice_id_123', label: 'Default Voice' },
  { value: 'voice_professional', label: 'Professional' },
  { value: 'voice_friendly', label: 'Friendly' },
  { value: 'voice_formal', label: 'Formal' },
]

export function PreferencesForm() {
  const [isLoading, setIsLoading] = useState(true)
  const [isSaving, setIsSaving] = useState(false)
  const [formData, setFormData] = useState<PreferencesFormData>({
    systemPrompt: '',
    timezone: 'UTC',
    emailNotifications: true,
    voiceId: 'voice_id_123',
    defaultLanguage: 'en'
  })
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [successMessage, setSuccessMessage] = useState('')

  useEffect(() => {
    loadPreferences()
  }, [])

  const loadPreferences = async () => {
    try {
      const userPreferences = await apiService.getUserPreferences()
      setFormData({
        systemPrompt: userPreferences.systemPrompt || '',
        timezone: userPreferences.timezone || 'UTC',
        emailNotifications: userPreferences.emailNotifications,
        voiceId: userPreferences.voiceId || 'voice_id_123',
        defaultLanguage: userPreferences.defaultLanguage || 'en'
      })
    } catch (error) {
      console.error('Failed to load preferences:', error)
      setErrors({ general: 'Failed to load preferences' })
    } finally {
      setIsLoading(false)
    }
  }

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {}
    
    const systemPromptError = validateSystemPrompt(formData.systemPrompt)
    if (systemPromptError) newErrors.systemPrompt = systemPromptError

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleSave = async () => {
    if (!validateForm()) return

    setIsSaving(true)
    try {
      await apiService.updateUserPreferences({
        systemPrompt: formData.systemPrompt,
        defaultLanguage: formData.defaultLanguage,
        defaultVoiceId: formData.voiceId,
        emailNotifications: formData.emailNotifications
      })
      
      setSuccessMessage('Preferences updated successfully!')
      setTimeout(() => setSuccessMessage(''), 3000)
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Failed to update preferences'
      setErrors({ general: errorMessage })
    } finally {
      setIsSaving(false)
    }
  }

  const handleFormChange = (field: keyof PreferencesFormData, value: string | boolean) => {
    setFormData(prev => ({ ...prev, [field]: value }))
    if (errors[field]) {
      setErrors(prev => {
        const newErrors = { ...prev }
        delete newErrors[field]
        return newErrors
      })
    }
  }

  if (isLoading) {
    return (
      <Card className="rounded-xl shadow-sm">
        <CardContent className="p-6">
          <div className="text-center text-gray-500">Loading preferences...</div>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card className="rounded-xl shadow-sm border-0">
      <CardHeader className="pb-4">
        <div className="flex items-center gap-3">
          <Settings className="w-6 h-6 text-purple-600" />
          <CardTitle className="text-xl font-semibold text-gray-800">
            Preferences
          </CardTitle>
        </div>
      </CardHeader>
      <CardContent className="space-y-6">
        {successMessage && (
          <div className="bg-green-50 border border-green-200 rounded-lg p-3">
            <p className="text-sm text-green-700">{successMessage}</p>
          </div>
        )}

        {errors.general && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-3">
            <p className="text-sm text-red-700">{errors.general}</p>
          </div>
        )}

        {/* AI System Prompt */}
        <div className="space-y-2">
          <Label htmlFor="systemPrompt" className="text-sm font-medium text-gray-700 flex items-center gap-2">
            <Settings className="w-4 h-4" />
            AI System Prompt
          </Label>
          <Textarea
            id="systemPrompt"
            value={formData.systemPrompt}
            onChange={(e) => handleFormChange('systemPrompt', e.target.value)}
            placeholder="Customize how the AI behaves during calls (optional)..."
            className={`min-h-[100px] resize-none ${errors.systemPrompt ? 'border-red-300' : ''}`}
            maxLength={1000}
          />
          <div className="flex justify-between text-xs text-gray-500">
            <span>
              {errors.systemPrompt && (
                <span className="text-red-600">{errors.systemPrompt}</span>
              )}
            </span>
            <span>{formData.systemPrompt.length}/1000</span>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* Default Language */}
          <div className="space-y-2">
            <Label className="text-sm font-medium text-gray-700 flex items-center gap-2">
              <Globe className="w-4 h-4" />
              Default Language
            </Label>
            <Select value={formData.defaultLanguage} onValueChange={(value) => handleFormChange('defaultLanguage', value)}>
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {LANGUAGE_OPTIONS.map(option => (
                  <SelectItem key={option.value} value={option.value}>
                    {option.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          {/* Voice Selection */}
          <div className="space-y-2">
            <Label className="text-sm font-medium text-gray-700 flex items-center gap-2">
              <Volume2 className="w-4 h-4" />
              AI Voice
            </Label>
            <Select value={formData.voiceId} onValueChange={(value) => handleFormChange('voiceId', value)}>
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {VOICE_OPTIONS.map(option => (
                  <SelectItem key={option.value} value={option.value}>
                    {option.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          {/* Timezone */}
          <div className="space-y-2">
            <Label className="text-sm font-medium text-gray-700">
              Timezone
            </Label>
            <Select value={formData.timezone} onValueChange={(value) => handleFormChange('timezone', value)}>
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {TIMEZONE_OPTIONS.map(option => (
                  <SelectItem key={option.value} value={option.value}>
                    {option.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          {/* Email Notifications */}
          <div className="space-y-2">
            <Label className="text-sm font-medium text-gray-700 flex items-center gap-2">
              <Bell className="w-4 h-4" />
              Email Notifications
            </Label>
            <Select 
              value={formData.emailNotifications ? "enabled" : "disabled"} 
              onValueChange={(value) => handleFormChange('emailNotifications', value === "enabled")}
            >
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="enabled">Enabled</SelectItem>
                <SelectItem value="disabled">Disabled</SelectItem>
              </SelectContent>
            </Select>
            <p className="text-xs text-gray-500">Receive email updates about your calls</p>
          </div>
        </div>

        <div className="pt-4">
          <Button
            onClick={handleSave}
            disabled={isSaving}
            className="bg-purple-600 hover:bg-purple-700"
          >
            <Save className="w-4 h-4 mr-2" />
            {isSaving ? 'Saving...' : 'Save Preferences'}
          </Button>
        </div>
      </CardContent>
    </Card>
  )
}