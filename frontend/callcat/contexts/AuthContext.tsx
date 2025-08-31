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
  login: (user: User) => void
  logout: () => Promise<void>
  checkAuth: () => Promise<void>
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const router = useRouter()

  const login = (userData: User) => {
    // httpOnly cookie is already set by server
    setUser(userData)
  }

  const logout = async () => {
    try {
      // Call backend logout to clear httpOnly cookie
      await apiService.logout()
    } catch (error) {
      console.error('Logout API call failed:', error)
      // Continue with local cleanup even if API call fails
    }
    
    // Clear user state - httpOnly cookie is cleared by server
    setUser(null)
    router.push('/login')
  }

  const checkAuth = async () => {
    try {
      // Try to get user profile - if httpOnly cookie is valid, this will succeed
      const userData = await apiService.getUserProfile()
      setUser({
        id: userData.id,
        email: userData.email,
        fullName: `${userData.firstName} ${userData.lastName}`
      })
    } catch (error) {
      console.error('Auth check failed:', error)
      // Cookie is invalid or expired - clear user state
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
