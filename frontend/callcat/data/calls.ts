import {
  Utensils,
  Stethoscope,
  Building,
  Shield,
} from "lucide-react"
import { CallRecord, StatusConfig } from "@/types"

export const allCalls: CallRecord[] = [
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
    timestamp: "2024-01-15T09:45:00Z",
    duration: "4:12",
    language: "en",
    transcript: [
      { speaker: "business", text: "City Insurance, claims department. How can I help you?" },
      { speaker: "ai", text: "Hi, I'm calling to check the status of claim number INS-789456." },
      { speaker: "business", text: "Let me look that up for you. One moment please." },
      { speaker: "business", text: "I can see your claim was submitted on January 10th. It's currently under review." },
      { speaker: "ai", text: "How long does the review process typically take?" },
      { speaker: "business", text: "Usually 5-7 business days. Your claim should be processed by next Tuesday." },
      { speaker: "ai", text: "Great, thank you. Can you also confirm the coverage amount?" },
      { speaker: "business", text: "I can see you have comprehensive coverage up to $50,000." },
      { speaker: "ai", text: "Perfect. Thank you for your help!" },
    ],
    result: "Claim status confirmed: Under review, expected completion by Tuesday. Coverage: $50,000.",
    icon: Shield,
  },
  {
    id: "CC-005",
    business: "TechCorp Solutions",
    phone: "+1-555-TECH",
    purpose: "Schedule IT consultation for new office setup",
    status: "scheduled",
    timestamp: "2024-01-16T10:00:00Z",
    duration: null,
    language: "en",
    transcript: [
      { speaker: "business", text: "TechCorp Solutions, this is Mike. How can I assist you today?" },
      { speaker: "ai", text: "Hi Mike, I need to schedule an IT consultation for setting up a new office." },
      { speaker: "business", text: "Absolutely! We offer office setup consultations. What size office are we talking about?" },
      { speaker: "ai", text: "It's a 15-person office, about 2,500 square feet." },
      { speaker: "business", text: "Perfect. We have consultants available next week. What day works best for you?" },
      { speaker: "ai", text: "Wednesday or Thursday would be ideal." },
      { speaker: "business", text: "I have Wednesday at 2 PM or Thursday at 10 AM available." },
      { speaker: "ai", text: "Wednesday at 2 PM works great." },
      { speaker: "business", text: "Excellent! I'll send you a calendar invite and confirmation email." },
    ],
    result: "IT consultation scheduled for Wednesday 2 PM. Confirmation email sent.",
    icon: Building,
  },
]

export const getStatusConfig = (status: string): StatusConfig => {
  const configs: Record<string, StatusConfig> = {
    successful: {
      label: "Successful",
      color: "bg-green-100 text-green-800 border-green-200",
      bgGradient: "from-green-50 to-emerald-50",
      icon: "✓",
    },
    failed: {
      label: "Failed",
      color: "bg-red-100 text-red-800 border-red-200",
      bgGradient: "from-red-50 to-pink-50",
      icon: "✗",
    },
    scheduled: {
      label: "Scheduled",
      color: "bg-blue-100 text-blue-800 border-blue-200",
      bgGradient: "from-blue-50 to-cyan-50",
      icon: "⏰",
    },
    needs_attention: {
      label: "Needs Attention",
      color: "bg-yellow-100 text-yellow-800 border-yellow-200",
      bgGradient: "from-yellow-50 to-orange-50",
      icon: "⚠",
    },
  }
  return configs[status] || configs.failed
}

export const formatTimestamp = (timestamp: string) => {
  const date = new Date(timestamp)
  return {
    date: date.toLocaleDateString("en-US", { month: "short", day: "numeric" }),
    time: date.toLocaleTimeString("en-US", { hour: "numeric", minute: "2-digit" }),
  }
}

export const getFilteredCalls = (searchQuery: string) => {
  const scheduledCalls = allCalls.filter((call) => call.status === "scheduled")
  const completedCalls = allCalls.filter((call) => call.status !== "scheduled")

  const filteredScheduledCalls = scheduledCalls.filter(
    (call) =>
      call.business.toLowerCase().includes(searchQuery.toLowerCase()) ||
      call.purpose.toLowerCase().includes(searchQuery.toLowerCase()) ||
      call.id.toLowerCase().includes(searchQuery.toLowerCase())
  )

  const filteredCompletedCalls = completedCalls.filter(
    (call) =>
      call.business.toLowerCase().includes(searchQuery.toLowerCase()) ||
      call.purpose.toLowerCase().includes(searchQuery.toLowerCase()) ||
      call.id.toLowerCase().includes(searchQuery.toLowerCase())
  )

  return { scheduledCalls: filteredScheduledCalls, completedCalls: filteredCompletedCalls }
}
