"use client"

import { Button } from "@/components/ui/button"
import { Phone } from "lucide-react"
import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"

const phrases = ["Business Calls", "Boring Calls", "Awkward Calls", "Stressful Calls", "Tedious Calls"]

export default function LandingPage() {
  const [currentPhrase, setCurrentPhrase] = useState(0)
  const router = useRouter()

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

  return (
    <div className="min-h-screen relative">
      <div
        className="fixed inset-0 bg-cover bg-center bg-no-repeat"
        style={{
          backgroundImage:
            "url('https://hebbkx1anhila5yf.public.blob.vercel-storage.com/cafe.jpg-K3GSFZVrcvSAeQPabPkF0tdc2dPuJK.jpeg')",
        }}
      />

      <div className="fixed inset-0 bg-black/20" />

      <div className="relative z-10">
        {/* Header */}
        <header className="backdrop-blur-sm bg-white/5 border-b border-white/20 sticky top-0 z-50">
          <div className="container mx-auto px-4 py-4 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 bg-amber-600 rounded-full flex items-center justify-center">
                <Phone className="w-4 h-4 text-white" />
              </div>
              <span className="text-xl font-bold text-white">CallCat</span>
            </div>
            <div className="flex items-center gap-3">
              <Button
                variant="ghost"
                className="text-white hover:bg-white/20 hover:text-white"
                onClick={handleSignIn}
              >
                Sign In
              </Button>
              <Button
                className="bg-amber-600 hover:bg-amber-700 text-white"
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
                    className="text-amber-400 inline-block animate-in slide-in-from-top-2 fade-in duration-700"
                  >
                    {phrases[currentPhrase]}
                  </span>
                </h1>
                <p className="text-xl text-white/90 mb-8 text-pretty max-w-2xl mx-auto leading-relaxed">
                  Schedule calls to any business and let our friendly AI assistant handle the conversation for you. From
                  restaurant reservations to appointment bookings, CallCat makes it cozy and simple.
                </p>
                <div className="flex flex-col sm:flex-row gap-4 justify-center items-center max-w-2xl mx-auto">
                  <Button
                    size="lg"
                    className="bg-amber-600 hover:bg-amber-700 text-white px-8 py-3 text-lg shadow-lg w-full sm:w-auto"
                    onClick={handleSignUp}
                  >
                    Make an Account
                  </Button>
                  <div className="flex gap-2 items-center w-full sm:w-auto">
                    <input
                      type="tel"
                      placeholder="Phone (optional for demo)"
                      className="px-4 py-3 rounded-lg bg-white/10 border border-white/30 text-white placeholder-white/70 backdrop-blur-sm focus:outline-none focus:ring-2 focus:ring-amber-400 flex-1 min-w-0"
                    />
                    <Button
                      variant="outline"
                      size="lg"
                      className="px-6 py-3 text-lg border-white/30 hover:bg-white/20 bg-transparent text-white whitespace-nowrap"
                    >
                      Get Demo
                    </Button>
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
                  I built CallCat because I noticed how many people (including myself sometimes) feel anxious about
                  making phone calls to businesses. Whether it&apos;s booking a restaurant reservation or scheduling an
                  appointment, these simple tasks can feel overwhelming. So I created a friendly AI assistant that
                  handles these conversations with the warmth and care you&apos;d want from a helpful friend.
                </p>
              </div>
            </div>
          </div>
        </section>

        <footer className="backdrop-blur-sm bg-white/3 border-t border-white/20 py-8 px-4">
          <div className="container mx-auto">
            <div className="flex flex-col md:flex-row items-center justify-between gap-4">
              <div className="flex items-center gap-2">
                <div className="w-8 h-8 bg-amber-600 rounded-full flex items-center justify-center">
                  <Phone className="w-4 h-4 text-white" />
                </div>
                <span className="text-xl font-bold text-white">CallCat</span>
              </div>

              <div className="flex gap-6 text-sm">
                <a href="#" className="text-white/70 hover:text-white transition-colors">
                  Help Center
                </a>
                <a href="#" className="text-white/70 hover:text-white transition-colors">
                  Contact Us
                </a>
              </div>
            </div>

            <div className="text-center mt-6 pt-6 border-t border-white/20">
              <p className="text-sm text-white/70">© 2024 CallCat. Made with ❤️ for stress-free calling.</p>
            </div>
          </div>
        </footer>
      </div>
    </div>
  )
}