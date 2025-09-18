'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Mail, Lock, AlertCircle, Phone } from 'lucide-react'
import { useAuth } from '@/contexts/AuthContext'
import { apiService } from '@/lib/api'

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
    <div className="min-h-screen relative">
      <div
        className="fixed inset-0 bg-cover bg-center bg-no-repeat"
        style={{
          backgroundImage: "url('/cafe.jpg')",
        }}
      />

      <div className="fixed inset-0 bg-black/20" />

      <div className="relative z-10 min-h-screen flex items-center justify-center p-4">
        <div className="w-full max-w-md">
          {/* CallCat Header */}
          <div className="text-center mb-8">
            <div className="flex items-center justify-center gap-2 mb-4">
              <div className="w-10 h-10 bg-amber-600 rounded-full flex items-center justify-center">
                <Phone className="w-5 h-5 text-white" />
              </div>
              <span className="text-2xl font-bold text-white">CallCat</span>
            </div>
            <h1 className="text-3xl font-bold text-white mb-2">Welcome back</h1>
            <p className="text-white/90">Ready to make some calls?</p>
          </div>

          <Card className="border border-white/20 shadow-lg bg-white/3 backdrop-blur-sm">
          <CardHeader>
            <CardTitle className="text-center text-white">Welcome back!</CardTitle>
            <CardDescription className="text-center text-white/80">
              Let&apos;s get you signed in
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="email" className="!text-white">Email Address</Label>
                <div className="relative">
                  <Mail className="absolute left-3 top-3 h-4 w-4 text-white/60" />
                  <Input
                    id="email"
                    type="email"
                    placeholder="Enter your email address"
                    value={formData.email}
                    onChange={(e) => updateFormData('email', e.target.value)}
                    className="pl-10 bg-white/5 border-white/40 !text-white !placeholder-white focus:ring-amber-400 focus:border-amber-400 focus:bg-white/10"
                    required
                  />
                </div>
              </div>
              
              <div className="space-y-2">
                <Label htmlFor="password" className="!text-white">Password</Label>
                <div className="relative">
                  <Lock className="absolute left-3 top-3 h-4 w-4 text-white/60" />
                  <Input
                    id="password"
                    type="password"
                    placeholder="Enter your password"
                    value={formData.password}
                    onChange={(e) => updateFormData('password', e.target.value)}
                    className="pl-10 bg-white/5 border-white/40 !text-white !placeholder-white focus:ring-amber-400 focus:border-amber-400 focus:bg-white/10"
                    required
                  />
                </div>
              </div>

              {error && (
                <div className="flex items-center space-x-2 p-3 bg-red-500/20 border border-red-400/30 rounded-md backdrop-blur-sm">
                  <AlertCircle className="h-4 w-4 text-red-300" />
                  <span className="text-sm text-red-200">{error}</span>
                </div>
              )}
              

              <Button type="submit" className="w-full bg-amber-600 hover:bg-amber-700 text-white" disabled={loading}>
                {loading ? (
                  <div className="flex items-center gap-2">
                    <div className="h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent" />
                    Signing In...
                  </div>
                ) : 'Sign In'}
              </Button>
            </form>

            <div className="mt-6 text-center">
              <button
                onClick={() => router.push('/auth?mode=reset')}
                className="text-sm text-amber-400 hover:text-amber-300 font-medium transition-colors"
              >
                Forgot your password?
              </button>
            </div>
          </CardContent>
        </Card>

          <div className="text-center mt-6">
            <p className="text-sm text-white/80">
              Don&apos;t have an account?{' '}
              <button
                onClick={() => router.push('/auth?mode=register')}
                className="text-amber-400 hover:text-amber-300 font-medium transition-colors"
              >
                Create one
              </button>
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}
