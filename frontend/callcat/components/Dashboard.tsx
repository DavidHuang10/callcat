"use client"

import { usePathname, useRouter } from "next/navigation"

import Header from "@/components/Header"
import Sidebar from "@/components/Sidebar"
import HomeSection from "@/components/sections/HomeSection"
import MakeCallSection from "@/components/sections/MakeCallSection"
import { ProfileSection } from "@/components/sections/ProfileSection"

import { useDashboard } from "@/hooks/useDashboard"

export default function CallCatDashboard() {
  const pathname = usePathname()
  const router = useRouter()
  const {
    sidebarOpen,
    sidebarCollapsed,
    expandedTranscripts,
    searchQuery,
    setSidebarOpen,
    toggleExpandedTranscript,
    setSearchQuery,
    toggleSidebarCollapsed,
  } = useDashboard()

  // Determine active section from pathname
  const getActiveSection = () => {
    if (pathname === '/make-call') return 'make-call'
    if (pathname === '/profile') return 'profile'
    return 'home'
  }

  const activeSection = getActiveSection()
  
  const setActiveSection = (section: string) => {
    // Navigate to the appropriate route using Next.js router
    if (section === 'make-call') {
      router.push('/make-call')
    } else if (section === 'profile') {
      router.push('/profile')
    } else {
      router.push('/')
    }
  }

  const renderSection = () => {
    switch (activeSection) {
      case "make-call":
        return (
          <MakeCallSection
            onCallCreated={() => router.push('/')}
          />
        )
      case "profile":
        return <ProfileSection />
      case "home":
      default:
        return (
          <HomeSection
            searchQuery={searchQuery}
            expandedTranscripts={expandedTranscripts}
            toggleExpandedTranscript={toggleExpandedTranscript}
            setActiveSection={setActiveSection}
          />
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
          sidebarCollapsed={sidebarCollapsed}
          toggleSidebarCollapsed={toggleSidebarCollapsed}
        />

        {/* Main Content */}
        <main 
          className={`flex-1 overflow-y-auto transition-all duration-300 ${
            sidebarCollapsed ? 'ml-20' : 'ml-64'
          } flex flex-col`}
        >
          <div className="flex-1">
            {renderSection()}
          </div>
          
          {/* Feedback Footer */}
          <footer className="p-4 border-t border-purple-200 bg-white/50 backdrop-blur-sm">
            <div className="text-center text-sm text-gray-600">
              Got feedback? Email me at polarpenguins24@gmail.com
            </div>
          </footer>
        </main>
      </div>
    </div>
  )
}
