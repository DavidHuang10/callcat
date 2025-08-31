"use client"

import {
  Phone,
  Clock,
  CheckCircle,
  Activity,
  Coffee,
  Zap,
  Settings,
} from "lucide-react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { getFilteredCalls } from "@/data/calls"
import CallCard from "@/components/CallCard"

interface HomeSectionProps {
  searchQuery: string
  expandedCall: string | null
  setExpandedCall: (id: string | null) => void
  setActiveSection: (section: string) => void
}

export default function HomeSection({ 
  searchQuery, 
  expandedCall, 
  setExpandedCall, 
  setActiveSection 
}: HomeSectionProps) {
  const { scheduledCalls, completedCalls } = getFilteredCalls(searchQuery)

  const stats = [
    {
      title: "Total Calls",
      value: "47",
      change: "+12%",
      changeType: "positive",
      icon: Phone,
      color: "text-blue-600",
      bgColor: "bg-blue-50",
    },
    {
      title: "Success Rate",
      value: "89%",
      change: "+5%",
      changeType: "positive",
      icon: CheckCircle,
      color: "text-green-600",
      bgColor: "bg-green-50",
    },
    {
      title: "Avg Duration",
      value: "3m 24s",
      change: "-8%",
      changeType: "negative",
      icon: Clock,
      color: "text-purple-600",
      bgColor: "bg-purple-50",
    },
    {
      title: "Active Calls",
      value: "3",
      change: "0",
      changeType: "neutral",
      icon: Activity,
      color: "text-orange-600",
      bgColor: "bg-orange-50",
    },
  ]

  return (
    <div className="space-y-6 p-4 lg:p-6">
      {/* Welcome Section */}
      <div className="text-center mb-8">
        <h1 className="text-3xl lg:text-4xl font-bold bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent mb-3">
          Welcome back, Sarah! 👋
        </h1>
        <p className="text-gray-600 text-lg">
          Here&apos;s what&apos;s happening with your calls today
        </p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {stats.map((stat, index) => {
          const Icon = stat.icon
          return (
            <Card key={index} className="hover:shadow-lg transition-all duration-300">
              <CardContent className="p-4">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm font-medium text-gray-600">{stat.title}</p>
                    <p className="text-2xl font-bold text-gray-900">{stat.value}</p>
                    <div className="flex items-center gap-1 mt-1">
                      <span
                        className={`text-xs font-medium ${
                          stat.changeType === "positive"
                            ? "text-green-600"
                            : stat.changeType === "negative"
                            ? "text-red-600"
                            : "text-gray-500"
                        }`}
                      >
                        {stat.change}
                      </span>
                      <span className="text-xs text-gray-500">from last week</span>
                    </div>
                  </div>
                  <div className={`p-3 rounded-full ${stat.bgColor}`}>
                    <Icon className={`h-6 w-6 ${stat.color}`} />
                  </div>
                </div>
              </CardContent>
            </Card>
          )
        })}
      </div>

      {/* Quick Actions */}
      <Card className="bg-gradient-to-r from-purple-50 to-pink-50 border-purple-200">
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-purple-700">
            <Zap className="h-5 w-5" />
            Quick Actions
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <button
              onClick={() => setActiveSection("make-call")}
              className="p-4 bg-white rounded-lg border-2 border-purple-200 hover:border-purple-400 hover:shadow-md transition-all duration-200 text-left group"
            >
              <div className="flex items-center gap-3 mb-2">
                <div className="p-2 bg-purple-100 rounded-lg group-hover:bg-purple-200 transition-colors duration-200">
                  <Phone className="h-5 w-5 text-purple-600" />
                </div>
                <div>
                  <h3 className="font-semibold text-gray-800">Make a Call</h3>
                  <p className="text-sm text-gray-600">Schedule a new call</p>
                </div>
              </div>
            </button>



            <button
              onClick={() => setActiveSection("settings")}
              className="p-4 bg-white rounded-lg border-2 border-purple-200 hover:border-purple-400 hover:shadow-md transition-all duration-200 text-left group"
            >
              <div className="flex items-center gap-3 mb-2">
                <div className="p-2 bg-blue-100 rounded-lg group-hover:bg-blue-200 transition-colors duration-200">
                  <Settings className="h-5 w-5 text-blue-600" />
                </div>
                <div>
                  <h3 className="font-semibold text-gray-800">Settings</h3>
                  <p className="text-sm text-gray-600">Configure preferences</p>
                </div>
              </div>
            </button>
          </div>
        </CardContent>
      </Card>

      {/* Recent Calls */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-xl font-semibold text-gray-800">Recent Calls</h2>
          <Badge variant="secondary" className="text-xs">
            {completedCalls.length} calls
          </Badge>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
          {completedCalls.slice(0, 4).map((call) => (
            <CallCard
              key={call.id}
              call={call}
              expandedCall={expandedCall}
              setExpandedCall={setExpandedCall}
              setActiveSection={setActiveSection}
            />
          ))}
        </div>

        {completedCalls.length === 0 && (
          <Card className="text-center py-12">
            <CardContent>
              <Coffee className="h-12 w-12 text-gray-400 mx-auto mb-4" />
              <h3 className="text-lg font-medium text-gray-600 mb-2">No calls yet</h3>
              <p className="text-gray-500">Start by making your first call!</p>
            </CardContent>
          </Card>
        )}
      </div>

      {/* Scheduled Calls */}
      {scheduledCalls.length > 0 && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-xl font-semibold text-gray-800">Scheduled Calls</h2>
            <Badge variant="secondary" className="text-xs">
              {scheduledCalls.length} scheduled
            </Badge>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
            {scheduledCalls.slice(0, 2).map((call) => (
              <CallCard
                key={call.id}
                call={call}
                expandedCall={expandedCall}
                setExpandedCall={setExpandedCall}
                setActiveSection={setActiveSection}
              />
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
