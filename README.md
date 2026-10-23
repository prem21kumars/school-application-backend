# Quiz & Assessment Management System - Spring Boot & MySQL Backend

Enterprise-grade Java Spring Boot and MySQL backend for the School Quiz & Examination Platform, designed to power the React TypeScript frontend located at `D:\Website\quiz-app (2)`.

---

## 🛠️ Technology Stack
- **Java**: 21 (OpenJDK Temurin LTS)
- **Framework**: Spring Boot 3.3.4
  - `spring-boot-starter-web` (REST APIs, Jackson JSON)
  - `spring-boot-starter-data-jpa` (Hibernate 6 ORM)
  - `spring-boot-starter-validation`
  - `mysql-connector-j`
  - `spring-boot-devtools`
- **Database**: MySQL 8.0 (Database name: `quiz_app_db`)
- **Build Tool**: Apache Maven 3.9+
- **Default Server Port**: `8080`

---

## 🏗️ Architecture & Database Design

### Database Tables (MySQL)
The database schema is automatically maintained by Hibernate (`ddl-auto=update`) and seeded with default curriculum data by `DataInitializer`:

| Table Name | Description |
|------------|-------------|
| `users` | Multi-role user accounts (Principal, Teachers, Students, Parents) |
| `user_assigned_classes` | Teacher class assignments |
| `user_assigned_subjects` | Teacher subject assignments |
| `school_classes` | Academic grades & sections (e.g. Grade 10 A, Grade 10 B) |
| `class_subjects` | Subjects assigned to classes |
| `subjects` | Curriculum subjects (Physics, Math, Chemistry) |
| `subject_chapters` | Subject chapters, summaries, and curriculum excerpts |
| `chapter_topics` | Topics per chapter |
| `book_sources` | Textbook sources with structured chapter content |
| `assessments` | Online/Offline quizzes, unit tests, and term exams |
| `assessment_classes` | Classes assigned to each assessment |
| `assessment_target_students` | Optional targeted student IDs |
| `assessment_instructions` | Exam guidelines and rules |
| `questions` | Question repository (MCQ, True/False, Key-In, Short & Long Answer) |
| `question_options` | Multiple-choice options with boolean correctness indicator |
| `student_exam_attempts` | Online student exam attempts, scores, and proctoring status |
| `malpractice_logs` | Anti-cheating proctoring event logs (tab switch, exit fullscreen, etc.) |
| `reattempt_requests` | Student exam retake requests and teacher approvals |
| `offline_answer_sheets` | Scanned student paper evaluation records |
| `notifications` | In-app alerts, proctoring warnings, and submission updates |

---

## 🔑 Default Credentials

| Role | Email / Enrolment Number | Password | Name |
|------|--------------------------|----------|------|
| **Principal** | `principal@oakridge.edu` | `admin` | Dr. Arthur Vance |
| **Teacher** | `sarah.jenkins@oakridge.edu` | `teach` | Dr. Sarah Jenkins (Physics) |
| **Teacher** | `alan.turing@oakridge.edu` | `teach` | Prof. Alan Turing (Math) |
| **Teacher** | `elena.rostova@oakridge.edu` | `teach` | Mrs. Elena Rostova (Chemistry) |
| **Student** | `ENR-2026-101` / `alex.rivera@student.oakridge.edu` | `pass` | Alex Rivera |
| **Student** | `ENR-2026-102` / `maya.patel@student.oakridge.edu` | `pass` | Maya Patel |
| **Student** | `ENR-2026-103` / `david.kim@student.oakridge.edu` | `pass` | David Kim |
| **Parent** | `carlos.rivera@gmail.com` | `pass` | Carlos & Maria Rivera |

---

## 🚀 How to Run the Backend

### 1. Ensure MySQL is Running
The application connects to MySQL on `localhost:3306`:
- Database: `quiz_app_db` (Created automatically)
- Username: `root`
- Password: `root`

*(To customize credentials, edit `src/main/resources/application.properties`)*

### 2. Run with Maven
```powershell
cd D:\Website\quiz_app_backend
mvn spring-boot:run
```
The server will start at: `http://localhost:8080`.

---

## 📡 REST API Reference

### 1. Authentication (`/api/auth`)
- `POST /api/auth/login`: Authenticates user by role and email or enrolment number.
```json
{
  "role": "principal",
  "identifier": "principal@oakridge.edu",
  "password": "admin"
}
```

### 2. Users (`/api/users`)
- `GET /api/users`: Get all users (optional `?role=teacher`)
- `GET /api/users/{id}`: Get user by ID
- `POST /api/users`: Create a teacher or student account
- `PUT /api/users/{id}`: Update user profile
- `PATCH /api/users/{id}/status`: Toggle active / inactive status
- `POST /api/users/{teacherId}/assignments`: Assign classes and subjects to a teacher

### 3. Classes & Subjects (`/api/classes`, `/api/subjects`)
- `GET /api/classes`: Get all school classes
- `POST /api/classes`: Create new class
- `GET /api/subjects`: Get all subjects and their chapters
- `POST /api/subjects`: Create new subject
- `POST /api/subjects/{subjectId}/chapters`: Add chapter to subject

### 4. Assessments & Question Generation (`/api/assessments`)
- `GET /api/assessments`: List all assessments
- `GET /api/assessments/{id}`: Get assessment details with full questions and options
- `POST /api/assessments`: Create new assessment
- `PUT /api/assessments/{id}`: Update assessment
- `DELETE /api/assessments/{id}`: Delete assessment
- `GET /api/assessments/{id}/sets?count=3`: Generate randomized printable exam sets (Set A, Set B, Set C) with full answer keys, marking rubrics, and explanations.
- `POST /api/assessments/generate-ai-questions`: Generate AI curriculum questions.
```json
{
  "subjectId": "sub-phy",
  "chapterId": "ch-phy-3",
  "questionTypes": ["mcq", "true_false", "key_in"],
  "marks": 3,
  "difficulty": "medium",
  "count": 5,
  "customPrompt": "Optional teacher instructions"
}
```
  *Powered by **Groq AI LLM** (`openai/gpt-oss-120b` / ultra-fast Llama models) with automatic graceful fallback to the built-in curriculum template engine if offline or disabled.*

- `POST /api/assessments/generate-book-questions`: Generate questions from textbook syllabus chapters.

### 4.1 🤖 Groq AI LLM Configuration (Optional)
Groq AI generates curriculum-aligned exam questions at high speeds:
- Configured in [application.properties](file:///d:/Website/quiz_app_backend/src/main/resources/application.properties):
  - `groq.api.enabled=true` (Toggle ON / OFF)
  - `groq.api.key=${GROQ_API_KEY:...}` (Groq API Key)
  - `groq.api.model=openai/gpt-oss-120b` (Default model)
- Status visible in `GET /api/system/health` (`groqAiEnabled`, `groqAiReady`, `groqAiModel`).


### 5. Exam Engine & Anti-Cheating Proctoring (`/api/attempts`)
- `GET /api/attempts`: Get attempts (filter by `studentId`, `assessmentId`, `classId`)
- `POST /api/attempts/start?assessmentId=...&studentId=...`: Start an exam session
- `POST /api/attempts/{attemptId}/malpractice`: Record proctoring violation (`tab_switch`, `fullscreen_exit`, etc.). Auto-terminates at >= 3 violations and alerts teacher.
- `POST /api/attempts/{attemptId}/submit`: Submit student answers. Computes automated scores for MCQs, True/False, Key-In and subjective questions.

### 6. Reattempt Requests (`/api/reattempts`)
- `GET /api/reattempts`: View re-attempt requests
- `POST /api/reattempts`: Submit re-attempt request
- `PATCH /api/reattempts/{requestId}/resolve`: Approve or reject reattempt (`status`: "approved" | "rejected", `paperMode`: "same" | "new_random")

### 7. Offline AI Sheet Evaluation (`/api/offline-sheets`)
- `GET /api/offline-sheets`: View offline sheets
- `POST /api/offline-sheets`: Upload scanned student sheet
- `POST /api/offline-sheets/{id}/evaluate`: Run AI OCR handwriting evaluation
- `PUT /api/offline-sheets/{id}/marks`: Teacher marks verification and override

### 8. System & Diagnostics (`/api/system`)
- `GET /api/system/health`: Application health check and table record counts
- `POST /api/system/reset`: Reset entire database back to default seed state
