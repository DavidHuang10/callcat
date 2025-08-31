"use client"

import {
  Menu,
  Bell,
  User,
  Heart,
  Settings,
  Search,
} from "lucide-react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
  DropdownMenuSeparator,
} from "@/components/ui/dropdown-menu"

interface HeaderProps {
  sidebarOpen: boolean
  setSidebarOpen: (open: boolean) => void
  searchQuery: string
  setSearchQuery: (query: string) => void
}

export default function Header({ sidebarOpen, setSidebarOpen, searchQuery, setSearchQuery }: HeaderProps) {
  return (
    <header className="bg-white/80 backdrop-blur-sm border-b-2 border-purple-200 px-4 lg:px-6 py-3 flex items-center justify-between sticky top-0 z-10">
      {/* Left Section */}
      <div className="flex items-center gap-3">
        <Button
          variant="ghost"
          size="icon"
          className="lg:hidden hover:bg-purple-100 transition-colors duration-200"
          onClick={() => setSidebarOpen(!sidebarOpen)}
        >
          <Menu className="h-5 w-5 text-purple-600" />
        </Button>

        <div className="hidden lg:block">
          <h1 className="text-xl font-bold bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent">
            CallCat
          </h1>
        </div>
      </div>

      {/* Center Section - Search */}
      <div className="flex-1 max-w-md mx-4 lg:mx-8">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
          <Input
            type="text"
            placeholder="Search calls, businesses, or purposes..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="pl-10 pr-4 py-2 bg-white/60 border-purple-200 focus:border-purple-400 focus:ring-purple-400 transition-all duration-200"
          />
        </div>
      </div>

      {/* Right Section */}
      <div className="flex items-center gap-2">
        {/* Notifications */}
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" size="icon" className="relative hover:bg-purple-100 transition-colors duration-200">
              <Bell className="h-5 w-5 text-purple-600" />
              <span className="absolute top-0 right-0 h-2 w-2 bg-pink-500 rounded-full animate-pulse"></span>
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            <DropdownMenuItem>
              <Bell className="h-4 w-4 mr-2" />
              New call scheduled
            </DropdownMenuItem>
            <DropdownMenuItem>
              <Bell className="h-4 w-4 mr-2" />
              Call completed successfully
            </DropdownMenuItem>
            <DropdownMenuItem>
              <Bell className="h-4 w-4 mr-2" />
              System update available
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>

        {/* User Menu */}
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button
              variant="ghost"
              className="relative h-10 w-10 rounded-full hover:bg-purple-100 transition-colors duration-200"
            >
              <Avatar className="h-10 w-10 border-2 border-purple-200">
                <AvatarImage src="/placeholder.svg?height=40&width=40" alt="Sarah" />
                <AvatarFallback className="bg-gradient-to-br from-purple-400 to-pink-500 text-white font-semibold">
                  S
                </AvatarFallback>
              </Avatar>
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            <DropdownMenuItem>
              <User className="h-4 w-4 mr-2" />
              Profile
            </DropdownMenuItem>
            <DropdownMenuItem>
              <Settings className="h-4 w-4 mr-2" />
              Settings
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem>
              <Heart className="h-4 w-4 mr-2" />
              Feedback
            </DropdownMenuItem>
            <DropdownMenuItem>Logout</DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </header>
  )
}
