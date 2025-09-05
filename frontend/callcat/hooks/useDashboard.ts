import { useState, useEffect } from "react"
import { DashboardState } from "@/types"

export function useDashboard() {
  const [state, setState] = useState<DashboardState>({
    activeSection: "home",
    isMobile: false,
    sidebarOpen: true,
    selectedLanguage: "en",
    expandedTranscripts: new Set<string>(),
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

  return {
    ...state,
    setActiveSection,
    setSidebarOpen,
    setSelectedLanguage,
    toggleExpandedTranscript,
    setScheduledPage,
    setCompletedPage,
    setSearchQuery,
  }
}
