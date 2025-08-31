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
  token: string | null
  isAuthenticated: boolean
  isLoading: boolean
  login: (token: string, user: User) => void
  logout: () => Promise<void>
  checkAuth: () => Promise<void>
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [token, setToken] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const router = useRouter()

  const login = (newToken: string, userData: User) => {
    localStorage.setItem('authToken', newToken)
    setToken(newToken)
    setUser(userData)
  }

  const logout = async () => {
    try {
      // Call backend logout to blacklist token
      await apiService.logout()
    } catch (error) {
      console.error('Logout API call failed:', error)
      // Continue with local cleanup even if API call fails
    }
    
    localStorage.removeItem('authToken')
    setToken(null)
    setUser(null)
    router.push('/login')
  }

  const checkAuth = async () => {
    try {
      const storedToken = localStorage.getItem('authToken')
      if (!storedToken) {
        setIsLoading(false)
        return
      }

      // Verify token with backend using API service
      const userData = await apiService.getUserProfile()
      setToken(storedToken)
      setUser({
        id: userData.id,
        email: userData.email,
        fullName: `${userData.firstName} ${userData.lastName}`
      })
    } catch (error) {
      console.error('Auth check failed:', error)
      // Token is invalid, remove it
      localStorage.removeItem('authToken')
      setToken(null)
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
    token,
    isAuthenticated: !!token && !!user,
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
