"use client"

import { useState } from "react"
import { usePathname } from "next/navigation"
import Link from "next/link"
import {
  Home,
  Phone,
  User,
  ChevronLeft,
  ChevronRight,
} from "lucide-react"
import { Button } from "@/components/ui/button"
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@/components/ui/tooltip"

interface SidebarProps {
  activeSection: string
  setActiveSection: (section: string) => void
  sidebarOpen: boolean
  sidebarCollapsed: boolean
  toggleSidebarCollapsed: () => void
}

export default function Sidebar({ 
  activeSection, 
  setActiveSection, 
  sidebarOpen, 
  sidebarCollapsed,
  toggleSidebarCollapsed
}: SidebarProps) {
  const [isHovered, setIsHovered] = useState(false)
  
  const navigationItems = [
    { id: "home", label: "Dashboard", icon: Home },
    { id: "make-call", label: "Make a Call", icon: Phone },
    { id: "profile", label: "Profile", icon: User },
  ]

  const isExpanded = sidebarCollapsed ? isHovered : true
  const sidebarWidth = sidebarCollapsed ? (isHovered ? 'w-64' : 'w-20') : 'w-64'

  return (
    <TooltipProvider delayDuration={300}>
      <div
        className={`fixed left-0 top-16 bottom-0 ${sidebarWidth} bg-white/90 backdrop-blur-sm border-r-2 border-purple-200 transition-all duration-300 ease-in-out shadow-lg z-40`}
        onMouseEnter={() => setIsHovered(true)}
        onMouseLeave={() => setIsHovered(false)}
      >
        <div className="p-4 h-full flex flex-col">
          {/* Collapse Toggle */}
          <div className="mb-4 flex justify-end">
            <Button
              variant="ghost" 
              size="icon"
              onClick={toggleSidebarCollapsed}
              className="h-8 w-8 hover:bg-purple-100 transition-colors"
            >
              {sidebarCollapsed ? (
                <ChevronRight className="h-4 w-4 text-purple-600" />
              ) : (
                <ChevronLeft className="h-4 w-4 text-purple-600" />
              )}
            </Button>
          </div>


          {/* Navigation */}
          <nav className="space-y-2 flex-1">
            {navigationItems.map((item) => {
              const Icon = item.icon
              const isActive = activeSection === item.id
              
              if (!isExpanded) {
                return (
                  <Tooltip key={item.id}>
                    <TooltipTrigger asChild>
                      <Button
                        variant="ghost"
                        size="icon"
                        className={`w-full h-12 transition-all duration-300 ${
                          isActive
                            ? "bg-gradient-to-r from-purple-100 to-pink-100 text-purple-700 shadow-md"
                            : "text-gray-600 hover:text-purple-600 hover:bg-purple-50"
                        }`}
                        onClick={() => setActiveSection(item.id)}
                      >
                        <Icon className="h-5 w-5" />
                      </Button>
                    </TooltipTrigger>
                    <TooltipContent side="right">
                      <p>{item.label}</p>
                    </TooltipContent>
                  </Tooltip>
                )
              }
              
              return (
                <Button
                  key={item.id}
                  variant="ghost"
                  className={`w-full justify-start text-left transition-all duration-300 ${
                    isActive
                      ? "bg-gradient-to-r from-purple-100 to-pink-100 text-purple-700 shadow-md"
                      : "text-gray-600 hover:text-purple-600 hover:bg-purple-50"
                  }`}
                  onClick={() => setActiveSection(item.id)}
                >
                  <Icon className="h-5 w-5 mr-3" />
                  {item.label}
                </Button>
              )
            })}
          </nav>

          {/* Footer */}
          {isExpanded && (
            <div className="pt-4 border-t border-purple-200">
              <div className="text-xs text-gray-500 text-center">
                CallCat v1.0.0
              </div>
            </div>
          )}
        </div>
      </div>
    </TooltipProvider>
  )
}
