"use client"

import { Button } from "@/components/ui/button"
import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import Image from "next/image"
import { apiService } from "@/lib/api"
import { Phone } from "lucide-react"

const phrases = ["Business Calls", "Routine Calls", "Time-Consuming Calls", "Repetitive Tasks", "Daily Errands"]

export default function LandingPage() {
  const [currentPhraseIndex, setCurrentPhraseIndex] = useState(0)
  const [isTransitioning, setIsTransitioning] = useState(false)
  const router = useRouter()
  const [phoneNumber, setPhoneNumber] = useState("")
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState("")
  const [success, setSuccess] = useState(false)

  useEffect(() => {
    const interval = setInterval(() => {
      setIsTransitioning(true)
      setTimeout(() => {
        setCurrentPhraseIndex((prev) => (prev + 1) % phrases.length)
        setIsTransitioning(false)
      }, 500) // Wait for fade out before changing text
    }, 4000)
    return () => clearInterval(interval)
  }, [])

  const handleSignIn = () => {
    router.push('/login')
  }

  const handleSignUp = () => {
    router.push('/auth')
  }

  const handleDemoCall = async () => {
    setError("")
    setSuccess(false)

    if (!phoneNumber.trim()) {
      setError("Please enter a phone number")
      return
    }

    const hasDigits = /\d/.test(phoneNumber)
    if (!hasDigits) {
      setError("Please enter a valid phone number")
      return
    }

    setLoading(true)

    try {
      await apiService.createDemoCall(phoneNumber)
      setSuccess(true)
      setPhoneNumber("")
      setTimeout(() => setSuccess(false), 5000)
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to create demo call")
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen relative font-sans text-white selection:bg-indigo-500/30">
      {/* Background Image with Overlay */}
      <div className="fixed inset-0 z-0">
        <Image
          src="/business-cats.jpeg"
          alt="Background"
          fill
          className="object-cover object-center"
          priority
        />
        {/* Gradient Overlay for Readability */}
        <div className="absolute inset-0 bg-gradient-to-b from-black/70 via-black/50 to-black/80 backdrop-blur-[2px]" />
      </div>

      <div className="relative z-10 flex flex-col min-h-screen">
        {/* Sticky Header */}
        <header className="sticky top-0 z-50 w-full border-b border-white/10 bg-black/20 backdrop-blur-md transition-all duration-300">
          <div className="container mx-auto px-6 h-20 flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="bg-white/10 p-2 rounded-xl backdrop-blur-sm border border-white/10">
                <Image
                  src="/logo.png"
                  alt="CallCat Logo"
                  width={32}
                  height={32}
                  className="w-8 h-8 drop-shadow-md"
                />
              </div>
              <span className="text-2xl font-bold tracking-tight text-white">CallCat</span>
            </div>
            <div className="flex items-center gap-4">
              <Button
                variant="ghost"
                className="text-white/90 hover:text-white hover:bg-white/10 font-medium transition-colors"
                onClick={handleSignIn}
              >
                Log In
              </Button>
              <Button
                className="bg-indigo-600 hover:bg-indigo-500 text-white font-semibold shadow-lg shadow-indigo-900/20 border border-indigo-500/50 transition-all hover:scale-105"
                onClick={handleSignUp}
              >
                Sign Up
              </Button>
            </div>
          </div>
        </header>

        {/* Hero Section */}
        <main className="flex-grow flex items-center justify-center px-6 py-20">
          <div className="container mx-auto max-w-5xl">
            <div className="flex flex-col items-center text-center space-y-12">
              
              {/* Main Heading with Smooth Transition */}
              <div className="space-y-6 max-w-4xl">
                <h1 className="text-5xl md:text-7xl font-bold tracking-tight leading-[1.1] drop-shadow-xl">
                  Let CallCat Handle Your{" "}
                  <span className="block h-[1.2em] overflow-hidden">
                    <span 
                      className={`block transition-all duration-500 transform ${
                        isTransitioning ? 'opacity-0 translate-y-8' : 'opacity-100 translate-y-0'
                      } text-transparent bg-clip-text bg-gradient-to-r from-indigo-300 to-white`}
                    >
                      {phrases[currentPhraseIndex]}
                    </span>
                  </span>
                </h1>
                
                <p className="text-xl md:text-2xl text-white/80 leading-relaxed max-w-2xl mx-auto font-light drop-shadow-md">
                  Save time and boost your productivity. From restaurant reservations to appointment bookings, CallCat gets things done efficiently.
                </p>
              </div>

              {/* Demo Call Input */}
              <div className="w-full max-w-lg space-y-4">
                <div className="flex flex-col sm:flex-row gap-3 p-2 bg-white/5 backdrop-blur-xl border border-white/10 rounded-2xl shadow-2xl">
                  <input
                    type="tel"
                    placeholder="Enter your phone number"
                    value={phoneNumber}
                    onChange={(e) => setPhoneNumber(e.target.value)}
                    disabled={loading}
                    className="flex-1 px-6 py-4 rounded-xl bg-transparent text-white placeholder-white/40 focus:outline-none focus:bg-white/5 transition-all text-lg"
                  />
                  <Button
                    size="lg"
                    onClick={handleDemoCall}
                    disabled={loading}
                    className="h-auto py-4 px-8 bg-white text-indigo-900 hover:bg-indigo-50 text-lg font-bold rounded-xl shadow-lg transition-all hover:scale-[1.02] active:scale-[0.98]"
                  >
                    {loading ? "Calling..." : "Get Demo"}
                    {!loading && <Phone className="ml-2 h-5 w-5" />}
                  </Button>
                </div>

                {/* Status Messages */}
                {(error || success) && (
                  <div className={`p-4 rounded-xl backdrop-blur-md border shadow-lg animate-in fade-in slide-in-from-top-2 ${
                    error 
                      ? 'bg-red-500/20 border-red-500/30 text-red-100' 
                      : 'bg-green-500/20 border-green-500/30 text-green-100'
                  }`}>
                    <p className="font-medium text-center">
                      {error || "Demo call sent! You should receive it shortly."}
                    </p>
                  </div>
                )}
              </div>
            </div>
          </div>
        </main>

        {/* Footer - Minimal */}
        <footer className="w-full border-t border-white/10 bg-black/40 backdrop-blur-sm py-8">
          <div className="container mx-auto px-6 flex flex-col md:flex-row items-center justify-between gap-4">
            <div className="flex items-center gap-2 opacity-70 hover:opacity-100 transition-opacity">
              <Image
                src="/logo.png"
                alt="CallCat Logo"
                width={24}
                height={24}
                className="w-6 h-6 grayscale brightness-200"
              />
              <span className="font-semibold tracking-wide">CallCat</span>
            </div>
            <p className="text-sm text-white/50">
              © 2024 CallCat. Built by David at Duke.
            </p>
          </div>
        </footer>
      </div>
    </div>
  )
}
