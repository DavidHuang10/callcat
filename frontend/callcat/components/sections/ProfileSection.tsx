"use client"

import { ProfileInfo } from "@/components/profile/ProfileInfo"
import { PreferencesForm } from "@/components/profile/PreferencesForm"
import { PasswordChangeForm } from "@/components/profile/PasswordChangeForm"

export function ProfileSection() {
  return (
    <div className="max-w-4xl mx-auto space-y-8 p-4">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">
          Profile Settings
        </h1>
        <p className="text-gray-600">
          Manage your account information, preferences, and security settings.
        </p>
      </div>

      {/* Profile Information */}
      <ProfileInfo />

      {/* User Preferences */}
      <PreferencesForm />

      {/* Security Settings */}
      <PasswordChangeForm />
    </div>
  )
}