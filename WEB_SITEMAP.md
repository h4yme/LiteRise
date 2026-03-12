# LiteRise Web Portal — Complete Sitemap
## Teacher & Admin Portal

> Cross-referenced from: `LiteriseWeb` (ASP.NET MVC), `APIFINAL` (PHP backend), and `LiteRise` (Android app).
> Last updated: 2026-03-12

---

## Codebase Overview

| Repo | Tech | Purpose |
|------|------|---------|
| `h4yme/LiteriseWeb` | ASP.NET MVC + Bootstrap + Chart.js | Web portal (Teacher & Admin UI) |
| `h4yme/APIFINAL` | PHP + SQL Server (stored procs) | REST API backend |
| `h4yme/LiteRise` (this repo) | Android Java | Mobile student app |

**Web base URL config:** `Web.config → ApiBaseUrl` (default: `http://192.168.1.13/api`)
**Auth:** Session-based (`Session["AuthToken"]`, `Session["UserRole"]`, `Session["SchoolId"]`)

---

## Role Separation (Login)

```
/Login  (LoginController → portal_login.php)
    │
    ├── Role: ADMIN  → /Dashboard
    └── Role: TEACHER → /TeacherDashboard
```

**Layout:** `_Layout.cshtml` renders navigation conditionally by role.
- Admin-only nav items: Schools, Masterfile, Analytics, Administration
- Common items: Dashboard (role-aware URL), Students ("My Students" for teacher)

---

## Build Status Legend

| Symbol | Meaning |
|--------|---------|
| ✅ | Fully built and functional |
| ⚠️ | Stub or partial (controller + view exist, limited functionality) |
| ❌ | Not yet built |

---

## ADMIN PORTAL

### 🏠 `/Dashboard` — Admin Dashboard
**Controller:** `DashboardController`  **View:** `Views/Dashboard/Index.cshtml`
**Status:** ✅ Built

| Section | Data Source API |
|---------|----------------|
| Total Students card | `get_all_students.php` |
| Partner Schools card | `get_schools.php` |
| Active Students card | `get_all_students.php` (filter last_active ≤ 7 days) |
| Average XP card | `get_all_students.php` |
| Demographics charts (gender, age, by school) | `get_all_students.php` |
| Engagement charts (login activity, active/inactive) | `get_all_students.php` |
| Performance charts (ability growth, XP distribution) | `get_all_students.php` |
| Recent Activity feed | `log_session.php` |

**Enhancements Needed:**
- Pre/Post Assessment completion rates
- Average theta growth across all students
- Top performing schools
- Game engagement summary (most-played game types)

---

### 👥 `/Students` — Admin Student Management
**Controller:** `StudentController`  **Views:** `Views/Student/Index.cshtml`, `Views/Student/Details.cshtml`
**Status:** ✅ Built (index + details fully implemented)

#### `/Students` — Student List
| Column | Source Field |
|--------|-------------|
| Name | `FullName` |
| Grade | `grade` |
| School | `school_name` |
| Barangay | from school record |
| Level (Beginner/Intermediate/Advanced) | `placement_level` derived from `pre_theta` |
| Pre-θ | `pre_theta` |
| Post-θ | `post_theta` |
| XP | `total_xp` |
| Last Active | `last_active` |
| Status | active/inactive |

**Filters:** School, Placement Level, Status
**Export:** CSV of filtered data
**API:** `get_all_students.php`, `get_schools.php`

#### `/Students/Details/{id}` — Student Detail Page
**Controller method:** `StudentController.Details(int id)`
**Parallel API calls (Task.WhenAll):**
1. `get_portal_student.php?student_id={id}` — profile, XP, streaks, theta, level
2. `get_portal_placement_progress.php?student_id={id}` — pre/post assessment results
3. `get_portal_node_progress.php?student_id={id}` — 65-node completion grid
4. `get_portal_module_ladder.php?student_id={id}` — 5 modules × 13 nodes structure
5. `get_game_results.php?student_id={id}` — game history
6. `get_badges.php?student_id={id}` — earned badges

**Tabs implemented:**

**Tab 1 — Profile**
- Avatar (initials), name, nickname, DOB, gender, grade, school, barangay
- XP, streak days, node completion count, placement level badge, status

**Tab 2 — Assessment**
- Pre-Assessment card: theta, level, accuracy %, item count, date
- Post-Assessment card: same fields + growth indicator (↑/↓)
- Radar chart: 5 category scores (Phonics, Vocabulary, Grammar, Comprehension, Creating Text)
- Categories mapped from `SP_GetStudentProgress` stored proc output

**Tab 3 — Lessons**
- Overall progress bar: X / 65 nodes completed
- Module-by-module node grid: ✓ completed / ▶ in-progress / 🔒 locked
- Quiz scores per node (from `get_portal_module_ladder.php` → `quiz_score` field)
- 5 modules × 13 nodes = 65 total

**Tab 4 — Games**
- Table: game type, score, accuracy, XP earned, date
- Source: `get_game_results.php`
- 11 game types: SentenceScramble, StorySequencing, FillInTheBlanks, PictureMatch,
  DialogueReading, WordHunt, TimedTrail, MinimalPairs, SynonymSprint, WordExplosion, PhonicsNinja

**Tab 5 — Badges**
- Grid: badge icon, name, type (Module/XP/Streak/Achievement), date earned
- 15+ badges tracked in system

---

### 🏫 `/Schools` — School Management
**Controller:** `SchoolsController`  **View:** `Views/Schools/`
**Status:** ✅ Built (full CRUD)

| Feature | Status |
|---------|--------|
| Schools table (Name, ID, Barangay, % of Students) | ✅ |
| Add School modal | ✅ |
| Edit School modal | ✅ |
| Remove School modal | ✅ |
| School Details modal | ✅ |
| Per-school report page (avg theta, lesson completion, top performers) | ❌ Needs build |

**API:** `get_schools.php`
**Enhancement needed:** Per-school drill-down with student roster, avg pre/post theta, lesson completion rate.

---

### 📊 `/Analytics` — Admin Analytics
**Controller:** `AnalyticsController`  **View:** `Views/Analytics/AnalyticsView.cshtml`
**Status:** ✅ Existing tabs built; 2 new tabs needed

| Tab | Status | Description |
|-----|--------|-------------|
| Demographics | ✅ | Age groups, gender, by school + barangay filter |
| Engagement | ✅ | Streak distribution, active/inactive, login frequency |
| Performance | ✅ | Ability growth trend, XP distribution |
| **Assessment** | ❌ New | Theta distribution, level distribution, category radar, pre vs post growth |
| **Games** | ❌ New | Play count per game type, avg accuracy, XP via games over time |

**New Assessment Tab specs:**
- Pre-Assessment completion map (school × barangay heatmap)
- Theta distribution histogram (all students)
- Level distribution pie chart (Beginner/Intermediate/Advanced)
- Category scores radar chart (avg per category across all students)
- Pre vs Post theta growth per school (grouped bar chart)
- Source: `get_all_students.php` + `get_portal_placement_progress.php`

**New Games Tab specs:**
- Play count per game type (bar chart, 11 game types)
- Average accuracy per game (horizontal bar)
- XP earned via games over time (line chart)
- Source: `get_game_results.php`

---

### 📚 `/Masterfile` — Content Management
**Controller:** `MasterfileController`  **View:** `Views/Masterfile/MasterfileView.cshtml`
**Status:** ⚠️ Stats-only display, no CRUD

#### Lessons & Modules Tab
**Status:** ❌ Needs build

| Feature | Details |
|---------|---------|
| Module list | 5 modules, title, node count, enabled/disabled status |
| Lesson nodes per module | 13 nodes each, title, type, order, enabled/disabled |
| Enable / Disable lessons | Toggle per node |
| Lesson content preview | Read-only view of lesson text/media |

**API needed:** New endpoint or extend `get_portal_module_ladder.php`

#### Question Bank Tab
**Status:** ❌ Needs build

**Placement Questions (IRT-based):**
| Column | Field |
|--------|-------|
| ID | question_id |
| Category | category (Phonics/Vocabulary/Grammar/Comprehension/Creating Text) |
| Difficulty (b) | IRT b-parameter |
| Discrimination (a) | IRT a-parameter |
| Type | multiple_choice / pronunciation / reading |
| Preview | text/image preview |
| Active | enabled flag |

Actions: Add / Edit / Deactivate / Delete / Import CSV
Source: `get_preassessment_items.php` (read) — write endpoints needed

**Quiz Questions (per-lesson):**
- View by lesson/node
- Add / Edit / Remove
- Source: `get_quiz_questions.php`

#### Badges Tab
**Status:** ❌ Needs build

| Feature | Details |
|---------|---------|
| Badge list | name, icon, criteria, type (Module/XP/Streak/Achievement) |
| Add / Edit / Remove badge | CRUD modal |
| Badge award history | who earned what and when |

**API:** `get_badges.php` (read) — write endpoints needed

#### Administrators Tab
**Status:** ⚠️ Counts only
**Controller:** `AdministrationController`  **View:** `Views/Administration/`

| Feature | Status |
|---------|--------|
| Admin accounts list (name, email, role, last login) | ❌ |
| Add Admin / Teacher account | ❌ |
| Edit role & permissions | ❌ |
| Deactivate account | ❌ |

---

### 🔔 `/Notifications` — Notification Center
**Controller:** `NotificationsController`  **View:** `Views/Notifications/NotificationsView.cshtml`
**Status:** ⚠️ Stub (empty view)

| Feature | Status |
|---------|--------|
| Notification list (sent/scheduled) with Title, Target, Sent At, Type columns | ❌ |
| Compose & Send (target: All/By School/By Grade, type: Info/Achievement/Reminder) | ❌ |
| Schedule or send immediately toggle | ❌ |
| Notification templates | ❌ |

**API needed:** New notification endpoints in APIFINAL

---

### 📋 `/Reports` — Report Generation
**Controller:** ❌ Not yet created
**Status:** ❌ Not yet built

| Report | Format | Source |
|--------|--------|--------|
| Student Progress Report (pre vs post, category breakdown) | PDF | `get_portal_placement_progress.php` |
| School Summary Report (all schools, avg theta, lesson completion, XP) | CSV + PDF | `get_all_students.php`, `get_schools.php` |
| System Usage Report (login frequency, session durations) | CSV | `log_session.php` |

---

### ⚙️ `/Settings` — Admin Settings
**Controller:** `SettingsController`  **View:** `Views/Settings/SettingsView.cshtml`
**Status:** ⚠️ Stub (empty view)

| Feature | Status |
|---------|--------|
| Profile (name, email, change password) | ❌ |
| System Config: passing threshold, max lessons before post-assessment unlock | ❌ |
| IRT parameters (min/max items, SEM target) | ❌ |
| Audit Logs (admin action log) | ❌ |

---

## TEACHER PORTAL

### 🏠 `/TeacherDashboard` — Teacher Dashboard
**Controller:** `TeacherDashboardController`  **View:** `Views/TeacherDashboard/Index.cshtml`
**Status:** ✅ Built

| Section | Source |
|---------|--------|
| Total students assigned | `get_all_students.php` filtered by `school_id` from session |
| Pre-assessment completion count | filtered student list |
| Average pre-theta | calculated from student list |
| Average lessons completed | calculated from student list |
| Level distribution doughnut chart | Beginner/Intermediate/Advanced counts |
| Pre-theta vs Post-theta bar chart (top 15 students) | student list fields |
| "Needs Attention" students (inactive 7+ days or missing pre-assessment) | filtered |
| Recent students table (10 rows): name, grade, level, scores, lessons, XP, last active | student list |

**Linked to:** `/TeacherStudents/Details/{id}` per student row

---

### 👥 `/TeacherStudents` — My Students
**Controller:** `TeacherStudentsController`  **View:** `Views/TeacherStudents/Index.cshtml`
**Status:** ✅ Built

#### `/TeacherStudents` — Student Roster
| Column | Source Field |
|--------|-------------|
| Name | `FullName` |
| Grade | `grade` |
| Level badge | `placement_level` |
| Pre-θ | `pre_theta` |
| Post-θ | `post_theta` |
| Lessons Done | `lessons_done` |
| XP | `total_xp` |
| Last Active | `last_active` |

**Filters:** Placement Level, Status (Active/Inactive), Search by name
**Export:** CSV of filtered data
**API:** `get_all_students.php` filtered by teacher's `school_id`

#### `/TeacherStudents/Details/{id}` — Student Detail Page
**Controller method:** `TeacherStudentsController.Details(int id)`
**Same 6 parallel API calls as AdminStudentController.Details**
**Same tabs as Admin student detail: Profile, Assessment, Lessons, Games, Badges**

> **Note:** Teachers see the same detail page as admins but without edit/CRUD controls.

---

### 📊 `/TeacherAnalytics` — Class Analytics
**Controller:** ❌ Not yet created
**View:** ❌ Not yet created
**Status:** ❌ Needs build

| Tab | Content | API |
|-----|---------|-----|
| Overview | Class avg pre/post theta, level distribution pie, lesson completion bar per module | `get_all_students.php`, `get_portal_placement_progress.php` |
| Skills | Category scores bar chart (class avg per category), per-student category heatmap table | `get_portal_placement_progress.php` |
| Engagement | Weekly login frequency, streak distribution, XP over time | `get_all_students.php`, `log_session.php` |

---

### 📋 `/TeacherReports` — Progress Reports
**Controller:** ❌ Not yet created
**Status:** ❌ Needs build

| Report | Format | Source |
|--------|--------|--------|
| Individual student report (pre/post comparison, category scores, lesson progress) | PDF | Multiple portal APIs |
| Class summary report (all students, avg scores, completion rates) | PDF | `get_all_students.php` |

---

### ⚙️ `/TeacherSettings` — Teacher Settings
**Controller:** ❌ Not yet created
**Status:** ❌ Needs build

| Feature | Details |
|---------|---------|
| Profile (name, email, change password) | Basic account management |
| View assigned students | List of students tied to teacher's school |
| Request to add/remove student from class | Sends request to admin |

---

## Priority Build Order

| Priority | Module | Portal | Status | Effort |
|----------|--------|--------|--------|--------|
| 🔴 1 | Student Detail Page — already built, verify data binding is complete | Both | ✅ Review | Low |
| 🔴 2 | Analytics → Assessment Tab (theta distribution, level distribution, growth by school) | Admin | ❌ Build | Medium |
| 🔴 3 | Analytics → Games Tab (play count, accuracy, XP per game type) | Admin | ❌ Build | Medium |
| 🟡 4 | Teacher Class Analytics (`/TeacherAnalytics`) | Teacher | ❌ Build | Medium |
| 🟡 5 | Masterfile → Question Bank CRUD | Admin | ❌ Build | High |
| 🟡 6 | Masterfile → Lessons/Modules management (enable/disable nodes) | Admin | ❌ Build | Medium |
| 🟡 7 | Masterfile → Badges CRUD | Admin | ❌ Build | Medium |
| 🟡 8 | Administration Tab — Admin account CRUD | Admin | ❌ Build | Medium |
| 🟢 9 | Notifications (compose + push send) | Admin | ⚠️ Stub | High |
| 🟢 10 | Reports — PDF/CSV export | Both | ❌ Build | High |
| 🟢 11 | Settings — profile + system config + IRT params | Both | ⚠️ Stub | Medium |
| 🟢 12 | Schools → per-school drill-down report | Admin | ❌ Build | Low |

---

## API Endpoint Map

### Portal-Specific Endpoints (APIFINAL)

| Endpoint | Method | Auth | Returns | Used By |
|----------|--------|------|---------|---------|
| `portal_login.php` | POST | — | JWT token + role | Login |
| `get_all_students.php` | GET | Bearer | Student list w/ theta, XP, level, last_active | Dashboard, Students index |
| `get_portal_student.php?student_id=` | GET | Bearer | Full profile: name, grade, school, XP, streak, theta, level, lessons_done | Student Details |
| `get_portal_placement_progress.php?student_id=` | GET | Bearer | `{pre_assessment, post_assessment}` with theta, level, accuracy, date | Student Details → Assessment tab |
| `get_portal_node_progress.php?student_id=` | GET | Bearer | Node progress array (status per node) | Student Details → Lessons tab |
| `get_portal_module_ladder.php?student_id=` | GET | Bearer | 5 modules × 13 nodes with completion + quiz_score | Student Details → Lessons tab |
| `get_game_results.php?student_id=` | GET | Bearer | Game history: type, score, accuracy, XP, date | Student Details → Games tab |
| `get_badges.php?student_id=` | GET | Bearer | Badge list with earned status + date | Student Details → Badges tab |
| `get_schools.php` | GET | Bearer | Schools list | Schools page |
| `get_preassessment_items.php` | POST | Bearer | IRT question bank | Masterfile → Question Bank |
| `get_quiz_questions.php` | GET | Bearer | Quiz questions per lesson | Masterfile → Question Bank |
| `log_session.php` | POST | Bearer | — | Analytics |
| `save_game_results.php` | POST | Bearer | — | (mobile only) |
| `award_badge.php` | POST | Bearer | — | (mobile only) |

### Endpoints Still Needed (Not Yet in APIFINAL)

| Endpoint | Purpose | Priority |
|----------|---------|----------|
| `get_game_summary.php` | Aggregate game stats by type across all students | Analytics → Games tab |
| `get_assessment_summary.php` | Aggregate theta/level distribution across all students | Analytics → Assessment tab |
| `create_notification.php` | Save + push notification to students | Notifications |
| `get_notifications.php` | List sent/scheduled notifications | Notifications |
| `update_question_bank.php` | Add/edit/deactivate placement questions | Masterfile → Question Bank |
| `manage_badges.php` | CRUD for badge definitions | Masterfile → Badges |
| `manage_admins.php` | CRUD for admin/teacher accounts | Administration tab |
| `get_system_config.php` | Read system settings (IRT params, thresholds) | Settings |
| `update_system_config.php` | Write system settings | Settings |
| `get_audit_log.php` | Admin action audit trail | Settings → Audit Log |

---

## Data Model Reference (from Android App)

### 5 Learning Categories
1. Phonics
2. Vocabulary
3. Grammar
4. Comprehension
5. Creating Text

### Curriculum Structure
- **5 Modules** × **13 Nodes** = **65 total lesson nodes**
- Each node has 4 adaptive quiz paths: Intervention → Supplemental → Proceed → Enrichment
- Post-assessment unlocks after all 65 nodes completed (`check_modules_complete.php`)

### IRT Thresholds (theta)
| Range | Level |
|-------|-------|
| θ < -0.5 | Beginner |
| -0.5 ≤ θ ≤ 0.5 | Intermediate |
| θ > 0.5 | Advanced |

### 11 Game Types
1. SentenceScramble
2. StorySequencing
3. FillInTheBlanks
4. PictureMatch
5. DialogueReading
6. WordHunt
7. TimedTrail
8. MinimalPairs
9. SynonymSprint
10. WordExplosion
11. PhonicsNinja

### Badge Categories
- Module completion badges (5 — one per module)
- XP milestone badges
- Streak milestone badges
- Achievement badges
- Total: 15+

---

## Current File Structure (LiteriseWeb)

```
Website/
├── Controllers/
│   ├── BaseController.cs               ✅
│   ├── LoginController.cs              ✅
│   ├── DashboardController.cs          ✅ Admin dashboard
│   ├── StudentController.cs            ✅ Admin students (index + details)
│   ├── SchoolsController.cs            ✅ Full CRUD
│   ├── AnalyticsController.cs          ✅ 3 tabs (+ 2 new tabs needed)
│   ├── MasterfileController.cs         ⚠️ Stats only
│   ├── AdministrationController.cs     ⚠️ Partial
│   ├── NotificationsController.cs      ⚠️ Stub
│   ├── SettingsController.cs           ⚠️ Stub
│   ├── TeacherDashboardController.cs   ✅ Full KPIs + charts
│   └── TeacherStudentsController.cs    ✅ Roster + details
│
├── Services/
│   ├── ApiService.cs                   ✅ All portal API calls
│   └── AuthService.cs                  ✅
│
├── Views/
│   ├── Dashboard/Index.cshtml          ✅
│   ├── Student/Index.cshtml            ✅
│   ├── Student/Details.cshtml          ✅ Full 5-tab profile
│   ├── Schools/                        ✅
│   ├── Analytics/AnalyticsView.cshtml  ✅ (expand with 2 tabs)
│   ├── Masterfile/MasterfileView.cshtml ⚠️
│   ├── Administration/                 ⚠️
│   ├── Notifications/NotificationsView.cshtml ⚠️ Empty
│   ├── Settings/SettingsView.cshtml    ⚠️ Empty
│   ├── TeacherDashboard/Index.cshtml   ✅
│   ├── TeacherStudents/Index.cshtml    ✅
│   └── Shared/_Layout.cshtml           ✅ Role-aware nav
│
├── Content/ (CSS)
│   ├── DashboardStyles.css             ✅
│   ├── StudentStyles.css               ✅
│   ├── SchoolsStyles.css               ✅
│   ├── AnalyticsStyles.css             ✅
│   ├── MasterfileStyles.css            ⚠️
│   └── AdministrationStyles.css        ⚠️
│
└── Scripts/ (JS)
    ├── DashboardScript.js              ✅
    ├── StudentScript.js                ✅
    ├── SchoolsScript.js                ✅
    ├── AnalyticsScript.js              ✅ (expand)
    ├── MasterfileScript.js             ⚠️
    └── AdministrationScript.js         ⚠️
```

---

*Generated from analysis of h4yme/LiteriseWeb, h4yme/APIFINAL, and h4yme/LiteRise (Android app).*
