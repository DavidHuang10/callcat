import CallCatDashboard from "@/dashboard"
import ProtectedRoute from "@/components/ProtectedRoute"

export default function Home() {
  return (
    <ProtectedRoute>
      <CallCatDashboard />
    </ProtectedRoute>
  )
}
