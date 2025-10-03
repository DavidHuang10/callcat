"use client"

import { Button } from "@/components/ui/button"
import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import Image from "next/image"
import { apiService } from "@/lib/api"

const phrases = ["Business Calls", "Routine Calls", "Time-Consuming Calls", "Repetitive Tasks", "Daily Errands"]

export default function LandingPage() {
  const [currentPhrase, setCurrentPhrase] = useState(0)
  const router = useRouter()
  const [phoneNumber, setPhoneNumber] = useState("")
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState("")
  const [success, setSuccess] = useState(false)

  useEffect(() => {
    const interval = setInterval(() => {
      setCurrentPhrase((prev) => (prev + 1) % phrases.length)
    }, 5000)
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

    // Basic validation - must contain at least some digits
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
    <div className="min-h-screen relative">
      <div
        className="fixed inset-0 bg-cover bg-center bg-no-repeat"
        style={{
          backgroundImage: "url('/business-cats.jpeg')",
        }}
      />

      <div className="fixed inset-0 bg-black/40" />

      <div className="relative z-10">
        {/* Header */}
        <header className="backdrop-blur-sm bg-white/5 border-b border-white/20 sticky top-0 z-50">
          <div className="container mx-auto px-4 py-4 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Image
                src="/logo.png"
                alt="CallCat Logo"
                width={40}
                height={40}
                className="drop-shadow-lg"
              />
              <span className="text-xl font-bold text-white">CallCat</span>
            </div>
            <div className="flex items-center gap-3">
              <Button
                variant="ghost"
                className="text-white hover:bg-white/20 hover:text-white"
                onClick={handleSignIn}
              >
                Log In
              </Button>
              <Button
                className="bg-blue-800 hover:bg-blue-900 text-white"
                onClick={handleSignUp}
              >
                Sign Up
              </Button>
            </div>
          </div>
        </header>

        {/* Hero Section */}
        <section className="py-20 px-4 text-center">
          <div className="container mx-auto">
            <div className="max-w-4xl mx-auto">
              <div className="backdrop-blur-sm bg-white/3 rounded-2xl p-8 border border-white/20">
                <h1 className="text-4xl md:text-6xl font-bold mb-6 text-balance leading-tight text-white">
                  Let CallCat Handle Your{" "}
                  <span
                    key={currentPhrase}
                    className="text-blue-400 inline-block animate-in slide-in-from-top-2 fade-in duration-700"
                  >
                    {phrases[currentPhrase]}
                  </span>
                </h1>
                <p className="text-xl text-white/90 mb-8 text-pretty max-w-2xl mx-auto leading-relaxed">
                  Save time and boost your productivity by letting our AI assistant handle business calls for you. From
                  restaurant reservations to appointment bookings, CallCat gets things done efficiently while you focus on what matters most.
                </p>
                <div className="flex flex-col sm:flex-row gap-4 justify-center items-center max-w-2xl mx-auto">
                  <Button
                    size="lg"
                    className="bg-amber-600 hover:bg-amber-700 text-white px-8 py-3 text-lg shadow-lg w-full sm:w-auto"
                    onClick={handleSignUp}
                  >
                    Make an Account
                  </Button>
                  <div className="flex flex-col gap-2 w-full sm:w-auto">
                    <div className="flex gap-2 items-center w-full sm:w-auto">
                      <input
                        type="tel"
                        placeholder="Enter your phone number"
                        value={phoneNumber}
                        onChange={(e) => setPhoneNumber(e.target.value)}
                        disabled={loading}
                        className="px-4 py-3 rounded-lg bg-white/10 border border-white/30 text-white placeholder-white/70 backdrop-blur-sm focus:outline-none focus:ring-2 focus:ring-blue-400 flex-1 min-w-0 disabled:opacity-50"
                      />
                      <Button
                        variant="outline"
                        size="lg"
                        onClick={handleDemoCall}
                        disabled={loading}
                        className="px-6 py-3 text-lg border-white/30 hover:bg-white/20 bg-transparent text-white whitespace-nowrap disabled:opacity-50"
                      >
                        {loading ? "Calling..." : "Get Demo"}
                      </Button>
                    </div>
                    {error && (
                      <p className="text-sm text-red-400 backdrop-blur-sm bg-red-900/20 px-3 py-2 rounded">
                        {error}
                      </p>
                    )}
                    {success && (
                      <p className="text-sm text-green-400 backdrop-blur-sm bg-green-900/20 px-3 py-2 rounded">
                        Demo call sent! You should receive it shortly.
                      </p>
                    )}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* About David Section */}
        <section className="py-20 px-4 text-center">
          <div className="container mx-auto">
            <div className="max-w-3xl mx-auto">
              <div className="backdrop-blur-sm bg-white/3 rounded-2xl p-8 border border-white/20">
                <h2 className="text-3xl md:text-4xl font-bold mb-6 text-balance text-white">About David</h2>
                <p className="text-xl text-white/90 mb-6 text-pretty leading-relaxed">
                  Hi! I&apos;m David, the creator of CallCat. I&apos;m currently studying Computer Science and Mathematics at Duke
                  University, where I spend most of my time diving deep into algorithms, machine learning, and building
                  cool projects like this one.
                </p>
                <p className="text-lg text-white/80 text-pretty leading-relaxed">
                  I built CallCat because I realized how much time we spend on routine phone calls that could be automated.
                  Whether it&apos;s booking a restaurant reservation or scheduling an appointment, these tasks take valuable time
                  away from more important work. So I created an intelligent AI assistant that handles these conversations
                  efficiently and professionally, giving you back hours in your week.
                </p>
              </div>
            </div>
          </div>
        </section>

        <footer className="backdrop-blur-sm bg-white/3 border-t border-white/20 py-8 px-4">
          <div className="container mx-auto">
            <div className="flex flex-col md:flex-row items-center justify-between gap-4">
              <div className="flex items-center gap-2">
                <Image
                  src="/logo.png"
                  alt="CallCat Logo"
                  width={32}
                  height={32}
                  className="drop-shadow-lg"
                />
                <span className="text-xl font-bold text-white">CallCat</span>
              </div>
            </div>

            <div className="text-center mt-6 pt-6 border-t border-white/20">
              <p className="text-sm text-white/70">© 2024 CallCat. Made with ❤️ for efficient, time-saving automation.</p>
            </div>
          </div>
        </footer>
      </div>
    </div>
  )
}