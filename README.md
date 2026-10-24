# SnapCompile — Photo to Code Compiler

A full-stack online compiler that accepts photo uploads, uses OCR to extract code from images,
and lets users run code in 6+ languages with full submission history.

---

## Tech Stack

| Layer     | Technology                          |
|-----------|-------------------------------------|
| Frontend  | React 18 + Vite + CSS Modules       |
| Backend   | Spring Boot 3 + Maven + Java 17     |
| Database  | PostgreSQL (Neon.tech free tier)     |
| Auth      | JWT (JSON Web Tokens)               |
| Compiler  | Judge0 API (free tier via RapidAPI) |
| OCR       | Tesseract.js (runs in browser)      |
| Editor    | Monaco Editor (same as VS Code)     |

---

## Features

- Register / Login with JWT authentication
- Upload photo of code → OCR extracts text → pastes into editor
- Run code in: Java, Python, JavaScript, SQL, C++, Go
- See output, runtime, memory usage
- Full submission history (all runs saved)
- Submission detail: code view, output, complexity, test cases
- Load any past submission back into editor

---

## Project Structure

```
snapcompile/
├── frontend/                      ← React app
│   └── src/
│       ├── pages/
│       │   ├── LoginPage.jsx
│       │   ├── RegisterPage.jsx
│       │   ├── EditorPage.jsx         ← Main editor + OCR
│       │   ├── HistoryPage.jsx        ← All past submissions
│       │   └── SubmissionDetailPage.jsx  ← One submission detail
│       ├── components/
│       │   └── Navbar.jsx
│       ├── context/
│       │   └── AuthContext.jsx        ← JWT token management
│       └── services/
│           └── api.js                 ← All API calls
│
└── backend/                       ← Spring Boot app
    └── src/main/java/com/snapcompile/
        ├── controller/
        │   ├── AuthController.java    ← POST /api/auth/login, /register
        │   └── SubmissionController.java  ← POST /run, GET /history
        ├── service/
        │   ├── AuthService.java
        │   ├── SubmissionService.java
        │   └── Judge0Service.java     ← Calls Judge0 API
        ├── model/
        │   ├── User.java
        │   └── Submission.java
        ├── repository/
        │   ├── UserRepository.java
        │   └── SubmissionRepository.java
        └── security/
            ├── JwtUtil.java
            └── JwtAuthFilter.java
```

---

## Setup Guide

### Backend

1. Create a free PostgreSQL database at https://neon.tech

2. Edit `backend/src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://YOUR_HOST/snapcompile?sslmode=require
   spring.datasource.username=YOUR_USERNAME
   spring.datasource.password=YOUR_PASSWORD
   judge0.api.key=YOUR_RAPIDAPI_KEY
   ```

3. Get a free Judge0 API key:
   - Go to https://rapidapi.com/judge0-official/api/judge0-ce
   - Subscribe to the free tier
   - Copy the X-RapidAPI-Key

4. Run the backend:
   ```bash
   cd backend
   mvn spring-boot:run
   ```

### Frontend

1. Install dependencies:
   ```bash
   cd frontend
   npm install
   ```

2. Create `.env` file:
   ```
   VITE_API_URL=http://localhost:8080
   ```

3. Run:
   ```bash
   npm run dev
   ```

4. Open: http://localhost:5173

---

## API Endpoints

| Method | URL                              | Auth | Description              |
|--------|----------------------------------|------|--------------------------|
| POST   | /api/auth/register               | No   | Register new user        |
| POST   | /api/auth/login                  | No   | Login, get JWT token     |
| POST   | /api/submissions/run             | Yes  | Run code, save to history|
| GET    | /api/submissions/history         | Yes  | Get all submissions      |
| GET    | /api/submissions/history/filter  | Yes  | Filter by language       |
| GET    | /api/submissions/{id}            | Yes  | Get one submission       |

---

## Deployment

- **Frontend**: Deploy to Vercel (drag & drop or `vercel deploy`)
- **Backend**: Deploy to Render (free tier, connect GitHub)
- **Database**: Neon.tech (already free, just update connection string)

---

## Cost: ₹0/month (all free tiers)
