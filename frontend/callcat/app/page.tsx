import CallCatDashboard from "@/components/Dashboard"
import ProtectedRoute from "@/components/ProtectedRoute"

export default function Home() {
  return (
    <ProtectedRoute>
      <CallCatDashboard />
    </ProtectedRoute>
  )
}
