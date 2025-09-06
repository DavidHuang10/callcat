"use client"

import { useDashboard } from "@/hooks/useDashboard"
import Header from "@/components/Header"
import Sidebar from "@/components/Sidebar"
import HomeSection from "@/components/sections/HomeSection"
import MakeCallSection from "@/components/sections/MakeCallSection"
import { ProfileSection } from "@/components/sections/ProfileSection"

export default function CallCatDashboard() {
  const {
    activeSection,
    sidebarOpen,
    sidebarCollapsed,
    expandedTranscripts,
    searchQuery,
    rescheduleData,
    setActiveSection,
    setSidebarOpen,
    toggleExpandedTranscript,
    setSearchQuery,
    toggleSidebarCollapsed,
    setRescheduleData,
    clearRescheduleData,
  } = useDashboard()

  const renderSection = () => {
    switch (activeSection) {
      case "home":
        return (
          <HomeSection
            searchQuery={searchQuery}
            expandedTranscripts={expandedTranscripts}
            toggleExpandedTranscript={toggleExpandedTranscript}
            setActiveSection={setActiveSection}
            setRescheduleData={setRescheduleData}
          />
        )
      case "make-call":
        return (
          <MakeCallSection 
            onCallCreated={() => setActiveSection("home")}
            rescheduleData={rescheduleData}
            clearRescheduleData={clearRescheduleData}
          />
        )
      case "active":
        return (
          <div className="p-4 lg:p-6">
            <h1 className="text-2xl font-bold text-gray-800 mb-4">Active Calls</h1>
            <p className="text-gray-600">Active calls will be displayed here...</p>
          </div>
        )

      case "profile":
        return <ProfileSection />
      default:
        return (
          <div className="p-4 lg:p-6">
            <h1 className="text-2xl font-bold text-gray-800 mb-4">Dashboard</h1>
            <p className="text-gray-600">Select a section from the sidebar...</p>
          </div>
        )
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-purple-50 via-white to-pink-50">
      {/* Header */}
      <Header
        sidebarOpen={sidebarOpen}
        setSidebarOpen={setSidebarOpen}
        searchQuery={searchQuery}
        setSearchQuery={setSearchQuery}
        setActiveSection={setActiveSection}
      />

      <div className="flex flex-1 overflow-hidden">
        {/* Sidebar */}
        <Sidebar
          activeSection={activeSection}
          setActiveSection={setActiveSection}
          sidebarOpen={sidebarOpen}
          sidebarCollapsed={sidebarCollapsed}
          toggleSidebarCollapsed={toggleSidebarCollapsed}
        />

        {/* Main Content */}
        <main 
          className={`flex-1 overflow-y-auto transition-all duration-300 ${
            sidebarCollapsed ? 'ml-20' : 'ml-64'
          }`}
        >
          {renderSection()}
        </main>
      </div>
    </div>
  )
}
