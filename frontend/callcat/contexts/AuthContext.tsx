'use client'

import { createContext, useContext, useEffect, useState, ReactNode } from 'react'
import { useRouter } from 'next/navigation'
import { apiService } from '@/lib/api'

interface User {
  id: string
  email: string
  fullName: string
}

interface AuthContextType {
  user: User | null
  isAuthenticated: boolean
  isLoading: boolean
  login: (user: User, token: string) => void
  logout: () => Promise<void>
  checkAuth: () => Promise<void>
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const router = useRouter()

  const login = (userData: User, token: string) => {
    // Store JWT in localStorage
    localStorage.setItem('jwt', token)
    setUser(userData)
  }

  const logout = async () => {
    try {
      // Call backend logout to blacklist token and clear localStorage
      await apiService.logout()
    } catch (error) {
      console.error('Logout API call failed:', error)
      // Continue with local cleanup even if API call fails
      localStorage.removeItem('jwt')
    }
    
    // Clear user state
    setUser(null)
    router.push('/login')
  }

  const checkAuth = async () => {
    try {
      // Check if token exists in localStorage
      const token = localStorage.getItem('jwt')
      if (!token) {
        setUser(null)
        setIsLoading(false)
        return
      }

      // Validate token by calling user profile endpoint
      const userData = await apiService.getUserProfile()
      setUser({
        id: userData.id,
        email: userData.email,
        fullName: `${userData.firstName} ${userData.lastName}`
      })
    } catch (error) {
      console.error('Auth check failed:', error)
      // Token is invalid or expired - clear user state and localStorage
      localStorage.removeItem('jwt')
      setUser(null)
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    checkAuth()
  }, [])

  const value: AuthContextType = {
    user,
    isAuthenticated: !!user,
    isLoading,
    login,
    logout,
    checkAuth
  }

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
