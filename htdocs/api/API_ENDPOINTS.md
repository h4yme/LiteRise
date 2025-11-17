# 📡 LiteRise API Endpoints - Complete Reference

## Overview

**Total Endpoints:** 21
**Base URL:** `http://your-server/api/`
**Format:** JSON
**Authentication:** Email/Password (separate for Students & Teachers)

---

## 📊 Endpoint Summary

### Student Endpoints (11)
| # | Endpoint | Method | Purpose |
|---|----------|--------|---------|
| 1 | `/login.php` | POST | Student authentication |
| 2 | `/create_session.php` | POST | Create test session |
| 3 | `/get_preassessment_items.php` | POST | Get assessment questions |
| 4 | `/submit_responses.php` | POST | Submit answers with IRT |
| 5 | `/update_ability.php` | POST | Manual ability update |
| 6 | `/get_student_progress.php` | GET/POST | Get progress stats |
| 7 | `/get_student_badges.php` | GET/POST | Get earned badges |
| 8 | `/get_session_history.php` | GET/POST | Get past sessions |
| 9 | `/change_password.php` | POST | Change password |
| 10 | `/update_profile.php` | POST | Update profile info |
| 11 | `/get_lessons.php` | GET/POST | Get lesson recommendations |

### Game Endpoints (2)
| # | Endpoint | Method | Purpose |
|---|----------|--------|---------|
| 12 | `/get_game_data.php` | GET/POST | Get game questions |
| 13 | `/save_game_result.php` | POST | Save game score + XP |

### Badge & Leaderboard Endpoints (2)
| # | Endpoint | Method | Purpose |
|---|----------|--------|---------|
| 14 | `/get_badges.php` | GET | Get all available badges |
| 15 | `/get_leaderboard.php` | GET | Get top students by XP |

### Teacher Endpoints (4)
| # | Endpoint | Method | Purpose |
|---|----------|--------|---------|
| 16 | `/teacher_login.php` | POST | Teacher authentication |
| 17 | `/get_students_by_teacher.php` | GET/POST | Get teacher's students |
| 18 | `/get_class_statistics.php` | GET/POST | Get class analytics |
| 19 | `/get_item_details.php` | GET/POST | Get question details |

### Utility Endpoints (2)
| # | Endpoint | Method | Purpose |
|---|----------|--------|---------|
| 20 | `/test_db.php` | GET | Test database connection |
| 21 | `/irt.php` | N/A | IRT Calculator class (imported) |

---

## 🔄 Typical User Flows

### Flow 1: Student Assessment
```
1. POST /login.php → Get StudentID
2. POST /create_session.php → Get SessionID
3. POST /get_preassessment_items.php → Get 20 questions
4. [Student answers questions]
5. POST /submit_responses.php → Calculate IRT, get final θ
6. GET /get_student_progress.php → View stats
```

### Flow 2: Teacher Dashboard
```
1. POST /teacher_login.php → Get TeacherID
2. GET /get_students_by_teacher.php → Get student list
3. GET /get_class_statistics.php → View analytics
4. [Select student] → GET /get_session_history.php
```

### Flow 3: Gamification
```
1. [Student logged in]
2. GET /get_game_data.php?GameType=SentenceScramble
3. [Student plays game]
4. POST /save_game_result.php → Earn XP, check badge unlocks
5. GET /get_student_badges.php → View earned badges
6. GET /get_leaderboard.php → Compare with peers
```

---

## 🎯 Endpoint Categories by Function

### Authentication & Profile
- `/login.php` - Student login
- `/teacher_login.php` - Teacher login
- `/change_password.php` - Password management
- `/update_profile.php` - Profile updates

### Assessment & IRT
- `/create_session.php` - Initialize test
- `/get_preassessment_items.php` - Adaptive question selection
- `/submit_responses.php` - IRT calculation & scoring
- `/update_ability.php` - Manual θ adjustment

### Progress & Analytics
- `/get_student_progress.php` - Individual stats
- `/get_session_history.php` - Past assessments
- `/get_class_statistics.php` - Teacher analytics
- `/get_students_by_teacher.php` - Class roster

### Gamification
- `/get_badges.php` - Badge catalog
- `/get_student_badges.php` - Earned badges
- `/get_leaderboard.php` - XP rankings
- `/save_game_result.php` - Game completion

### Content Delivery
- `/get_lessons.php` - Personalized lessons
- `/get_game_data.php` - Game questions
- `/get_item_details.php` - Question metadata

### System
- `/test_db.php` - Health check
- `/irt.php` - IRT engine (class)

---

## 📈 Response Patterns

### Success Response (200)
```json
{
  "success": true,
  "data": { ... },
  "message": "Optional success message"
}
```

### Error Response (4xx/5xx)
```json
{
  "error": "Error description",
  "details": "Optional technical details"
}
```

### IRT Response Format
```json
{
  "success": true,
  "SessionID": 1,
  "FinalTheta": 0.23,
  "InitialTheta": 0.0,
  "ThetaChange": 0.23,
  "Accuracy": 75.0,
  "StandardError": 0.45,
  "Reliability": 0.85
}
```

---

## 🔐 Security Implementation

### Current (Development)
- ✅ Input validation (type checking, sanitization)
- ✅ SQL injection prevention (PDO prepared statements)
- ✅ CORS headers for cross-origin requests
- ✅ Email format validation
- ✅ Password length requirements (min 6 chars)
- ✅ Active account checking (`IsActive = 1`)

### Required for Production
- ⚠️ **HTTPS only** - Encrypt all traffic
- ⚠️ **Password hashing** - Use `password_hash()` and `password_verify()`
- ⚠️ **Rate limiting** - Prevent brute force attacks
- ⚠️ **JWT tokens** - Replace session-based auth
- ⚠️ **CORS restriction** - Limit to specific domains
- ⚠️ **Input sanitization** - Enhanced XSS prevention
- ⚠️ **Error hiding** - Disable detailed error messages
- ⚠️ **API key authentication** - Add API key requirement

---

## 🧪 Testing Tools

### Quick Test (Any Endpoint)
```bash
curl -X POST http://localhost/api/login.php \
  -H "Content-Type: application/json" \
  -d '{"email":"maria.santos@student.com","password":"password123"}'
```

### Full Test Suite
```bash
bash test_api.sh
```

### Test with jq (Pretty Print)
```bash
curl -s http://localhost/api/get_badges.php | jq .
```

---

## 📊 Database Dependencies

### Stored Procedures Used
- `SP_CreateTestSession` - Create session
- `SP_GetPreAssessmentItems` - Get questions
- `SP_SaveResponses` - (Not used, manual INSERT)
- `SP_UpdateStudentAbility` - Update θ
- `SP_GetStudentProgress` - Progress stats
- `SP_GetLessonsByAbility` - Lesson recommendations
- `SP_GetSentenceScrambleData` - Game data
- `SP_SaveGameResult` - Save game
- `SP_CheckBadgeUnlock` - Badge checking

### Tables Accessed
- **Students** - Student accounts
- **Teachers** - Teacher accounts
- **Items** - Assessment questions
- **TestSessions** - Test sessions
- **Responses** - Student answers
- **StudentBadges** - Earned badges
- **Badges** - Badge definitions
- **Lessons** - Lesson content
- **GameResults** - Game scores
- **SentenceScramble** - Game data

---

## 🚀 Performance Considerations

### Optimizations Implemented
- ✅ Indexed queries (ItemID, StudentID, SessionID)
- ✅ Prepared statements (query caching)
- ✅ Efficient joins (INNER JOIN over subqueries)
- ✅ Result limits (TOP N, max 100)
- ✅ Selective column fetching (only needed fields)

### Potential Bottlenecks
- ⚠️ IRT calculation for large response sets (20+ items)
- ⚠️ Leaderboard queries on large datasets (>1000 students)
- ⚠️ Class statistics aggregation (multiple subqueries)

### Recommended Caching
- Badge list (rarely changes)
- Leaderboard (refresh every 5 minutes)
- Class statistics (refresh hourly)
- IRT item parameters (static data)

---

## 📝 Sample Credentials

### Students
```
maria.santos@student.com / password123
juan.delacruz@student.com / password123
ana.reyes@student.com / password123
```

### Teachers
```
elena.torres@teacher.com / password123
carlos.mendoza@teacher.com / password123
```

---

## 🔗 Related Documentation

- **[README.md](README.md)** - Detailed endpoint documentation with cURL examples
- **[../../QUICK_START.md](../../QUICK_START.md)** - 30-minute setup guide
- **[../../SETUP_GUIDE.md](../../SETUP_GUIDE.md)** - Complete installation instructions
- **[../../database/schema.sql](../../database/schema.sql)** - Database structure

---

## 📞 Support

**Issue tracking:** Check logs in `/var/log/apache2/error.log`
**Database issues:** Verify connection in `src/db.php`
**API testing:** Use `test_api.sh` for automated testing

---

**Last Updated:** January 2025
**API Version:** 1.0
**Status:** Production-ready (with security enhancements)
