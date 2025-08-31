import { useState, useEffect } from "react"
import { DashboardState } from "@/types"

export function useDashboard() {
  const [state, setState] = useState<DashboardState>({
    activeSection: "home",
    isMobile: false,
    sidebarOpen: true,
    selectedLanguage: "en",
    expandedCall: null,
    scheduledPage: 0,
    completedPage: 0,
    searchQuery: "",
  })

  useEffect(() => {
    const handleResize = () => {
      const isMobile = window.innerWidth < 768
      setState(prev => ({
        ...prev,
        isMobile,
        sidebarOpen: isMobile ? false : prev.sidebarOpen
      }))
    }

    handleResize()
    window.addEventListener("resize", handleResize)

    return () => {
      window.removeEventListener("resize", handleResize)
    }
  }, [])

  const setActiveSection = (section: string) => {
    setState(prev => ({ ...prev, activeSection: section }))
  }

  const setSidebarOpen = (open: boolean) => {
    setState(prev => ({ ...prev, sidebarOpen: open }))
  }

  const setSelectedLanguage = (language: string) => {
    setState(prev => ({ ...prev, selectedLanguage: language }))
  }

  const setExpandedCall = (id: number | null) => {
    setState(prev => ({ ...prev, expandedCall: id }))
  }

  const setScheduledPage = (page: number) => {
    setState(prev => ({ ...prev, scheduledPage: page }))
  }

  const setCompletedPage = (page: number) => {
    setState(prev => ({ ...prev, completedPage: page }))
  }

  const setSearchQuery = (query: string) => {
    setState(prev => ({ ...prev, searchQuery: query }))
  }

  return {
    ...state,
    setActiveSection,
    setSidebarOpen,
    setSelectedLanguage,
    setExpandedCall,
    setScheduledPage,
    setCompletedPage,
    setSearchQuery,
  }
}
