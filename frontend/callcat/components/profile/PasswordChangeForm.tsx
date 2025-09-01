"use client"

import { useState } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Shield, Lock, Eye, EyeOff } from "lucide-react"
import { apiService } from "@/lib/api"
import { validatePassword, validatePasswordMatch } from "@/schemas/auth"
import type { ChangePasswordFormData } from "@/schemas/auth"

export function PasswordChangeForm() {
  const [formData, setFormData] = useState<ChangePasswordFormData>({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  })
  const [showPasswords, setShowPasswords] = useState({
    current: false,
    new: false,
    confirm: false
  })
  const [isLoading, setIsLoading] = useState(false)
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [successMessage, setSuccessMessage] = useState('')

  const handleFormChange = (field: keyof ChangePasswordFormData, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }))
    
    // Clear specific field error when user starts typing
    if (errors[field]) {
      setErrors(prev => {
        const newErrors = { ...prev }
        delete newErrors[field]
        return newErrors
      })
    }
  }

  const togglePasswordVisibility = (field: 'current' | 'new' | 'confirm') => {
    setShowPasswords(prev => ({ ...prev, [field]: !prev[field] }))
  }

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {}

    if (!formData.currentPassword) {
      newErrors.currentPassword = 'Current password is required'
    }

    const newPasswordError = validatePassword(formData.newPassword)
    if (newPasswordError) newErrors.newPassword = newPasswordError

    const passwordMatchError = validatePasswordMatch(formData.newPassword, formData.confirmPassword)
    if (passwordMatchError) newErrors.confirmPassword = passwordMatchError

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!validateForm()) return

    setIsLoading(true)
    try {
      await apiService.changePassword(formData.currentPassword, formData.newPassword)
      
      // Reset form
      setFormData({
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
      })
      
      setSuccessMessage('Password changed successfully!')
      setTimeout(() => setSuccessMessage(''), 5000)
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Failed to change password'
      setErrors({ general: errorMessage })
    } finally {
      setIsLoading(false)
    }
  }


  return (
    <Card className="rounded-xl shadow-sm border-0">
      <CardHeader className="pb-4">
        <div className="flex items-center gap-3">
          <Shield className="w-6 h-6 text-purple-600" />
          <CardTitle className="text-xl font-semibold text-gray-800">
            Security Settings
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

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Current Password */}
          <div className="space-y-2">
            <Label htmlFor="currentPassword" className="text-sm font-medium text-gray-700">
              Current Password
            </Label>
            <div className="relative">
              <Input
                id="currentPassword"
                type={showPasswords.current ? "text" : "password"}
                value={formData.currentPassword}
                onChange={(e) => handleFormChange('currentPassword', e.target.value)}
                className={errors.currentPassword ? 'border-red-300 pr-10' : 'pr-10'}
                placeholder="Enter your current password"
              />
              <button
                type="button"
                onClick={() => togglePasswordVisibility('current')}
                className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
              >
                {showPasswords.current ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
              </button>
            </div>
            {errors.currentPassword && (
              <p className="text-sm text-red-600">{errors.currentPassword}</p>
            )}
          </div>

          {/* New Password */}
          <div className="space-y-2">
            <Label htmlFor="newPassword" className="text-sm font-medium text-gray-700">
              New Password
            </Label>
            <div className="relative">
              <Input
                id="newPassword"
                type={showPasswords.new ? "text" : "password"}
                value={formData.newPassword}
                onChange={(e) => handleFormChange('newPassword', e.target.value)}
                className={errors.newPassword ? 'border-red-300 pr-10' : 'pr-10'}
                placeholder="Enter your new password"
              />
              <button
                type="button"
                onClick={() => togglePasswordVisibility('new')}
                className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
              >
                {showPasswords.new ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
              </button>
            </div>
            {errors.newPassword && (
              <p className="text-sm text-red-600">{errors.newPassword}</p>
            )}
            <div className="text-xs text-gray-500">
              Password must be at least 8 characters with uppercase, lowercase, and number
            </div>
          </div>

          {/* Confirm New Password */}
          <div className="space-y-2">
            <Label htmlFor="confirmPassword" className="text-sm font-medium text-gray-700">
              Confirm New Password
            </Label>
            <div className="relative">
              <Input
                id="confirmPassword"
                type={showPasswords.confirm ? "text" : "password"}
                value={formData.confirmPassword}
                onChange={(e) => handleFormChange('confirmPassword', e.target.value)}
                className={errors.confirmPassword ? 'border-red-300 pr-10' : 'pr-10'}
                placeholder="Confirm your new password"
              />
              <button
                type="button"
                onClick={() => togglePasswordVisibility('confirm')}
                className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
              >
                {showPasswords.confirm ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
              </button>
            </div>
            {errors.confirmPassword && (
              <p className="text-sm text-red-600">{errors.confirmPassword}</p>
            )}
          </div>

          <div className="flex items-center gap-3 pt-4">
            <Button
              type="submit"
              disabled={isLoading}
              className="bg-purple-600 hover:bg-purple-700"
            >
              <Lock className="w-4 h-4 mr-2" />
              {isLoading ? 'Changing Password...' : 'Change Password'}
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  )
}