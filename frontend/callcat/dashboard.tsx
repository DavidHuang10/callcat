"use client"

import { useState, useEffect } from "react"
import {
  Phone,
  PhoneCall,
  Clock,
  CheckCircle,
  AlertCircle,
  Users,
  Settings,
  Globe,
  Plus,
  Menu,
  Bell,
  User,
  Languages,
  Timer,
  ChevronDown,
  ChevronRight,
  RotateCcw,
  Activity,
  Heart,
  Star,
  Sparkles,
  Coffee,
  MessageCircle,
  Calendar,
  Zap,
  Home,
  Search,
  ChevronLeft,
  Send,
  Building,
  Utensils,
  Stethoscope,
  Shield,
  X,
  XCircle,
} from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
  DropdownMenuSeparator,
} from "@/components/ui/dropdown-menu"
import { Badge } from "@/components/ui/badge"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Textarea } from "@/components/ui/textarea"
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from "@/components/ui/collapsible"

export default function CallCatDashboard() {
  const [activeSection, setActiveSection] = useState("home")
  const [isMobile, setIsMobile] = useState(false)
  const [sidebarOpen, setSidebarOpen] = useState(true)
  const [selectedLanguage, setSelectedLanguage] = useState("en")
  const [expandedCall, setExpandedCall] = useState<number | null>(null)
  const [scheduledPage, setScheduledPage] = useState(0)
  const [completedPage, setCompletedPage] = useState(0)
  const [searchQuery, setSearchQuery] = useState("")

  useEffect(() => {
    const handleResize = () => {
      setIsMobile(window.innerWidth < 768)
      if (window.innerWidth < 768) {
        setSidebarOpen(false)
      }
    }

    handleResize()
    window.addEventListener("resize", handleResize)

    return () => {
      window.removeEventListener("resize", handleResize)
    }
  }, [])

  // Enhanced call data with comprehensive transcripts
  const allCalls = [
    {
      id: "CC-001",
      business: "Pizza Palace",
      phone: "+1-555-PIZZA",
      purpose: "Ask about gluten-free pizza options",
      status: "successful",
      timestamp: "2024-01-15T14:30:00Z",
      duration: "3:24",
      language: "en",
      transcript: [
        { speaker: "business", text: "Hello, Pizza Palace, how can I help you?" },
        { speaker: "ai", text: "Hi! I'm calling to ask about your gluten-free pizza options." },
        {
          speaker: "business",
          text: "Yes, we have gluten-free crust available for all our pizzas. It's an additional $3.",
        },
        { speaker: "ai", text: "Great! What sizes are available for gluten-free?" },
        { speaker: "business", text: "We have small, medium, and large. All the same toppings are available." },
        { speaker: "ai", text: "Perfect. Can you email the full menu to sarah@email.com?" },
        { speaker: "business", text: "Absolutely, I'll send that right over. Is there anything else?" },
        { speaker: "ai", text: "That's everything, thank you so much!" },
        { speaker: "business", text: "You're welcome! Have a great day!" },
      ],
      result: "Confirmed gluten-free options available (+$3). Menu sent to email.",
      icon: Utensils,
    },
    {
      id: "CC-002",
      business: "Dr. Smith's Office",
      phone: "+1-555-TEETH",
      purpose: "Reschedule appointment from Tuesday to Friday",
      status: "successful",
      timestamp: "2024-01-15T11:15:00Z",
      duration: "2:18",
      language: "en",
      transcript: [
        { speaker: "business", text: "Dr. Smith's office, this is Jennifer." },
        { speaker: "ai", text: "Hi Jennifer, I need to reschedule Sarah Johnson's appointment from Tuesday at 2 PM." },
        { speaker: "business", text: "Let me check the schedule. What day works better?" },
        { speaker: "ai", text: "Friday afternoon would be ideal, any time after 1 PM." },
        { speaker: "business", text: "I have Friday at 3 PM available." },
        { speaker: "ai", text: "Perfect, that works great." },
        { speaker: "business", text: "All set! I'll send a confirmation text." },
        { speaker: "ai", text: "Thank you so much for your help!" },
      ],
      result: "Appointment moved to Friday 3 PM. Confirmation sent via SMS.",
      icon: Stethoscope,
    },
    {
      id: "CC-003",
      business: "Sakura Sushi",
      phone: "+1-555-SUSHI",
      purpose: "Make dinner reservation for 4 people",
      status: "scheduled",
      timestamp: "2024-01-15T19:00:00Z",
      duration: null,
      language: "ja",
      transcript: [
        { speaker: "business", text: "いらっしゃいませ、桜寿司です。" },
        { speaker: "ai", text: "こんにちは。今夜4名様でご予約をお願いしたいのですが。" },
        { speaker: "business", text: "何時頃をご希望でしょうか？" },
        { speaker: "ai", text: "7時半はいかがでしょうか？" },
        { speaker: "business", text: "申し訳ございませんが、7時半は満席です。8時はいかがでしょうか？" },
        { speaker: "ai", text: "8時で結構です。お名前はサラ・ジョンソンです。" },
        { speaker: "business", text: "承知いたしました。8時に4名様でご予約をお取りしました。" },
      ],
      result: "Reservation confirmed for 4 people at 8:00 PM tonight.",
      icon: Utensils,
    },
    {
      id: "CC-004",
      business: "City Insurance",
      phone: "+1-555-CLAIM",
      purpose: "Check claim status #INS-789456",
      status: "needs_attention",
      timestamp: "2024-01-14T09:30:00Z",
      duration: "5:42",
      language: "en",
      transcript: [
        { speaker: "business", text: "City Insurance claims department, this is Mark." },
        { speaker: "ai", text: "Hi Mark, I'm calling to check on claim status for policy holder Sarah Johnson." },
        { speaker: "business", text: "I'll need the policy number and claim number to look that up." },
        { speaker: "ai", text: "The claim number is INS-789456." },
        {
          speaker: "business",
          text: "I see the claim, but I need the policy number as well to access the full account.",
        },
        { speaker: "ai", text: "I don't have that information available right now." },
        {
          speaker: "business",
          text: "Without the policy number, I can't provide claim details. You'll need to call back with that information.",
        },
        { speaker: "ai", text: "I understand. Where can I find the policy number?" },
        {
          speaker: "business",
          text: "It should be on your insurance card or any policy documents. It's usually a 10-digit number.",
        },
        { speaker: "ai", text: "Thank you, I'll locate that and call back." },
      ],
      result: "Need policy number to proceed. Please provide and reschedule.",
      icon: Shield,
    },
    {
      id: "CC-005",
      business: "Sunny Apartments",
      phone: "+1-555-RENT",
      purpose: "Inquire about 2BR availability",
      status: "scheduled",
      timestamp: "2024-01-15T15:00:00Z",
      duration: null,
      language: "en",
      transcript: [
        { speaker: "business", text: "Sunny Apartments leasing office, this is Maria." },
        { speaker: "ai", text: "Hi Maria, I'm interested in your 2-bedroom apartment availability." },
        { speaker: "business", text: "Great! We have a few units available. What's your move-in timeframe?" },
        { speaker: "ai", text: "I'm looking to move in within the next month." },
        {
          speaker: "business",
          text: "Perfect. We have units on the 3rd and 5th floors available. Would you like to schedule a viewing?",
        },
        { speaker: "ai", text: "Yes, that would be great. What times work best?" },
        { speaker: "business", text: "How about tomorrow at 2 PM?" },
        { speaker: "ai", text: "That works perfectly. Should I bring anything specific?" },
        { speaker: "business", text: "Just a photo ID. I'll have all the information ready for you." },
      ],
      result: "Viewing scheduled for tomorrow at 2 PM. Bring photo ID.",
      icon: Building,
    },
    {
      id: "CC-006",
      business: "Fluffy Vet Clinic",
      phone: "+1-555-PETS",
      purpose: "Schedule checkup for Whiskers",
      status: "successful",
      timestamp: "2024-01-14T16:20:00Z",
      duration: "4:15",
      language: "en",
      transcript: [
        { speaker: "business", text: "Fluffy Vet Clinic, this is Dr. Martinez's office. How can we help?" },
        { speaker: "ai", text: "Hi, I'd like to schedule a routine checkup for my cat Whiskers." },
        { speaker: "business", text: "Of course! How old is Whiskers and when was the last visit?" },
        { speaker: "ai", text: "She's 3 years old and her last checkup was about 8 months ago." },
        { speaker: "business", text: "Perfect timing for an annual checkup. What days work best for you?" },
        { speaker: "ai", text: "Thursday would be ideal if you have anything available." },
        { speaker: "business", text: "We have Thursday at 2 PM with Dr. Martinez. Does that work?" },
        { speaker: "ai", text: "That's perfect! Should I bring her vaccination records?" },
        {
          speaker: "business",
          text: "We have them on file, but feel free to bring them just in case. We'll send a reminder the day before.",
        },
        { speaker: "ai", text: "Wonderful, thank you so much!" },
      ],
      result: "Appointment scheduled for Thursday 2 PM. Reminder will be sent.",
      icon: Heart,
    },
    {
      id: "CC-007",
      business: "Local Pharmacy",
      phone: "+1-555-MEDS",
      purpose: "Check prescription status",
      status: "scheduled",
      timestamp: "2024-01-16T10:00:00Z",
      duration: null,
      language: "en",
      transcript: [
        { speaker: "business", text: "Westside Pharmacy, this is Tom speaking." },
        { speaker: "ai", text: "Hi Tom, I'm calling to check on a prescription for Sarah Johnson." },
        { speaker: "business", text: "Let me look that up. What's the medication name or prescription number?" },
        { speaker: "ai", text: "It's for Lisinopril, prescribed by Dr. Williams last week." },
        { speaker: "business", text: "I see it here. It's ready for pickup! We've been holding it since yesterday." },
        { speaker: "ai", text: "Great! What are your pickup hours today?" },
        { speaker: "business", text: "We're open until 9 PM tonight, and we're here all day tomorrow too." },
        { speaker: "ai", text: "Perfect, I'll be by this afternoon. Thank you!" },
      ],
      result: "Prescription ready for pickup. Pharmacy open until 9 PM.",
      icon: Stethoscope,
    },
    {
      id: "CC-008",
      business: "Garden Center",
      phone: "+1-555-PLANT",
      purpose: "Ask about spring planting schedule",
      status: "failed",
      timestamp: "2024-01-14T14:30:00Z",
      duration: "1:12",
      language: "en",
      transcript: [
        {
          speaker: "business",
          text: "Thank you for calling Green Thumb Garden Center. We're currently closed for inventory.",
        },
        {
          speaker: "ai",
          text: "Oh, I was hoping to ask about your spring planting schedule and what vegetables to plant now.",
        },
        {
          speaker: "business",
          text: "I'm sorry, but our staff isn't available right now. We're doing our annual inventory count.",
        },
        { speaker: "ai", text: "When would be a good time to call back?" },
        {
          speaker: "business",
          text: "We'll be back to normal hours tomorrow morning at 8 AM. Our garden specialists will be available then.",
        },
        { speaker: "ai", text: "Thank you, I'll call back tomorrow morning." },
      ],
      result: "Business closed for inventory. Call back tomorrow at 8 AM.",
      icon: Sparkles,
    },
  ]

  const scheduledCalls = allCalls.filter((call) => call.status === "scheduled")
  const completedCalls = allCalls.filter((call) => call.status !== "scheduled")

  const filteredScheduledCalls = scheduledCalls.filter(
    (call) =>
      call.business.toLowerCase().includes(searchQuery.toLowerCase()) ||
      call.purpose.toLowerCase().includes(searchQuery.toLowerCase()),
  )

  const filteredCompletedCalls = completedCalls.filter(
    (call) =>
      call.business.toLowerCase().includes(searchQuery.toLowerCase()) ||
      call.purpose.toLowerCase().includes(searchQuery.toLowerCase()),
  )

  const languages = [
    { code: "en", name: "English", flag: "🇺🇸" },
    { code: "es", name: "Español", flag: "🇪🇸" },
    { code: "fr", name: "Français", flag: "🇫🇷" },
    { code: "de", name: "Deutsch", flag: "🇩🇪" },
    { code: "ja", name: "日本語", flag: "🇯🇵" },
    { code: "zh", name: "中文", flag: "🇨🇳" },
  ]

  const getStatusConfig = (status: string) => {
    switch (status) {
      case "successful":
        return {
          color: "bg-green-100 text-green-700 border-green-200",
          icon: CheckCircle,
          label: "Successful",
          bgGradient: "from-green-50 to-emerald-50",
        }
      case "failed":
        return {
          color: "bg-red-100 text-red-700 border-red-200",
          icon: XCircle,
          label: "Failed",
          bgGradient: "from-red-50 to-pink-50",
        }
      case "scheduled":
        return {
          color: "bg-purple-100 text-purple-700 border-purple-200",
          icon: Clock,
          label: "Scheduled",
          bgGradient: "from-purple-50 to-pink-50",
        }
      case "needs_attention":
        return {
          color: "bg-amber-100 text-amber-700 border-amber-200",
          icon: AlertCircle,
          label: "Needs Attention",
          bgGradient: "from-amber-50 to-orange-50",
        }
      case "active":
        return {
          color: "bg-blue-100 text-blue-700 border-blue-200",
          icon: Activity,
          label: "Active",
          bgGradient: "from-blue-50 to-cyan-50",
        }
      default:
        return {
          color: "bg-gray-100 text-gray-700 border-gray-200",
          icon: Clock,
          label: "Unknown",
          bgGradient: "from-gray-50 to-slate-50",
        }
    }
  }

  const formatTimestamp = (timestamp: string) => {
    const date = new Date(timestamp)
    return {
      date: date.toLocaleDateString(),
      time: date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
    }
  }

  const CallCard = ({ call }: { call: any }) => {
    const statusConfig = getStatusConfig(call.status)
    const StatusIcon = statusConfig.icon
    const CallIcon = call.icon
    const isExpanded = expandedCall === call.id
    const { date, time } = formatTimestamp(call.timestamp)

    return (
      <Card
        className={`bg-gradient-to-br ${statusConfig.bgGradient} border-2 hover:shadow-lg transition-all duration-300 hover:scale-[1.02] group h-fit`}
      >
        <CardContent className="p-4">
          {/* Header Section */}
          <div className="flex items-start justify-between mb-3">
            <div className="flex items-center gap-3 min-w-0 flex-1">
              <div className="w-10 h-10 bg-white/60 rounded-lg flex items-center justify-center group-hover:scale-110 transition-transform duration-200 flex-shrink-0">
                <CallIcon className="w-5 h-5 text-gray-600" />
              </div>
              <div className="min-w-0 flex-1">
                <h3 className="font-semibold text-gray-800 text-base truncate">{call.business}</h3>
                <p className="text-xs text-gray-600 truncate">{call.id}</p>
              </div>
            </div>
            <Badge className={`${statusConfig.color} font-medium px-2 py-1 text-xs flex-shrink-0 ml-2`}>
              <StatusIcon className="w-3 h-3 mr-1" />
              {statusConfig.label}
            </Badge>
          </div>

          {/* Purpose Section */}
          <div className="mb-3">
            <p className="text-gray-700 text-sm leading-relaxed line-clamp-2 mb-2">{call.purpose}</p>
            <div className="bg-white/60 rounded-lg p-2">
              <p className="text-xs text-gray-800 font-medium line-clamp-2">{call.result}</p>
            </div>
          </div>

          {/* Metadata Section */}
          <div className="grid grid-cols-2 gap-2 text-xs text-gray-600 mb-3">
            <div className="flex items-center gap-1 truncate">
              <Calendar className="w-3 h-3 flex-shrink-0" />
              <span className="truncate">{date}</span>
            </div>
            <div className="flex items-center gap-1 truncate">
              <Clock className="w-3 h-3 flex-shrink-0" />
              <span className="truncate">{time}</span>
            </div>
            <div className="flex items-center gap-1 truncate">
              <Timer className="w-3 h-3 flex-shrink-0" />
              <span className="truncate">{call.duration || "00:00"}</span>
            </div>
            <div className="flex items-center gap-1 truncate">
              <Globe className="w-3 h-3 flex-shrink-0" />
              <span className="truncate">{call.language.toUpperCase()}</span>
            </div>
          </div>

          {/* Actions Section */}
          <div className="flex items-center justify-between gap-2">
            <div className="flex items-center gap-1">
              <Collapsible open={isExpanded} onOpenChange={() => setExpandedCall(isExpanded ? null : call.id)}>
                <CollapsibleTrigger asChild>
                  <Button
                    variant="ghost"
                    size="sm"
                    className="text-blue-600 hover:text-blue-700 hover:bg-blue-50 transition-all duration-200 h-7 px-2 text-xs"
                  >
                    {isExpanded ? <ChevronDown className="w-3 h-3" /> : <ChevronRight className="w-3 h-3" />}
                    <MessageCircle className="w-3 h-3 ml-1" />
                    <span className="ml-1 hidden sm:inline">Transcript</span>
                  </Button>
                </CollapsibleTrigger>
              </Collapsible>
              {(call.status === "successful" || call.status === "failed" || call.status === "needs_attention") && (
                <Button
                  variant="ghost"
                  size="sm"
                  className="text-purple-600 hover:text-purple-700 hover:bg-purple-50 transition-all duration-200 h-7 px-2 text-xs"
                  onClick={() => {
                    setActiveSection("make-call")
                    console.log(`Setting up a new call to ${call.business}`)
                  }}
                >
                  <RotateCcw className="w-3 h-3 mr-1" />
                  <span className="hidden sm:inline">Reschedule</span>
                </Button>
              )}
            </div>
          </div>

          {/* Transcript Section */}
          <Collapsible open={isExpanded} onOpenChange={() => setExpandedCall(isExpanded ? null : call.id)}>
            <CollapsibleContent className="mt-3 animate-in slide-in-from-top-2 duration-300">
              <div className="bg-white/80 rounded-xl p-3 border border-white/50 backdrop-blur-sm">
                <div className="flex items-center gap-2 mb-2">
                  <MessageCircle className="w-4 h-4 text-blue-500" />
                  <span className="font-medium text-gray-800 text-sm">Call Transcript</span>
                </div>
                <div className="space-y-2 max-h-48 overflow-y-auto">
                  {call.transcript.map((line: any, index: number) => (
                    <div key={index} className="flex gap-2">
                      <div
                        className={`w-2 h-2 rounded-full mt-1.5 flex-shrink-0 ${
                          line.speaker === "ai" ? "bg-blue-400" : "bg-green-400"
                        }`}
                      ></div>
                      <div className="flex-1 min-w-0">
                        <div
                          className={`inline-block px-2 py-1 rounded-lg text-xs leading-relaxed break-words ${
                            line.speaker === "ai"
                              ? "bg-blue-100 text-blue-800 rounded-bl-sm"
                              : "bg-green-100 text-green-800 rounded-br-sm"
                          }`}
                        >
                          {line.text}
                        </div>
                      </div>
                    </div>
                  ))}
                  {call.transcript.length === 0 && (
                    <div className="text-center py-3 text-gray-500">
                      <Coffee className="w-6 h-6 mx-auto mb-1 text-gray-300" />
                      <p className="text-xs">No transcript available yet</p>
                    </div>
                  )}
                </div>
              </div>
            </CollapsibleContent>
          </Collapsible>
        </CardContent>
      </Card>
    )
  }

  const renderHome = () => (
    <div className="space-y-6">
      {/* Search Section - Moved to top */}
      <div className="bg-white rounded-2xl p-4 border-2 border-gray-200 shadow-sm">
        <div className="flex items-center gap-3 mb-3">
          <Search className="w-5 h-5 text-gray-500" />
          <h3 className="text-lg font-semibold text-gray-800">Search Your Calls</h3>
          <span className="text-lg">🔍</span>
        </div>
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
          <Input
            placeholder="Search by business name or purpose..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="pl-10 bg-gray-50 border-gray-200 focus:border-purple-400 transition-colors"
          />
          {searchQuery && (
            <Button
              variant="ghost"
              size="sm"
              onClick={() => setSearchQuery("")}
              className="absolute right-2 top-1/2 transform -translate-y-1/2 h-6 w-6 p-0"
            >
              <X className="w-4 h-4" />
            </Button>
          )}
        </div>
      </div>

      {/* Welcome Section with Stats */}
      <div className="bg-gradient-to-r from-pink-100 via-purple-100 to-blue-100 rounded-2xl p-6 border border-purple-200">
        <div className="flex flex-col lg:flex-row lg:items-start lg:justify-between gap-4">
          <div className="flex items-center gap-4">
            <div className="w-16 h-16 bg-gradient-to-br from-purple-400 to-pink-500 rounded-2xl flex items-center justify-center flex-shrink-0">
              <span className="text-2xl">🐱</span>
            </div>
            <div>
              <h1 className="text-2xl font-bold text-gray-800 mb-1">Welcome back, Sarah! 🐾</h1>
              <p className="text-gray-600">Here's what I've accomplished for you</p>
            </div>
          </div>
          <div className="grid grid-cols-2 gap-3 lg:gap-4">
            <div className="bg-white/60 rounded-lg p-3 text-center">
              <div className="flex items-center justify-center gap-1 mb-1">
                <CheckCircle className="w-4 h-4 text-green-600" />
                <span className="text-xl lg:text-2xl font-bold text-green-800">24</span>
              </div>
              <p className="text-xs text-green-700">Calls Completed</p>
            </div>
            <div className="bg-white/60 rounded-lg p-3 text-center">
              <div className="flex items-center justify-center gap-1 mb-1">
                <Timer className="w-4 h-4 text-blue-600" />
                <span className="text-xl lg:text-2xl font-bold text-blue-800">2h 15m</span>
              </div>
              <p className="text-xs text-blue-700">Time Saved</p>
            </div>
            <div className="bg-white/60 rounded-lg p-3 text-center">
              <div className="flex items-center justify-center gap-1 mb-1">
                <Star className="w-4 h-4 text-amber-600" />
                <span className="text-xl lg:text-2xl font-bold text-amber-800">96%</span>
              </div>
              <p className="text-xs text-amber-700">Success Rate</p>
            </div>
            <div className="bg-white/60 rounded-lg p-3 text-center">
              <div className="flex items-center justify-center gap-1 mb-1">
                <Languages className="w-4 h-4 text-purple-600" />
                <span className="text-xl lg:text-2xl font-bold text-purple-800">5</span>
              </div>
              <p className="text-xs text-purple-700">Languages</p>
            </div>
          </div>
        </div>
      </div>

      {/* Scheduled Calls */}
      <div>
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl lg:text-2xl font-bold text-gray-800 flex items-center gap-3">
            <Clock className="w-6 h-6 lg:w-7 lg:h-7 text-purple-500" />
            Scheduled Calls
          </h2>
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => setScheduledPage(Math.max(0, scheduledPage - 1))}
              disabled={scheduledPage === 0}
              className="bg-white border-gray-200 h-8 w-8 p-0"
            >
              <ChevronLeft className="w-4 h-4" />
            </Button>
            <span className="text-sm text-gray-600 px-2 whitespace-nowrap">
              {scheduledPage + 1} of {Math.ceil(filteredScheduledCalls.length / 6) || 1}
            </span>
            <Button
              variant="outline"
              size="sm"
              onClick={() => setScheduledPage(scheduledPage + 1)}
              disabled={(scheduledPage + 1) * 6 >= filteredScheduledCalls.length}
              className="bg-white border-gray-200 h-8 w-8 p-0"
            >
              <ChevronRight className="w-4 h-4" />
            </Button>
          </div>
        </div>
        <div className="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-4">
          {filteredScheduledCalls.slice(scheduledPage * 6, (scheduledPage + 1) * 6).map((call) => (
            <CallCard key={call.id} call={call} />
          ))}
          {filteredScheduledCalls.length === 0 && (
            <div className="col-span-full text-center py-8 text-gray-500">
              <Clock className="w-12 h-12 mx-auto mb-2 text-gray-300" />
              <p>No scheduled calls found</p>
            </div>
          )}
        </div>
      </div>

      {/* Completed Calls */}
      <div>
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl lg:text-2xl font-bold text-gray-800 flex items-center gap-3">
            <CheckCircle className="w-6 h-6 lg:w-7 lg:h-7 text-green-500" />
            Completed Calls
          </h2>
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => setCompletedPage(Math.max(0, completedPage - 1))}
              disabled={completedPage === 0}
              className="bg-white border-gray-200 h-8 w-8 p-0"
            >
              <ChevronLeft className="w-4 h-4" />
            </Button>
            <span className="text-sm text-gray-600 px-2 whitespace-nowrap">
              {completedPage + 1} of {Math.ceil(filteredCompletedCalls.length / 6) || 1}
            </span>
            <Button
              variant="outline"
              size="sm"
              onClick={() => setCompletedPage(completedPage + 1)}
              disabled={(completedPage + 1) * 6 >= filteredCompletedCalls.length}
              className="bg-white border-gray-200 h-8 w-8 p-0"
            >
              <ChevronRight className="w-4 h-4" />
            </Button>
          </div>
        </div>
        <div className="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-4">
          {filteredCompletedCalls.slice(completedPage * 6, (completedPage + 1) * 6).map((call) => (
            <CallCard key={call.id} call={call} />
          ))}
          {filteredCompletedCalls.length === 0 && (
            <div className="col-span-full text-center py-8 text-gray-500">
              <CheckCircle className="w-12 h-12 mx-auto mb-2 text-gray-300" />
              <p>No completed calls found</p>
            </div>
          )}
        </div>
      </div>
    </div>
  )

  const renderMakeCall = () => (
    <div className="max-w-3xl mx-auto space-y-6">
      <div className="text-center mb-6">
        <div className="w-16 h-16 lg:w-20 lg:h-20 bg-gradient-to-br from-blue-400 to-purple-500 rounded-3xl flex items-center justify-center mx-auto mb-4">
          <Phone className="w-8 h-8 lg:w-10 lg:h-10 text-white" />
        </div>
        <h2 className="text-2xl lg:text-3xl font-bold text-gray-800 mb-2">Make a New Call</h2>
        <p className="text-gray-600 text-base lg:text-lg">
          Tell me who to call and what to say - I'll handle the rest!
        </p>
      </div>

      <Card className="bg-gradient-to-br from-blue-50 via-purple-50 to-pink-50 border-2 border-purple-200 shadow-xl">
        <CardHeader className="pb-4">
          <CardTitle className="text-xl lg:text-2xl text-purple-800 flex items-center gap-3">
            <Settings className="w-6 h-6 lg:w-7 lg:h-7" />
            Call Setup
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <Label className="text-sm font-semibold text-gray-700 mb-2 block">Who should I call?</Label>
              <div className="flex gap-2">
                <Input
                  placeholder="Business name or phone number"
                  className="flex-1 bg-white/80 border-gray-200 focus:border-purple-400 transition-colors"
                />
                <Button
                  variant="outline"
                  size="icon"
                  className="border-gray-200 hover:border-purple-400 bg-transparent flex-shrink-0"
                >
                  <Users className="h-4 w-4" />
                </Button>
              </div>
              <p className="text-xs text-gray-500 mt-1">Or choose from your contacts</p>
            </div>
            <div>
              <Label className="text-sm font-semibold text-gray-700 mb-2 block">Language</Label>
              <Select defaultValue="en">
                <SelectTrigger className="bg-white/80 border-gray-200 focus:border-purple-400">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {languages.map((lang) => (
                    <SelectItem key={lang.code} value={lang.code}>
                      <div className="flex items-center gap-2">
                        <Globe className="w-4 h-4" />
                        {lang.name}
                      </div>
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>

          <div>
            <Label className="text-sm font-semibold text-gray-700 mb-2 block">What should I say?</Label>
            <Textarea
              placeholder="Be as specific as possible! For example: 'Ask if they have gluten-free pizza options and what the price difference is' or 'Reschedule my appointment from Tuesday to Friday afternoon'"
              className="bg-white/80 border-gray-200 focus:border-purple-400 transition-colors min-h-[120px] resize-none"
              rows={5}
            />
          </div>

          <div>
            <Label className="text-sm font-semibold text-gray-700 mb-2 block">When to call?</Label>
            <Select defaultValue="now">
              <SelectTrigger className="bg-white/80 border-gray-200 focus:border-purple-400">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="now">
                  <div className="flex items-center gap-2">
                    <Zap className="w-4 h-4" />
                    Right now
                  </div>
                </SelectItem>
                <SelectItem value="30min">
                  <div className="flex items-center gap-2">
                    <Clock className="w-4 h-4" />
                    In 30 minutes
                  </div>
                </SelectItem>
                <SelectItem value="1hour">
                  <div className="flex items-center gap-2">
                    <Clock className="w-4 h-4" />
                    In 1 hour
                  </div>
                </SelectItem>
                <SelectItem value="custom">
                  <div className="flex items-center gap-2">
                    <Calendar className="w-4 h-4" />
                    Custom time
                  </div>
                </SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div className="bg-gradient-to-r from-blue-50 to-purple-50 rounded-xl p-4 lg:p-6 border border-blue-200">
            <h4 className="font-semibold text-blue-800 mb-3 flex items-center gap-2">
              <Sparkles className="w-5 h-5" />
              Pro Tips for Better Calls!
            </h4>
            <ul className="text-sm text-blue-700 space-y-2">
              <li className="flex items-start gap-2">
                <Star className="w-4 h-4 mt-0.5 flex-shrink-0" />
                <span>Be specific about what information you need</span>
              </li>
              <li className="flex items-start gap-2">
                <MessageCircle className="w-4 h-4 mt-0.5 flex-shrink-0" />
                <span>Mention any reference numbers or account details</span>
              </li>
              <li className="flex items-start gap-2">
                <Activity className="w-4 h-4 mt-0.5 flex-shrink-0" />
                <span>Let me know if there are multiple options to ask about</span>
              </li>
              <li className="flex items-start gap-2">
                <Bell className="w-4 h-4 mt-0.5 flex-shrink-0" />
                <span>I'll send you a detailed summary when the call is complete!</span>
              </li>
            </ul>
          </div>

          <Button className="w-full bg-gradient-to-r from-blue-500 to-purple-600 hover:from-blue-600 hover:to-purple-700 text-white font-semibold py-3 lg:py-4 text-base lg:text-lg shadow-lg hover:shadow-xl transition-all duration-300">
            <Send className="w-5 h-5 mr-2" />
            Submit Call Request
          </Button>
        </CardContent>
      </Card>

      {/* Quick Templates */}
      <Card className="bg-gradient-to-br from-yellow-50 to-orange-50 border-2 border-orange-200">
        <CardHeader>
          <CardTitle className="text-lg lg:text-xl text-orange-800 flex items-center gap-2">
            <Zap className="w-5 h-5 lg:w-6 lg:h-6" />
            Quick Templates
            <Badge className="bg-orange-200 text-orange-700 text-xs">Popular!</Badge>
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {[
              {
                title: "Restaurant Reservation",
                description: "Book a table for dinner",
                icon: Utensils,
                color: "from-red-100 to-pink-100 border-red-200",
                template: "Make a reservation for [number] people on [date] at [time]",
              },
              {
                title: "Appointment Reschedule",
                description: "Change existing appointment",
                icon: Calendar,
                color: "from-blue-100 to-cyan-100 border-blue-200",
                template: "Reschedule my appointment from [old date] to [new date]",
              },
              {
                title: "Service Inquiry",
                description: "Ask about services or pricing",
                icon: MessageCircle,
                color: "from-green-100 to-emerald-100 border-green-200",
                template: "Ask about [service] availability and pricing",
              },
              {
                title: "Order Status Check",
                description: "Check on existing order",
                icon: Search,
                color: "from-purple-100 to-pink-100 border-purple-200",
                template: "Check status of order #[order number]",
              },
              {
                title: "Complaint Resolution",
                description: "Address an issue",
                icon: AlertCircle,
                color: "from-amber-100 to-yellow-100 border-amber-200",
                template: "Report an issue with [problem] and request resolution",
              },
              {
                title: "Information Request",
                description: "Get specific details",
                icon: Star,
                color: "from-indigo-100 to-blue-100 border-indigo-200",
                template: "Ask about [specific information needed]",
              },
            ].map((template, index) => (
              <div
                key={index}
                className={`p-4 bg-gradient-to-br ${template.color} rounded-xl border-2 hover:shadow-lg cursor-pointer transition-all duration-300 hover:scale-105 group`}
                onClick={() => {
                  console.log(`${template.title} template loaded`)
                }}
              >
                <div className="w-10 h-10 bg-white/60 rounded-lg flex items-center justify-center mb-3 group-hover:scale-110 transition-transform duration-200">
                  <template.icon className="w-5 h-5 text-gray-600" />
                </div>
                <h4 className="font-semibold text-gray-800 mb-2 text-sm lg:text-base">{template.title}</h4>
                <p className="text-xs lg:text-sm text-gray-600 mb-3">{template.description}</p>
                <p className="text-xs text-gray-500 bg-white/60 rounded-lg p-2 break-words">{template.template}</p>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  )

  const renderContacts = () => (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-xl lg:text-2xl font-bold text-gray-800 flex items-center gap-3">
            <Users className="w-6 h-6 lg:w-7 lg:h-7 text-purple-500" />
            My Contacts
          </h2>
          <p className="text-gray-600 mt-1">Your personal contact book for quick calling</p>
        </div>
        <Button className="bg-gradient-to-r from-purple-500 to-pink-600 hover:from-purple-600 hover:to-pink-700 text-white font-semibold shadow-lg">
          <Plus className="w-4 w-4 mr-2" />
          Add Contact
        </Button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {[
          {
            name: "Pizza Palace",
            phone: "+1-555-PIZZA",
            category: "Food",
            calls: 3,
            icon: Utensils,
            color: "from-red-100 to-orange-100 border-red-200",
          },
          {
            name: "Dr. Smith",
            phone: "+1-555-TEETH",
            category: "Healthcare",
            calls: 2,
            icon: Stethoscope,
            color: "from-blue-100 to-cyan-100 border-blue-200",
          },
          {
            name: "Sakura Sushi",
            phone: "+1-555-SUSHI",
            category: "Food",
            calls: 1,
            icon: Utensils,
            color: "from-green-100 to-emerald-100 border-green-200",
          },
          {
            name: "City Insurance",
            phone: "+1-555-CLAIM",
            category: "Finance",
            calls: 4,
            icon: Shield,
            color: "from-purple-100 to-pink-100 border-purple-200",
          },
          {
            name: "Sunny Apartments",
            phone: "+1-555-RENT",
            category: "Housing",
            calls: 1,
            icon: Building,
            color: "from-amber-100 to-yellow-100 border-amber-200",
          },
          {
            name: "Fluffy Vet",
            phone: "+1-555-PETS",
            category: "Pets",
            calls: 2,
            icon: Heart,
            color: "from-pink-100 to-rose-100 border-pink-200",
          },
        ].map((contact, index) => (
          <Card
            key={index}
            className={`bg-gradient-to-br ${contact.color} border-2 hover:shadow-lg transition-all duration-300 hover:scale-105 group`}
          >
            <CardContent className="p-4 lg:p-6">
              <div className="flex items-center gap-3 lg:gap-4 mb-4">
                <div className="w-10 h-10 lg:w-12 lg:h-12 bg-white/60 rounded-lg flex items-center justify-center group-hover:scale-110 transition-transform duration-200 flex-shrink-0">
                  <contact.icon className="w-5 h-5 lg:w-6 lg:h-6 text-gray-600" />
                </div>
                <div className="flex-1 min-w-0">
                  <h3 className="font-semibold text-gray-800 text-base lg:text-lg truncate">{contact.name}</h3>
                  <p className="text-sm text-gray-600 truncate">{contact.phone}</p>
                </div>
              </div>
              <div className="flex items-center justify-between gap-2">
                <div className="flex items-center gap-2 lg:gap-3 min-w-0">
                  <Badge variant="secondary" className="bg-white/70 text-gray-700 text-xs">
                    {contact.category}
                  </Badge>
                  <span className="text-xs text-gray-600 whitespace-nowrap">{contact.calls} calls</span>
                </div>
                <Button
                  size="sm"
                  className="bg-gradient-to-r from-blue-500 to-purple-600 hover:from-blue-600 hover:to-purple-700 text-white font-medium shadow-md flex-shrink-0"
                  onClick={() => {
                    setActiveSection("make-call")
                    console.log(`Setting up call to ${contact.name}`)
                  }}
                >
                  <Phone className="w-4 h-4 mr-1" />
                  Call
                </Button>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  )

  return (
    <div className="flex flex-col h-screen bg-gradient-to-br from-pink-50 via-purple-50 to-blue-50">
      {/* Header */}
      <header className="bg-white/90 backdrop-blur-sm border-b-2 border-purple-200 flex items-center justify-between px-4 lg:px-6 py-4 shadow-sm">
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-gradient-to-br from-purple-400 to-pink-500 rounded-xl flex items-center justify-center shadow-lg">
              <span className="text-lg">🐱</span>
            </div>
            <div>
              <h1 className="text-lg lg:text-xl font-bold bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent">
                CallCat
              </h1>
              <p className="text-xs text-gray-600">AI Phone Assistant</p>
            </div>
          </div>
          {!isMobile && (
            <Button
              variant="ghost"
              size="sm"
              onClick={() => setSidebarOpen(!sidebarOpen)}
              className="ml-4 hover:bg-purple-100 transition-colors duration-200"
            >
              <Menu className="h-4 w-4 text-purple-600" />
            </Button>
          )}
        </div>
        <div className="flex items-center gap-2 lg:gap-4">
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button
                variant="outline"
                size="sm"
                className="bg-white/80 border-purple-200 hover:border-purple-400 transition-colors"
              >
                <Globe className="h-4 w-4 mr-2" />
                <span className="hidden md:inline">English</span>
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              {languages.map((lang) => (
                <DropdownMenuItem key={lang.code} onClick={() => setSelectedLanguage(lang.code)}>
                  {lang.flag} {lang.name}
                </DropdownMenuItem>
              ))}
            </DropdownMenuContent>
          </DropdownMenu>

          <Button variant="ghost" size="icon" className="relative hover:bg-purple-100 transition-colors duration-200">
            <Bell className="h-5 w-5 text-purple-600" />
            <span className="absolute top-0 right-0 h-2 w-2 bg-pink-500 rounded-full animate-pulse"></span>
          </Button>

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

      <div className="flex flex-1 overflow-hidden">
        {/* Sidebar */}
        <div
          className={`${
            sidebarOpen ? "w-64" : "w-0"
          } bg-white/90 backdrop-blur-sm border-r-2 border-purple-200 transition-all duration-500 ease-in-out overflow-hidden shadow-lg`}
        >
          <div className="p-4 lg:p-6 h-full flex flex-col">
            <nav className="space-y-2 flex-1">
              <Button
                variant="ghost"
                className={`w-full justify-start text-left transition-all duration-300 ${
                  activeSection === "home"
                    ? "bg-gradient-to-r from-purple-100 to-pink-100 text-purple-700 shadow-md"
                    : "text-gray-600 hover:text-purple-600 hover:bg-purple-50"
                }`}
                onClick={() => setActiveSection("home")}
              >
                <Home className="h-5 w-5 mr-3" />
                Dashboard
              </Button>
              <Button
                variant="ghost"
                className={`w-full justify-start text-left transition-all duration-300 ${
                  activeSection === "make-call"
                    ? "bg-gradient-to-r from-purple-100 to-pink-100 text-purple-700 shadow-md"
                    : "text-gray-600 hover:text-purple-600 hover:bg-purple-50"
                }`}
                onClick={() => setActiveSection("make-call")}
              >
                <Phone className="h-5 w-5 mr-3" />
                Make a Call
              </Button>
              <Button
                variant="ghost"
                className={`w-full justify-start text-left transition-all duration-300 ${
                  activeSection === "active"
                    ? "bg-gradient-to-r from-purple-100 to-pink-100 text-purple-700 shadow-md"
                    : "text-gray-600 hover:text-purple-600 hover:bg-purple-50"
                }`}
                onClick={() => setActiveSection("active")}
              >
                <div className="relative mr-3">
                  <PhoneCall className="h-5 w-5" />
                  <div className="absolute -top-1 -right-1 w-2 h-2 bg-green-400 rounded-full animate-pulse"></div>
                </div>
                Active Calls
              </Button>
              <Button
                variant="ghost"
                className={`w-full justify-start text-left transition-all duration-300 ${
                  activeSection === "contacts"
                    ? "bg-gradient-to-r from-purple-100 to-pink-100 text-purple-700 shadow-md"
                    : "text-gray-600 hover:text-purple-600 hover:bg-purple-50"
                }`}
                onClick={() => setActiveSection("contacts")}
              >
                <Users className="h-5 w-5 mr-3" />
                Contacts
              </Button>
              <Button
                variant="ghost"
                className={`w-full justify-start text-left transition-all duration-300 ${
                  activeSection === "settings"
                    ? "bg-gradient-to-r from-purple-100 to-pink-100 text-purple-700 shadow-md"
                    : "text-gray-600 hover:text-purple-600 hover:bg-purple-50"
                }`}
                onClick={() => setActiveSection("settings")}
              >
                <Settings className="h-5 w-5 mr-3" />
                Settings
              </Button>
            </nav>
          </div>
        </div>

        {/* Main Content */}
        <main className="flex-1 overflow-y-auto p-4 lg:p-6">
          {activeSection === "home" && renderHome()}
          {activeSection === "make-call" && renderMakeCall()}
          {activeSection === "contacts" && renderContacts()}
          {(activeSection === "active" || activeSection === "settings") && (
            <div className="flex items-center justify-center h-full">
              <Card className="w-full max-w-md text-center bg-white/80 backdrop-blur-sm border-2 border-purple-200 shadow-xl">
                <CardContent className="p-6 lg:p-8">
                  <div className="w-16 h-16 bg-gradient-to-br from-purple-400 to-pink-500 rounded-2xl flex items-center justify-center mx-auto mb-4">
                    <Settings className="w-8 h-8 text-white" />
                  </div>
                  <h3 className="text-xl lg:text-2xl font-bold mb-2 text-gray-800">Coming Soon!</h3>
                  <p className="text-gray-600 mb-6">
                    This section is being built with lots of love and attention to detail!
                  </p>
                  <Button
                    onClick={() => setActiveSection("home")}
                    className="bg-gradient-to-r from-purple-500 to-pink-600 hover:from-purple-600 hover:to-pink-700 text-white font-semibold shadow-lg"
                  >
                    <Heart className="w-4 h-4 mr-2" />
                    Back to Home
                  </Button>
                </CardContent>
              </Card>
            </div>
          )}
        </main>
      </div>

      {/* Mobile Menu Button */}
      {isMobile && (
        <Button
          variant="outline"
          size="icon"
          className="fixed bottom-6 right-6 z-50 rounded-full h-14 w-14 shadow-xl bg-white border-2 border-purple-200 hover:border-purple-400 transition-all duration-300"
          onClick={() => setSidebarOpen(!sidebarOpen)}
        >
          <Menu className="h-6 w-6 text-purple-600" />
        </Button>
      )}
    </div>
  )
}
