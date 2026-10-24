import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import Navbar from '../components/Navbar'
import { submissionAPI } from '../services/api'
import styles from './HistoryPage.module.css'

/**
 * HistoryPage - Shows all past code submissions
 *
 * Each card shows: problem title, language, status, date, runtime
 * Clicking a card → goes to SubmissionDetailPage
 */
export default function HistoryPage() {
  const [submissions, setSubmissions] = useState([])
  const [loading, setLoading] = useState(true)
  const [filterLang, setFilterLang] = useState('all')
  const [error, setError] = useState('')

  const navigate = useNavigate()

  // Load history on mount
  useEffect(() => {
    loadHistory()
  }, [])

  async function loadHistory() {
    try {
      const response = await submissionAPI.getHistory()
      setSubmissions(response.data)
    } catch (err) {
      setError('Failed to load history')
    } finally {
      setLoading(false)
    }
  }

  // Filter by language
  const filtered = filterLang === 'all'
    ? submissions
    : submissions.filter(s => s.language === filterLang)

  // Get unique languages used
  const usedLanguages = [...new Set(submissions.map(s => s.language))]

  // Format date nicely
  function formatDate(dateStr) {
    const date = new Date(dateStr)
    return date.toLocaleDateString('en-IN', {
      day: '2-digit', month: 'short', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    })
  }

  // Status badge style
  function statusStyle(status) {
    if (status === 'ACCEPTED') return styles.accepted
    if (status === 'COMPILE_ERROR' || status === 'RUNTIME_ERROR') return styles.failed
    return styles.pending
  }

  return (
    <div className={styles.page}>
      <Navbar />

      <div className={styles.container}>
        {/* Header */}
        <div className={styles.header}>
          <div>
            <h1 className={styles.title}>Submission History</h1>
            <p className={styles.subtitle}>{submissions.length} problems solved</p>
          </div>
          <button className={styles.newCodeBtn} onClick={() => navigate('/editor')}>
            + New Code
          </button>
        </div>

        {/* Language filter */}
        <div className={styles.filters}>
          <button
            className={`${styles.filterBtn} ${filterLang === 'all' ? styles.filterActive : ''}`}
            onClick={() => setFilterLang('all')}
          >
            All
          </button>
          {usedLanguages.map(lang => (
            <button
              key={lang}
              className={`${styles.filterBtn} ${filterLang === lang ? styles.filterActive : ''}`}
              onClick={() => setFilterLang(lang)}
            >
              {lang}
            </button>
          ))}
        </div>

        {/* Content */}
        {loading && <p className={styles.loadingText}>Loading history...</p>}
        {error && <p className={styles.errorText}>{error}</p>}

        {!loading && filtered.length === 0 && (
          <div className={styles.emptyState}>
            <p>No submissions yet.</p>
            <button className={styles.newCodeBtn} onClick={() => navigate('/editor')}>
              Write your first code →
            </button>
          </div>
        )}

        {/* Submission cards */}
        <div className={styles.cardList}>
          {filtered.map((submission, index) => (
            <div
              key={submission.id}
              className={styles.card}
              onClick={() => navigate(`/history/${submission.id}`)}
            >
              {/* Left: index + title */}
              <div className={styles.cardLeft}>
                <span className={styles.cardIndex}>#{index + 1}</span>
                <div>
                  <p className={styles.cardTitle}>{submission.problemTitle || 'Untitled'}</p>
                  <p className={styles.cardDate}>{formatDate(submission.submittedAt)}</p>
                </div>
              </div>

              {/* Right: language, status, runtime */}
              <div className={styles.cardRight}>
                <span className={styles.langBadge}>{submission.language}</span>
                <span className={`${styles.statusBadge} ${statusStyle(submission.status)}`}>
                  {submission.status === 'ACCEPTED' ? '✓' : '✗'} {submission.status?.replace('_', ' ')}
                </span>
                {submission.runtimeMs && (
                  <span className={styles.runtimeBadge}>⏱ {submission.runtimeMs}</span>
                )}
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none"
                  className={styles.chevron}>
                  <path d="M9 18l6-6-6-6" stroke="#6e7681" strokeWidth="1.8"
                    strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
