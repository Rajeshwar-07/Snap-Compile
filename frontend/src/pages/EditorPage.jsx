import React, { useState, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import Editor from '@monaco-editor/react'
import Tesseract from 'tesseract.js'
import Navbar from '../components/Navbar'
import { submissionAPI } from '../services/api'
import styles from './EditorPage.module.css'

// Map language name to Monaco editor language ID
const LANGUAGE_MAP = {
  java: 'java',
  python: 'python',
  javascript: 'javascript',
  sql: 'sql',
  cpp: 'cpp',
  go: 'go'
}

// Default starter code per language
const STARTER_CODE = {
  java: `public class Main {\n    public static void main(String[] args) {\n        System.out.println("Hello World");\n    }\n}`,
  python: `print("Hello World")`,
  javascript: `console.log("Hello World");`,
  sql: `SELECT * FROM users WHERE id = 1;`,
  cpp: `#include <iostream>\nusing namespace std;\n\nint main() {\n    cout << "Hello World" << endl;\n    return 0;\n}`,
  go: `package main\n\nimport "fmt"\n\nfunc main() {\n    fmt.Println("Hello World")\n}`
}

const LANGUAGES = ['java', 'python', 'javascript', 'sql', 'cpp', 'go']

/**
 * EditorPage - Main code editor with OCR upload and run functionality
 */
export default function EditorPage() {
  const [language, setLanguage] = useState('java')
  const [code, setCode] = useState(STARTER_CODE['java'])
  const [problemTitle, setProblemTitle] = useState('')
  const [output, setOutput] = useState('')
  const [errorMsg, setErrorMsg] = useState('')
  const [status, setStatus] = useState('')       // "ACCEPTED", "RUNTIME_ERROR", etc.
  const [runtimeMs, setRuntimeMs] = useState('')
  const [memoryUsed, setMemoryUsed] = useState('')
  const [loading, setLoading] = useState(false)
  const [ocrLoading, setOcrLoading] = useState(false)
  const [ocrText, setOcrText] = useState('')

  const fileInputRef = useRef()
  const navigate = useNavigate()

  // Switch language - reset code to starter template
  function handleLanguageChange(lang) {
    setLanguage(lang)
    setCode(STARTER_CODE[lang] || '')
    setOutput('')
    setErrorMsg('')
    setStatus('')
  }

  // Handle photo upload - run OCR and paste code into editor
  async function handlePhotoUpload(e) {
    const file = e.target.files[0]
    if (!file) return

    setOcrLoading(true)
    setOcrText('Reading code from photo...')

    try {
      // Tesseract.js - reads text from image locally (no server needed)
      const result = await Tesseract.recognize(file, 'eng', {
        logger: (m) => {
          if (m.status === 'recognizing text') {
            setOcrText(`Reading... ${Math.round(m.progress * 100)}%`)
          }
        }
      })

      const extractedCode = result.data.text
      if (extractedCode.trim()) {
        setCode(extractedCode)   // Paste OCR result into editor
        setOcrText(`✅ Code extracted (${extractedCode.split('\n').length} lines)`)
      } else {
        setOcrText('⚠️ Could not extract code. Try a clearer photo.')
      }
    } catch (err) {
      setOcrText('❌ OCR failed. Try again.')
    } finally {
      setOcrLoading(false)
    }
  }

  // Run code via Spring Boot → Judge0
  async function handleRun() {
    if (!code.trim()) return

    setLoading(true)
    setOutput('')
    setErrorMsg('')
    setStatus('')

    try {
      const response = await submissionAPI.runCode({
        code,
        language,
        problemTitle: problemTitle || 'Untitled'
      })

      const submission = response.data
      setOutput(submission.output || '')
      setErrorMsg(submission.errorMessage || '')
      setStatus(submission.status)
      setRuntimeMs(submission.runtimeMs || '')
      setMemoryUsed(submission.memoryUsed || '')
    } catch (err) {
      setErrorMsg('Failed to run code. Check if backend is running.')
      setStatus('RUNTIME_ERROR')
    } finally {
      setLoading(false)
    }
  }

  // Status badge color
  function getStatusColor(s) {
    if (s === 'ACCEPTED') return styles.statusGreen
    if (s === 'COMPILE_ERROR' || s === 'RUNTIME_ERROR') return styles.statusRed
    return styles.statusYellow
  }

  return (
    <div className={styles.page}>
      <Navbar />

      <div className={styles.layout}>
        {/* ── Left Sidebar: OCR Upload ── */}
        <aside className={styles.sidebar}>
          <p className={styles.sidebarTitle}>📷 Photo OCR</p>

          {/* Upload button */}
          <div
            className={styles.uploadZone}
            onClick={() => fileInputRef.current.click()}
          >
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <rect x="3" y="3" width="18" height="18" rx="3"
                stroke="#7c3aed" strokeWidth="1.5"/>
              <circle cx="8.5" cy="8.5" r="1.5" fill="#7c3aed"/>
              <path d="M21 15l-5-5L5 21" stroke="#7c3aed"
                strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
            <span>Upload code photo</span>
            <small>JPG, PNG up to 10MB</small>
          </div>

          <input
            type="file"
            accept="image/*"
            ref={fileInputRef}
            style={{ display: 'none' }}
            onChange={handlePhotoUpload}
          />

          {/* OCR status */}
          {(ocrLoading || ocrText) && (
            <div className={styles.ocrStatus}>
              {ocrLoading && <div className={styles.spinner}></div>}
              <span>{ocrText}</span>
            </div>
          )}

          {/* Problem title input */}
          <div className={styles.titleField}>
            <label className={styles.sidebarLabel}>Problem Title</label>
            <input
              type="text"
              value={problemTitle}
              onChange={(e) => setProblemTitle(e.target.value)}
              className={styles.titleInput}
              placeholder="e.g. Fibonacci, Two Sum"
            />
          </div>

          {/* View History */}
          <button
            className={styles.historyBtn}
            onClick={() => navigate('/history')}
          >
            📋 View History
          </button>
        </aside>

        {/* ── Center: Monaco Editor ── */}
        <main className={styles.editorSection}>
          {/* Editor Toolbar */}
          <div className={styles.editorToolbar}>
            {/* Language pills */}
            <div className={styles.langPills}>
              {LANGUAGES.map(lang => (
                <button
                  key={lang}
                  onClick={() => handleLanguageChange(lang)}
                  className={`${styles.langPill} ${language === lang ? styles.langPillActive : ''}`}
                >
                  {lang}
                </button>
              ))}
            </div>

            {/* Run button */}
            <button
              className={styles.runBtn}
              onClick={handleRun}
              disabled={loading}
            >
              {loading ? (
                <><div className={styles.spinnerSmall}></div> Running...</>
              ) : (
                <>▶ Run Code</>
              )}
            </button>
          </div>

          {/* Monaco Editor */}
          <div className={styles.editorWrapper}>
            <Editor
              height="100%"
              language={LANGUAGE_MAP[language] || 'plaintext'}
              value={code}
              onChange={(value) => setCode(value || '')}
              theme="vs-dark"
              options={{
                fontSize: 14,
                fontFamily: 'JetBrains Mono, monospace',
                minimap: { enabled: false },
                scrollBeyondLastLine: false,
                lineNumbers: 'on',
                wordWrap: 'on',
                padding: { top: 16 }
              }}
            />
          </div>
        </main>

        {/* ── Right Panel: Output ── */}
        <aside className={styles.outputPanel}>
          <p className={styles.sidebarTitle}>Output</p>

          {/* Status badge */}
          {status && (
            <div className={`${styles.statusBadge} ${getStatusColor(status)}`}>
              {status.replace('_', ' ')}
              {runtimeMs && <span className={styles.statSmall}> · {runtimeMs}</span>}
              {memoryUsed && <span className={styles.statSmall}> · {memoryUsed}</span>}
            </div>
          )}

          {/* Output box */}
          <div className={styles.outputBox}>
            {loading && <p className={styles.outputMuted}>Running code...</p>}
            {!loading && !output && !errorMsg && (
              <p className={styles.outputMuted}>Run your code to see output here</p>
            )}
            {output && <pre className={styles.outputText}>{output}</pre>}
            {errorMsg && <pre className={styles.errorText}>{errorMsg}</pre>}
          </div>
        </aside>
      </div>
    </div>
  )
}
