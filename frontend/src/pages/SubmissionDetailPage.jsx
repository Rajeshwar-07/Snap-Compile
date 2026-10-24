import React, { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import Editor from '@monaco-editor/react'
import Navbar from '../components/Navbar'
import { submissionAPI } from '../services/api'
import styles from './SubmissionDetailPage.module.css'

/**
 * SubmissionDetailPage - Shows full details of a single submission
 *
 * Shows:
 * - Problem title, language, status
 * - Full code (read-only Monaco editor)
 * - Output / error
 * - Time & space complexity
 * - Runtime & memory stats
 * - Load in editor button
 */
export default function SubmissionDetailPage() {
  const { id } = useParams()      // Get submission ID from URL
  const navigate = useNavigate()

  const [submission, setSubmission] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    loadSubmission()
  }, [id])

  async function loadSubmission() {
    try {
      const response = await submissionAPI.getById(id)
      setSubmission(response.data)
    } catch (err) {
      setError('Submission not found or access denied.')
    } finally {
      setLoading(false)
    }
  }

  // Open this code back in editor to re-run or modify
  function loadInEditor() {
    // Save code to sessionStorage, editor page will pick it up
    sessionStorage.setItem('loadCode', submission.code)
    sessionStorage.setItem('loadLang', submission.language)
    navigate('/editor')
  }

  function formatDate(dateStr) {
    return new Date(dateStr).toLocaleDateString('en-IN', {
      day: '2-digit', month: 'long', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    })
  }

  function statusColor(status) {
    if (status === 'ACCEPTED') return styles.statusGreen
    if (status === 'RUNTIME_ERROR' || status === 'COMPILE_ERROR') return styles.statusRed
    return styles.statusYellow
  }

  if (loading) return <div className={styles.center}>Loading...</div>
  if (error) return <div className={styles.center}>{error}</div>

  return (
    <div className={styles.page}>
      <Navbar />

      <div className={styles.container}>
        {/* ── Back button ── */}
        <button className={styles.backBtn} onClick={() => navigate('/history')}>
          ← Back to History
        </button>

        {/* ── Header ── */}
        <div className={styles.header}>
          <div>
            <h1 className={styles.title}>{submission.problemTitle || 'Untitled'}</h1>
            <p className={styles.date}>{formatDate(submission.submittedAt)}</p>
          </div>
          <div className={styles.headerRight}>
            <span className={styles.langBadge}>{submission.language}</span>
            <span className={`${styles.statusBadge} ${statusColor(submission.status)}`}>
              {submission.status?.replace('_', ' ')}
            </span>
            <button className={styles.loadBtn} onClick={loadInEditor}>
              ↗ Load in Editor
            </button>
          </div>
        </div>

        <div className={styles.twoCol}>
          {/* ── Left: Code ── */}
          <div className={styles.codeSection}>
            <p className={styles.sectionLabel}>Source Code</p>
            <div className={styles.editorBox}>
              <Editor
                height="400px"
                language={submission.language}
                value={submission.code}
                theme="vs-dark"
                options={{
                  readOnly: true,
                  fontSize: 13,
                  fontFamily: 'JetBrains Mono, monospace',
                  minimap: { enabled: false },
                  scrollBeyondLastLine: false,
                  lineNumbers: 'on',
                  padding: { top: 12 }
                }}
              />
            </div>
          </div>

          {/* ── Right: Stats + Output ── */}
          <div className={styles.statsSection}>

            {/* Performance stats */}
            <div className={styles.statsGrid}>
              <div className={styles.statCard}>
                <p className={styles.statLabel}>Runtime</p>
                <p className={styles.statValue}>{submission.runtimeMs || '—'}</p>
              </div>
              <div className={styles.statCard}>
                <p className={styles.statLabel}>Memory</p>
                <p className={styles.statValue}>{submission.memoryUsed || '—'}</p>
              </div>
              <div className={styles.statCard}>
                <p className={styles.statLabel}>Time Complexity</p>
                <p className={styles.statValueMono}>{submission.timeComplexity || '—'}</p>
              </div>
              <div className={styles.statCard}>
                <p className={styles.statLabel}>Space Complexity</p>
                <p className={styles.statValueMono}>{submission.spaceComplexity || '—'}</p>
              </div>
            </div>

            {/* Output */}
            <div>
              <p className={styles.sectionLabel}>Output</p>
              <div className={styles.outputBox}>
                {submission.output
                  ? <pre className={styles.outputText}>{submission.output}</pre>
                  : <p className={styles.muted}>No output</p>
                }
                {submission.errorMessage && (
                  <pre className={styles.errorText}>{submission.errorMessage}</pre>
                )}
              </div>
            </div>

            {/* Test cases - if available */}
            {submission.testCasesResult && (
              <div>
                <p className={styles.sectionLabel}>
                  Test Cases
                  {submission.testsPassed != null && (
                    <span className={styles.testScore}>
                      {submission.testsPassed}/{submission.testsTotal} passed
                    </span>
                  )}
                </p>
                <TestCases json={submission.testCasesResult} />
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

/**
 * TestCases - Renders test case results from JSON string
 */
function TestCases({ json }) {
  try {
    const cases = JSON.parse(json)
    return (
      <div className={styles.testList}>
        {cases.map((tc, i) => (
          <div key={i} className={`${styles.testCase} ${tc.passed ? styles.testPass : styles.testFail}`}>
            <div className={styles.testHeader}>
              <span>{tc.passed ? '✓' : '✗'} Test {i + 1}</span>
              <span className={styles.testStatus}>{tc.passed ? 'Passed' : 'Failed'}</span>
            </div>
            <div className={styles.testRow}>
              <span className={styles.testLabel}>Input</span>
              <code className={styles.testCode}>{tc.input}</code>
            </div>
            <div className={styles.testRow}>
              <span className={styles.testLabel}>Expected</span>
              <code className={styles.testCode}>{tc.expected}</code>
            </div>
            <div className={styles.testRow}>
              <span className={styles.testLabel}>Got</span>
              <code className={`${styles.testCode} ${tc.passed ? styles.gotGreen : styles.gotRed}`}>
                {tc.actual}
              </code>
            </div>
          </div>
        ))}
      </div>
    )
  } catch {
    return <p style={{ color: 'var(--text-faint)', fontSize: 12 }}>Could not parse test cases</p>
  }
}
