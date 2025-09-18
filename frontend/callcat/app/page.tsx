"use client"

import CallCatDashboard from "@/components/Dashboard"
import LandingPage from "@/components/LandingPage"
import { useAuth } from "@/contexts/AuthContext"

export default function Home() {
  const { isAuthenticated, isLoading } = useAuth()

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-lg">Loading...</div>
      </div>
    )
  }

  if (isAuthenticated) {
    return <CallCatDashboard />
  }

  return <LandingPage />
}
