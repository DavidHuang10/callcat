"use client"

import { useState } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Edit, Save, X, Mail } from "lucide-react"
import { useAuth } from "@/contexts/AuthContext"
import { apiService } from "@/lib/api"
import { validateName } from "@/schemas/auth"
import type { ProfileFormData } from "@/schemas/auth"
import type { UserResponse } from "@/types"

export function ProfileInfo() {
  const { user, checkAuth } = useAuth()
  const [isEditing, setIsEditing] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const [formData, setFormData] = useState<ProfileFormData>({
    firstName: '',
    lastName: ''
  })
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [successMessage, setSuccessMessage] = useState('')

  const handleEditClick = () => {
    if (user) {
      const [firstName, lastName] = user.fullName.split(' ', 2)
      setFormData({
        firstName: firstName || '',
        lastName: lastName || ''
      })
    }
    setIsEditing(true)
    setErrors({})
    setSuccessMessage('')
  }

  const handleCancelEdit = () => {
    setIsEditing(false)
    setFormData({ firstName: '', lastName: '' })
    setErrors({})
    setSuccessMessage('')
  }

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {}
    
    const firstNameError = validateName(formData.firstName, 'First name')
    if (firstNameError) newErrors.firstName = firstNameError
    
    const lastNameError = validateName(formData.lastName, 'Last name')
    if (lastNameError) newErrors.lastName = lastNameError

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleSave = async () => {
    if (!validateForm()) return

    setIsLoading(true)
    try {
      await apiService.updateProfile({
        firstName: formData.firstName,
        lastName: formData.lastName
      })
      
      await checkAuth()
      setIsEditing(false)
      setSuccessMessage('Profile updated successfully!')
      setTimeout(() => setSuccessMessage(''), 3000)
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Failed to update profile'
      setErrors({ general: errorMessage })
    } finally {
      setIsLoading(false)
    }
  }


  if (!user) {
    return (
      <Card className="rounded-xl shadow-sm">
        <CardContent className="p-6">
          <div className="text-center text-gray-500">Loading profile information...</div>
        </CardContent>
      </Card>
    )
  }

  const [firstName, lastName] = user.fullName.split(' ', 2)

  return (
    <Card className="rounded-xl shadow-sm border-0">
      <CardHeader className="pb-4">
        <div className="flex items-center justify-between">
          <CardTitle className="text-xl font-semibold text-gray-800">
            Profile Information
          </CardTitle>
          {!isEditing && (
            <Button
              onClick={handleEditClick}
              variant="outline"
              size="sm"
              className="flex items-center gap-2"
            >
              <Edit className="w-4 h-4" />
              Edit
            </Button>
          )}
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

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {isEditing ? (
            <>
              <div>
                <Label htmlFor="firstName" className="text-sm font-medium text-gray-700">
                  First Name
                </Label>
                <Input
                  id="firstName"
                  value={formData.firstName}
                  onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                  className={errors.firstName ? 'border-red-300' : ''}
                />
                {errors.firstName && (
                  <p className="text-sm text-red-600 mt-1">{errors.firstName}</p>
                )}
              </div>
              <div>
                <Label htmlFor="lastName" className="text-sm font-medium text-gray-700">
                  Last Name
                </Label>
                <Input
                  id="lastName"
                  value={formData.lastName}
                  onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                  className={errors.lastName ? 'border-red-300' : ''}
                />
                {errors.lastName && (
                  <p className="text-sm text-red-600 mt-1">{errors.lastName}</p>
                )}
              </div>
            </>
          ) : (
            <>
              <div>
                <Label className="text-sm font-medium text-gray-500">First Name</Label>
                <p className="text-gray-800 font-medium mt-1">{firstName || 'N/A'}</p>
              </div>
              <div>
                <Label className="text-sm font-medium text-gray-500">Last Name</Label>
                <p className="text-gray-800 font-medium mt-1">{lastName || 'N/A'}</p>
              </div>
            </>
          )}
        </div>

        <div className="flex items-center gap-3 p-4 bg-gray-50 rounded-lg">
          <Mail className="w-5 h-5 text-gray-500" />
          <div>
            <Label className="text-sm font-medium text-gray-500">Email Address</Label>
            <p className="text-gray-800 font-medium">{user.email}</p>
            <p className="text-xs text-gray-500 mt-1">Email cannot be changed</p>
          </div>
        </div>

        {isEditing && (
          <div className="flex items-center gap-3 pt-4">
            <Button
              onClick={handleSave}
              disabled={isLoading}
              className="bg-purple-600 hover:bg-purple-700"
            >
              <Save className="w-4 h-4 mr-2" />
              {isLoading ? 'Saving...' : 'Save Changes'}
            </Button>
            <Button
              onClick={handleCancelEdit}
              variant="outline"
              disabled={isLoading}
            >
              <X className="w-4 h-4 mr-2" />
              Cancel
            </Button>
          </div>
        )}
      </CardContent>
    </Card>
  )
}