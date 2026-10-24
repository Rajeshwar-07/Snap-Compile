import React from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './context/AuthContext'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import EditorPage from './pages/EditorPage'
import HistoryPage from './pages/HistoryPage'
import SubmissionDetailPage from './pages/SubmissionDetailPage'

/**
 * ProtectedRoute - Redirects to login if user is not authenticated
 */
function ProtectedRoute({ children }) {
  const { isLoggedIn } = useAuth()
  return isLoggedIn ? children : <Navigate to="/login" replace />
}

/**
 * App - Main router setup
 *
 * Routes:
 * /login          → LoginPage (public)
 * /register       → RegisterPage (public)
 * /editor         → EditorPage (protected - needs login)
 * /history        → HistoryPage (protected)
 * /history/:id    → SubmissionDetailPage (protected)
 */
function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/editor" element={
        <ProtectedRoute><EditorPage /></ProtectedRoute>
      } />
      <Route path="/history" element={
        <ProtectedRoute><HistoryPage /></ProtectedRoute>
      } />
      <Route path="/history/:id" element={
        <ProtectedRoute><SubmissionDetailPage /></ProtectedRoute>
      } />
      {/* Default redirect */}
      <Route path="/" element={<Navigate to="/editor" replace />} />
    </Routes>
  )
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <AppRoutes />
      </BrowserRouter>
    </AuthProvider>
  )
}
