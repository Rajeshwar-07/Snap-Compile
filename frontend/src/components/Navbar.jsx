import React from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import styles from './Navbar.module.css'

/**
 * Navbar - Top navigation bar shown on all protected pages
 */
export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  function handleLogout() {
    logout()
    navigate('/login')
  }

  return (
    <nav className={styles.navbar}>
      {/* Logo */}
      <Link to="/editor" className={styles.logo}>
        <div className={styles.logoIcon}>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
            <path d="M4 16l4-4 4 4 4-8 4 4" stroke="#fff" strokeWidth="2.2"
              strokeLinecap="round" strokeLinejoin="round"/>
          </svg>
        </div>
        <span>SnapCompile</span>
      </Link>

      {/* Nav Links */}
      <div className={styles.navLinks}>
        <Link
          to="/editor"
          className={`${styles.navLink} ${location.pathname === '/editor' ? styles.active : ''}`}
        >
          Editor
        </Link>
        <Link
          to="/history"
          className={`${styles.navLink} ${location.pathname === '/history' ? styles.active : ''}`}
        >
          History
        </Link>
      </div>

      {/* User info + logout */}
      <div className={styles.userSection}>
        <span className={styles.username}>👤 {user?.username}</span>
        <button className={styles.logoutBtn} onClick={handleLogout}>
          Logout
        </button>
      </div>
    </nav>
  )
}
