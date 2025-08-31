"use client"

import {
  Home,
  Phone,
  PhoneCall,
  Settings,
  Globe,
} from "lucide-react"
import { Button } from "@/components/ui/button"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Label } from "@/components/ui/label"

interface SidebarProps {
  activeSection: string
  setActiveSection: (section: string) => void
  sidebarOpen: boolean
  selectedLanguage: string
  setSelectedLanguage: (language: string) => void
}

export default function Sidebar({ 
  activeSection, 
  setActiveSection, 
  sidebarOpen, 
  selectedLanguage, 
  setSelectedLanguage 
}: SidebarProps) {
  const navigationItems = [
    { id: "home", label: "Dashboard", icon: Home },
    { id: "make-call", label: "Make a Call", icon: Phone },
    { id: "active", label: "Active Calls", icon: PhoneCall },
    { id: "settings", label: "Settings", icon: Settings },
  ]

  return (
    <div
      className={`${
        sidebarOpen ? "w-64" : "w-0"
      } bg-white/90 backdrop-blur-sm border-r-2 border-purple-200 transition-all duration-500 ease-in-out overflow-hidden shadow-lg`}
    >
      <div className="p-4 lg:p-6 h-full flex flex-col">
        {/* Language Selector */}
        <div className="mb-6">
          <Label className="text-sm font-medium text-gray-700 mb-2 block">Language</Label>
          <Select value={selectedLanguage} onValueChange={setSelectedLanguage}>
            <SelectTrigger className="w-full">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="en">
                <div className="flex items-center gap-2">
                  <Globe className="w-4 h-4" />
                  English
                </div>
              </SelectItem>
              <SelectItem value="es">
                <div className="flex items-center gap-2">
                  <Globe className="w-4 h-4" />
                  Español
                </div>
              </SelectItem>
              <SelectItem value="fr">
                <div className="flex items-center gap-2">
                  <Globe className="w-4 h-4" />
                  Français
                </div>
              </SelectItem>
              <SelectItem value="de">
                <div className="flex items-center gap-2">
                  <Globe className="w-4 h-4" />
                  Deutsch
                </div>
              </SelectItem>
              <SelectItem value="ja">
                <div className="flex items-center gap-2">
                  <Globe className="w-4 h-4" />
                  日本語
                </div>
              </SelectItem>
              <SelectItem value="zh">
                <div className="flex items-center gap-2">
                  <Globe className="w-4 h-4" />
                  中文
                </div>
              </SelectItem>
            </SelectContent>
          </Select>
        </div>

        {/* Navigation */}
        <nav className="space-y-2 flex-1">
          {navigationItems.map((item) => {
            const Icon = item.icon
            return (
              <Button
                key={item.id}
                variant="ghost"
                className={`w-full justify-start text-left transition-all duration-300 ${
                  activeSection === item.id
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
        <div className="pt-4 border-t border-purple-200">
          <div className="text-xs text-gray-500 text-center">
            CallCat v1.0.0
          </div>
        </div>
      </div>
    </div>
  )
}
