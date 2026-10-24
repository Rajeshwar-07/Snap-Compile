import React, { createContext, useContext, useState } from 'react'

/**
 * AuthContext - Global authentication state
 *
 * Stores: token, user info
 * Used by: all pages that need to know if user is logged in
 *
 * Usage:
 *   const { user, token, login, logout } = useAuth()
 */
const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  // Load saved token from localStorage (persists after page refresh)
  const [token, setToken] = useState(() => localStorage.getItem('token'))
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('user')
    return saved ? JSON.parse(saved) : null
  })

  // Called after successful login/register
  function login(authData) {
    setToken(authData.token)
    setUser({ username: authData.username, email: authData.email, id: authData.userId })
    localStorage.setItem('token', authData.token)
    localStorage.setItem('user', JSON.stringify({
      username: authData.username,
      email: authData.email,
      id: authData.userId
    }))
  }

  // Called on logout
  function logout() {
    setToken(null)
    setUser(null)
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return (
    <AuthContext.Provider value={{ token, user, login, logout, isLoggedIn: !!token }}>
      {children}
    </AuthContext.Provider>
  )
}

// Custom hook - easier to use
export function useAuth() {
  return useContext(AuthContext)
}
