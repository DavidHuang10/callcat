import CallCatDashboard from "@/components/Dashboard"
import ProtectedRoute from "@/components/ProtectedRoute"

export default function ProfilePage() {
  return (
    <ProtectedRoute>
      <CallCatDashboard />
    </ProtectedRoute>
  )
}