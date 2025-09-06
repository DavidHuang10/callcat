import { useState, useEffect } from "react"
import { DashboardState, RescheduleData } from "@/types"

export function useDashboard() {
  const [state, setState] = useState<DashboardState>({
    activeSection: "home",
    isMobile: false,
    sidebarOpen: true,
    sidebarCollapsed: false,
    selectedLanguage: "en",
    expandedTranscripts: new Set<string>(),
    scheduledPage: 0,
    completedPage: 0,
    searchQuery: "",
    rescheduleData: null,
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

    // Load sidebar collapsed state from localStorage
    const savedCollapsed = localStorage.getItem('sidebar-collapsed')
    if (savedCollapsed) {
      setState(prev => ({
        ...prev,
        sidebarCollapsed: JSON.parse(savedCollapsed)
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

  const toggleExpandedTranscript = (id: string) => {
    setState(prev => {
      const newExpandedTranscripts = new Set(prev.expandedTranscripts)
      if (newExpandedTranscripts.has(id)) {
        newExpandedTranscripts.delete(id)
      } else {
        newExpandedTranscripts.add(id)
      }
      return { ...prev, expandedTranscripts: newExpandedTranscripts }
    })
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

  const setSidebarCollapsed = (collapsed: boolean) => {
    setState(prev => ({ ...prev, sidebarCollapsed: collapsed }))
    localStorage.setItem('sidebar-collapsed', JSON.stringify(collapsed))
  }

  const toggleSidebarCollapsed = () => {
    setSidebarCollapsed(!state.sidebarCollapsed)
  }

  const setRescheduleData = (data: RescheduleData | null) => {
    setState(prev => ({ ...prev, rescheduleData: data }))
  }

  const clearRescheduleData = () => {
    setState(prev => ({ ...prev, rescheduleData: null }))
  }

  return {
    ...state,
    setActiveSection,
    setSidebarOpen,
    setSelectedLanguage,
    toggleExpandedTranscript,
    setScheduledPage,
    setCompletedPage,
    setSearchQuery,
    setSidebarCollapsed,
    toggleSidebarCollapsed,
    setRescheduleData,
    clearRescheduleData,
  }
}
