"use client"

import { useDashboard } from "@/hooks/useDashboard"
import Header from "@/components/Header"
import Sidebar from "@/components/Sidebar"
import HomeSection from "@/components/sections/HomeSection"
import MakeCallSection from "@/components/sections/MakeCallSection"

export default function CallCatDashboard() {
  const {
    activeSection,
    isMobile,
    sidebarOpen,
    selectedLanguage,
    expandedCall,
    scheduledPage,
    completedPage,
    searchQuery,
    setActiveSection,
    setSidebarOpen,
    setSelectedLanguage,
    setExpandedCall,
    setScheduledPage,
    setCompletedPage,
    setSearchQuery,
  } = useDashboard()

  const renderSection = () => {
    switch (activeSection) {
      case "home":
        return (
          <HomeSection
            searchQuery={searchQuery}
            expandedCall={expandedCall}
            setExpandedCall={setExpandedCall}
            setActiveSection={setActiveSection}
          />
        )
      case "make-call":
        return (
          <MakeCallSection 
            onCallCreated={() => setActiveSection("home")}
          />
        )
      case "active":
        return (
          <div className="p-4 lg:p-6">
            <h1 className="text-2xl font-bold text-gray-800 mb-4">Active Calls</h1>
            <p className="text-gray-600">Active calls will be displayed here...</p>
          </div>
        )

      case "settings":
        return (
          <div className="p-4 lg:p-6">
            <h1 className="text-2xl font-bold text-gray-800 mb-4">Settings</h1>
            <p className="text-gray-600">Settings will be displayed here...</p>
          </div>
        )
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
      />

      <div className="flex flex-1 overflow-hidden">
        {/* Sidebar */}
        <Sidebar
          activeSection={activeSection}
          setActiveSection={setActiveSection}
          sidebarOpen={sidebarOpen}
          selectedLanguage={selectedLanguage}
          setSelectedLanguage={setSelectedLanguage}
        />

        {/* Main Content */}
        <main className="flex-1 overflow-y-auto">
          {renderSection()}
        </main>
      </div>
    </div>
  )
}
