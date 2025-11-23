'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Mail, Lock, AlertCircle, ArrowLeft } from 'lucide-react'
import { useAuth } from '@/contexts/AuthContext'
import { apiService } from '@/lib/api'
import Image from 'next/image'

export default function LoginPage() {
  const router = useRouter()
  const { login } = useAuth()
  const [formData, setFormData] = useState({
    email: '',
    password: ''
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError('')
    
    try {
      const data = await apiService.login(formData.email, formData.password)
      login({
        id: data.userId,
        email: data.email,
        fullName: data.fullName
      }, data.token)
      router.replace('/')
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Login failed. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  const updateFormData = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }))
  }

  return (
    <div className="min-h-screen relative font-sans text-white">
      {/* Background Image with Overlay */}
      <div className="fixed inset-0 z-0">
        <Image
          src="/business-cats.jpeg"
          alt="Background"
          fill
          className="object-cover object-center"
          priority
        />
        <div className="absolute inset-0 bg-gradient-to-b from-black/70 via-black/60 to-black/80 backdrop-blur-[2px]" />
      </div>

      {/* Header */}
      <header className="sticky top-0 z-50 w-full border-b border-white/10 bg-black/20 backdrop-blur-md">
        <div className="container mx-auto px-6 h-20 flex items-center justify-between">
          <button
            onClick={() => router.push('/')}
            className="flex items-center gap-3 group"
          >
            <div className="bg-white/10 p-2 rounded-xl backdrop-blur-sm border border-white/10 group-hover:bg-white/20 transition-colors">
              <Image
                src="/logo.png"
                alt="CallCat Logo"
                width={32}
                height={32}
                className="w-8 h-8 drop-shadow-md"
              />
            </div>
            <span className="text-2xl font-bold tracking-tight text-white">CallCat</span>
          </button>
          
          <Button
            variant="ghost"
            className="text-white/80 hover:text-white hover:bg-white/10 gap-2"
            onClick={() => router.push('/')}
          >
            <ArrowLeft className="w-4 h-4" />
            Back to Home
          </Button>
        </div>
      </header>

      <div className="relative z-10 min-h-[calc(100vh-80px)] flex items-center justify-center p-4">
        <div className="w-full max-w-md animate-in fade-in zoom-in-95 duration-500">
          <Card className="border-white/10 bg-black/40 backdrop-blur-xl shadow-2xl">
            <CardHeader className="space-y-1 pb-6">
              <CardTitle className="text-2xl font-bold text-center text-white">Welcome back</CardTitle>
              <CardDescription className="text-center text-white/60">
                Enter your credentials to access your account
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <form onSubmit={handleSubmit} className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="email" className="text-white/90">Email Address</Label>
                  <div className="relative group">
                    <Mail className="absolute left-3 top-3 h-4 w-4 text-white/50 group-focus-within:text-indigo-400 transition-colors" />
                    <Input
                      id="email"
                      type="email"
                      placeholder="name@example.com"
                      value={formData.email}
                      onChange={(e) => updateFormData('email', e.target.value)}
                      className="pl-10 bg-white/5 border-white/10 text-white placeholder:text-white/30 focus:border-indigo-500/50 focus:bg-white/10 focus:ring-indigo-500/20 transition-all"
                      required
                    />
                  </div>
                </div>
                
                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <Label htmlFor="password" className="text-white/90">Password</Label>
                    <button
                      type="button"
                      onClick={() => router.push('/auth?mode=reset')}
                      className="text-xs text-indigo-300 hover:text-indigo-200 transition-colors"
                    >
                      Forgot password?
                    </button>
                  </div>
                  <div className="relative group">
                    <Lock className="absolute left-3 top-3 h-4 w-4 text-white/50 group-focus-within:text-indigo-400 transition-colors" />
                    <Input
                      id="password"
                      type="password"
                      placeholder="Enter your password"
                      value={formData.password}
                      onChange={(e) => updateFormData('password', e.target.value)}
                      className="pl-10 bg-white/5 border-white/10 text-white placeholder:text-white/30 focus:border-indigo-500/50 focus:bg-white/10 focus:ring-indigo-500/20 transition-all"
                      required
                    />
                  </div>
                </div>

                {error && (
                  <div className="flex items-center gap-2 p-3 rounded-lg bg-red-500/10 border border-red-500/20 text-red-200 text-sm animate-in fade-in slide-in-from-top-1">
                    <AlertCircle className="h-4 w-4 shrink-0" />
                    <span>{error}</span>
                  </div>
                )}

                <Button 
                  type="submit" 
                  className="w-full bg-indigo-600 hover:bg-indigo-500 text-white font-semibold shadow-lg shadow-indigo-900/20 border border-indigo-500/50 transition-all hover:scale-[1.02] active:scale-[0.98]" 
                  disabled={loading}
                >
                  {loading ? (
                    <div className="flex items-center gap-2">
                      <div className="h-4 w-4 animate-spin rounded-full border-2 border-white/30 border-t-white" />
                      Logging In...
                    </div>
                  ) : 'Log In'}
                </Button>
              </form>

              <div className="relative">
                <div className="absolute inset-0 flex items-center">
                  <span className="w-full border-t border-white/10" />
                </div>
                <div className="relative flex justify-center text-xs uppercase">
                  <span className="bg-transparent px-2 text-white/40 backdrop-blur-xl">
                    Or
                  </span>
                </div>
              </div>

              <div className="text-center">
                <p className="text-sm text-white/60">
                  Don&apos;t have an account?{' '}
                  <button
                    onClick={() => router.push('/auth?mode=register')}
                    className="text-indigo-300 hover:text-indigo-200 font-medium transition-colors hover:underline"
                  >
                    Create one
                  </button>
                </p>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
}
