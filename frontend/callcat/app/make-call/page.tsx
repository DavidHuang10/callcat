import CallCatDashboard from "@/components/Dashboard"
import ProtectedRoute from "@/components/ProtectedRoute"

export default function MakeCallPage() {
  return (
    <ProtectedRoute>
      <CallCatDashboard />
    </ProtectedRoute>
  )
}