# LiteRise API Documentation

Complete REST API for the LiteRise adaptive literacy assessment system.

## 📋 Table of Contents

- [Setup](#setup)
- [Authentication](#authentication)
- [Endpoints](#endpoints)
- [Error Handling](#error-handling)
- [Testing](#testing)

---

## 🚀 Setup

### Prerequisites

1. **XAMPP** with PHP 7.4+ and Apache
2. **SQL Server** with LiteRiseDB database
3. **Microsoft SQL Server PDO Driver** for PHP

### Installation Steps

1. **Copy API files to XAMPP htdocs**
   ```bash
   cp -r /path/to/LiteRise/htdocs/api C:/xampp/htdocs/api
   ```

2. **Configure .env file**
   ```bash
   cd C:/xampp/htdocs/api
   # Edit .env with your database credentials
   ```

3. **Install SQL Server PDO Driver**
   - Download from: https://docs.microsoft.com/en-us/sql/connect/php/download-drivers-php-sql-server
   - Copy .dll files to `C:/xampp/php/ext/`
   - Enable in `php.ini`:
     ```ini
     extension=php_pdo_sqlsrv_74_ts_x64.dll
     extension=php_sqlsrv_74_ts_x64.dll
     ```

4. **Restart Apache**
   ```bash
   # From XAMPP Control Panel, restart Apache
   ```

5. **Test connection**
   ```bash
   curl http://localhost/api/test_db.php
   ```

---

## 🔐 Authentication

All endpoints (except `login.php` and `test_db.php`) require JWT authentication.

### Login

**POST** `/api/login.php`

**Request:**
```json
{
  "email": "student@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "success": true,
  "StudentID": 1,
  "FullName": "John Doe",
  "email": "student@example.com",
  "GradeLevel": 5,
  "CurrentAbility": 0.5,
  "TotalXP": 1200,
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Using JWT Token

Include in Authorization header:
```
Authorization: Bearer <your_jwt_token>
```

Or as query parameter:
```
http://localhost/api/endpoint.php?token=<your_jwt_token>
```

---

## 📡 Endpoints

### 1. Test Database Connection

**GET** `/api/test_db.php`

**Authentication:** Not required

**Response:**
```json
{
  "success": true,
  "message": "Database connection successful",
  "tests": {
    "connection": "✅ Connected",
    "students_table": "✅ 3 students found"
  }
}
```

---

### 2. Student Login

**POST** `/api/login.php`

**Authentication:** Not required

**Request:**
```json
{
  "email": "student@example.com",
  "password": "password123"
}
```

---

### 3. Create Test Session

**POST** `/api/create_session.php`

**Authentication:** Required

**Request:**
```json
{
  "student_id": 1,
  "type": "PreAssessment"
}
```

**Session Types:**
- `PreAssessment`
- `Lesson`
- `PostAssessment`
- `Game`

**Response:**
```json
{
  "success": true,
  "session": {
    "SessionID": 123,
    "StudentID": 1,
    "SessionType": "PreAssessment",
    "InitialTheta": 0.5,
    "StartTime": "2024-11-17 10:30:00"
  }
}
```

---

### 4. Get Pre-Assessment Items

**POST** `/api/get_preassessment_items.php`

**Authentication:** Required

**Response:**
```json
{
  "success": true,
  "count": 20,
  "items": [
    {
      "ItemID": 1,
      "ItemText": "Choose the correct spelling:",
      "ItemType": "Spelling",
      "DifficultyLevel": "Easy",
      "AnswerChoices": ["receive", "recieve", "recive"],
      "OptionA": "receive",
      "OptionB": "recieve",
      "OptionC": "recive",
      "CorrectOption": "A"
    }
  ]
}
```

---

### 5. Submit Responses

**POST** `/api/submit_responses.php`

**Authentication:** Required

**Request:**
```json
{
  "session_id": 123,
  "student_id": 1,
  "responses": [
    {
      "ItemID": 1,
      "Response": "A",
      "IsCorrect": 1,
      "TimeSpent": 15
    }
  ]
}
```

**Response:**
```json
{
  "success": true,
  "session_id": 123,
  "total_responses": 20,
  "correct_answers": 15,
  "accuracy": 75.0,
  "ability": {
    "initial_theta": 0.5,
    "final_theta": 0.75,
    "classification": "Proficient",
    "standard_error": 0.32
  }
}
```

---

### 6. Update Student Ability

**POST** `/api/update_ability.php`

**Authentication:** Required

**Request:**
```json
{
  "student_id": 1,
  "session_id": 123
}
```

**Response:**
```json
{
  "success": true,
  "ability": 0.75,
  "classification": "Proficient",
  "standard_error": 0.32,
  "responses_analyzed": 45
}
```

---

### 7. Get Student Progress

**POST** `/api/get_student_progress.php`

**Authentication:** Required

**Request:**
```json
{
  "student_id": 1
}
```

**Response:**
```json
{
  "success": true,
  "student": {
    "StudentID": 1,
    "FullName": "John Doe",
    "CurrentAbility": 0.75,
    "Classification": "Proficient",
    "TotalXP": 1200,
    "CurrentStreak": 5
  },
  "stats": {
    "TotalSessions": 15,
    "AverageAccuracy": 78.5,
    "TotalBadges": 5
  },
  "recent_sessions": [],
  "badges": []
}
```

---

### 8. Get Lessons

**POST** `/api/get_lessons.php`

**Authentication:** Required

**Request:**
```json
{
  "student_id": 1
}
```

**Response:**
```json
{
  "success": true,
  "student_ability": 0.75,
  "classification": "Proficient",
  "lessons": [
    {
      "LessonID": 1,
      "LessonTitle": "Reading Comprehension Basics",
      "RequiredAbility": 0.5,
      "IsUnlocked": true,
      "CompletionStatus": "NotStarted"
    }
  ]
}
```

---

### 9. Get Game Data

**POST** `/api/get_game_data.php`

**Authentication:** Required

**Request:**
```json
{
  "student_id": 1,
  "game_type": "SentenceScramble",
  "count": 10
}
```

**Game Types:**
- `SentenceScramble`
- `TimedTrail`

**Response:**
```json
{
  "success": true,
  "game_type": "SentenceScramble",
  "items": [
    {
      "ItemID": 1,
      "ItemText": "homework / diligently / finished / her / Maria",
      "CorrectAnswer": "Maria diligently finished her homework.",
      "Words": ["homework", "diligently", "finished", "her", "Maria"]
    }
  ]
}
```

---

### 10. Save Game Result

**POST** `/api/save_game_result.php`

**Authentication:** Required

**Request:**
```json
{
  "session_id": 123,
  "student_id": 1,
  "game_type": "SentenceScramble",
  "score": 850,
  "accuracy_percentage": 85.0,
  "time_completed": 120,
  "xp_earned": 100,
  "streak_achieved": 7
}
```

**Response:**
```json
{
  "success": true,
  "game_result_id": 456,
  "student": {
    "TotalXP": 1300,
    "CurrentStreak": 7
  },
  "badges_unlocked": []
}
```

---

## ❌ Error Handling

All errors follow this format:

```json
{
  "success": false,
  "error": "Error message here",
  "details": "Detailed error (only in debug mode)"
}
```

### HTTP Status Codes

- `200` - Success
- `201` - Created
- `400` - Bad Request (validation error)
- `401` - Unauthorized (missing/invalid token)
- `403` - Forbidden (not allowed)
- `404` - Not Found
- `500` - Internal Server Error

---

## 🧪 Testing

### Using cURL

**Test Database Connection:**
```bash
curl http://localhost/api/test_db.php
```

**Login:**
```bash
curl -X POST http://localhost/api/login.php \
  -H "Content-Type: application/json" \
  -d '{"email":"maria.santos@student.com","password":"password123"}'
```

**Get Pre-Assessment Items (with auth):**
```bash
curl -X POST http://localhost/api/get_preassessment_items.php \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Using Postman

1. Import the collection (if provided)
2. Set `BASE_URL` variable to `http://localhost/api`
3. Login first to get JWT token
4. Set token in Authorization header for other requests

---

## 🔧 Troubleshooting

### "Database connection failed"
- Check SQL Server is running
- Verify credentials in `.env`
- Ensure SQL Server PDO drivers are installed

### "Invalid or expired token"
- Login again to get new token
- Check token is included in Authorization header
- Verify JWT_SECRET in `.env` hasn't changed

### "Stored procedure not found"
- Run the database schema script
- Check database name in `.env`

---

## 📞 Support

For issues, contact the development team or check the GitHub repository.

---

## 📄 License

Copyright © 2024 LiteRise. All rights reserved.
