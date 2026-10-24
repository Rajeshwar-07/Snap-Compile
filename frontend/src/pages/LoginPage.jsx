import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { authAPI } from '../services/api'
import styles from './AuthPage.module.css'

/**
 * LoginPage - User login form
 */
export default function LoginPage() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const { login } = useAuth()
  const navigate = useNavigate()

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      const response = await authAPI.login({ email, password })
      login(response.data)        // Save token + user in context
      navigate('/editor')         // Go to editor after login
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid email or password')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        {/* Logo */}
        <div className={styles.logoRow}>
          <div className={styles.logoIcon}>
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
              <path d="M4 16l4-4 4 4 4-8 4 4" stroke="#fff" strokeWidth="2.2"
                strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
          </div>
          <span className={styles.logoText}>SnapCompile</span>
        </div>

        <h1 className={styles.title}>Welcome back</h1>
        <p className={styles.subtitle}>Login to your account</p>

        <form onSubmit={handleSubmit} className={styles.form}>
          <div className={styles.field}>
            <label className={styles.label}>Email</label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className={styles.input}
              placeholder="you@example.com"
              required
            />
          </div>

          <div className={styles.field}>
            <label className={styles.label}>Password</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className={styles.input}
              placeholder="Enter password"
              required
            />
          </div>

          {/* Error message */}
          {error && <p className={styles.error}>{error}</p>}

          <button
            type="submit"
            className={styles.submitBtn}
            disabled={loading}
          >
            {loading ? 'Logging in...' : 'Login'}
          </button>
        </form>

        <p className={styles.switchText}>
          Don't have an account?{' '}
          <Link to="/register" className={styles.switchLink}>Register</Link>
        </p>
      </div>
    </div>
  )
}
