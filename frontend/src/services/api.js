import axios from 'axios'

/**
 * API Service - All HTTP calls to Spring Boot backend
 *
 * Base URL: http://localhost:8080 (dev) or your Render URL (prod)
 * Every request to protected routes automatically includes JWT token
 */

// Create axios instance with base URL
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json'
  }
})

// Interceptor: automatically add JWT token to every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Interceptor: handle 401 (token expired) - redirect to login
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

// =============================================
// Auth APIs
// =============================================

export const authAPI = {
  register: (data) => api.post('/api/auth/register', data),
  login: (data) => api.post('/api/auth/login', data)
}

// =============================================
// Submission / Code Run APIs
// =============================================

export const submissionAPI = {
  // Run code and save to history
  runCode: (data) => api.post('/api/submissions/run', data),

  // Get full history (all submissions)
  getHistory: () => api.get('/api/submissions/history'),

  // Get history filtered by language
  getHistoryByLanguage: (language) =>
    api.get(`/api/submissions/history/filter?language=${language}`),

  // Get single submission detail
  getById: (id) => api.get(`/api/submissions/${id}`)
}

export default api
