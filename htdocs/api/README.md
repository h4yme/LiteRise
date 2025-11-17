# 🔌 LiteRise API Documentation

## Quick Start

### 1. Test Database Connection

```bash
curl http://localhost/api/test_db.php
```

**Expected Response:**
```json
{
  "status": "success",
  "message": "Database connection successful",
  "database": "LiteRiseDB",
  "statistics": {
    "TotalStudents": 3,
    "TotalItems": 11
  }
}
```

---

## 📋 API Endpoints

### Base URL
```
http://your-server-ip/api/
```

---

## 1. Student Login

**Endpoint:** `POST /login.php`

**Request:**
```json
{
  "email": "maria.santos@student.com",
  "password": "password123"
}
```

**cURL Test:**
```bash
curl -X POST http://localhost/api/login.php \
  -H "Content-Type: application/json" \
  -d '{
    "email": "maria.santos@student.com",
    "password": "password123"
  }'
```

**Success Response (200):**
```json
{
  "StudentID": 1,
  "FullName": "Maria Santos",
  "FirstName": "Maria",
  "LastName": "Santos",
  "email": "maria.santos@student.com",
  "GradeLevel": 4,
  "Section": "A",
  "AbilityScore": 0.0,
  "XP": 0,
  "CurrentStreak": 0,
  "LongestStreak": 0
}
```

**Error Response (401):**
```json
{
  "error": "Invalid credentials"
}
```

---

## 2. Create Test Session

**Endpoint:** `POST /create_session.php`

**Request:**
```json
{
  "StudentID": 1,
  "SessionType": "PreAssessment"
}
```

**cURL Test:**
```bash
curl -X POST http://localhost/api/create_session.php \
  -H "Content-Type: application/json" \
  -d '{
    "StudentID": 1,
    "SessionType": "PreAssessment"
  }'
```

**Success Response (201):**
```json
{
  "SessionID": 1,
  "StudentID": 1,
  "SessionType": "PreAssessment",
  "InitialTheta": 0.0,
  "StartTime": "2024-01-15T10:30:00"
}
```

**Valid Session Types:**
- `PreAssessment`
- `Lesson`
- `PostAssessment`
- `Game`

---

## 3. Get Pre-Assessment Items

**Endpoint:** `POST /get_preassessment_items.php`

**Request:** (No body required)

**cURL Test:**
```bash
curl -X POST http://localhost/api/get_preassessment_items.php \
  -H "Content-Type: application/json"
```

**Success Response (200):**
```json
[
  {
    "ItemID": 1,
    "PassageText": null,
    "QuestionText": "homework / diligently / finished / her / Maria",
    "OptionA": "",
    "OptionB": "",
    "OptionC": "",
    "OptionD": "",
    "CorrectOption": "",
    "Difficulty": -1.0,
    "Discrimination": 1.5,
    "Guessing": 0.0,
    "ItemType": "Syntax",
    "DifficultyLevel": "Easy"
  },
  {
    "ItemID": 6,
    "PassageText": null,
    "QuestionText": "Choose the correct spelling:",
    "OptionA": "recieve",
    "OptionB": "receive",
    "OptionC": "recive",
    "OptionD": "",
    "CorrectOption": "B",
    "Difficulty": -0.5,
    "Discrimination": 1.3,
    "Guessing": 0.33,
    "ItemType": "Spelling",
    "DifficultyLevel": "Easy"
  }
]
```

Returns **20 items** ordered by difficulty (easy → hard).

---

## 4. Submit Assessment Responses

**Endpoint:** `POST /submit_responses.php`

**Request:**
```json
{
  "StudentID": 1,
  "SessionID": 1,
  "Responses": [
    {
      "ItemID": 1,
      "SelectedOption": "A",
      "Correct": true,
      "TimeTakenSec": 15.5
    },
    {
      "ItemID": 2,
      "SelectedOption": "B",
      "Correct": false,
      "TimeTakenSec": 22.3
    }
  ]
}
```

**cURL Test:**
```bash
curl -X POST http://localhost/api/submit_responses.php \
  -H "Content-Type: application/json" \
  -d '{
    "StudentID": 1,
    "SessionID": 1,
    "Responses": [
      {
        "ItemID": 6,
        "SelectedOption": "B",
        "Correct": true,
        "TimeTakenSec": 15.5
      },
      {
        "ItemID": 9,
        "SelectedOption": "A",
        "Correct": false,
        "TimeTakenSec": 12.0
      }
    ]
  }'
```

**Success Response (200):**
```json
{
  "success": true,
  "SessionID": 1,
  "FinalTheta": 0.1234,
  "InitialTheta": 0.0,
  "ThetaChange": 0.1234,
  "TotalQuestions": 2,
  "CorrectAnswers": 1,
  "Accuracy": 50.0,
  "StandardError": 0.4523,
  "Reliability": 0.6234,
  "message": "Assessment completed successfully"
}
```

**IRT Calculation Details:**
- **FinalTheta**: Updated ability estimate (-4 to +4 scale)
- **StandardError**: Measurement precision (lower is better)
- **Reliability**: Test reliability (0-1, higher is better)

---

## 5. Get Student Progress

**Endpoint:** `GET /get_student_progress.php?StudentID=1`
**Or:** `POST /get_student_progress.php`

**cURL Test (GET):**
```bash
curl http://localhost/api/get_student_progress.php?StudentID=1
```

**cURL Test (POST):**
```bash
curl -X POST http://localhost/api/get_student_progress.php \
  -H "Content-Type: application/json" \
  -d '{"StudentID": 1}'
```

**Success Response (200):**
```json
{
  "StudentID": 1,
  "FirstName": "Maria",
  "LastName": "Santos",
  "FullName": "Maria Santos",
  "CurrentAbility": 0.1234,
  "TotalXP": 150,
  "CurrentStreak": 5,
  "LongestStreak": 10,
  "TotalSessions": 3,
  "AverageAccuracy": 85.5,
  "TotalBadges": 2,
  "RecentActivities": [],
  "EarnedBadges": [],
  "SessionHistory": []
}
```

---

## 6. Get Personalized Lessons

**Endpoint:** `GET /get_lessons.php?StudentID=1`

**cURL Test:**
```bash
curl http://localhost/api/get_lessons.php?StudentID=1
```

**Success Response (200):**
```json
[
  {
    "LessonID": 1,
    "LessonTitle": "Introduction to Reading Comprehension",
    "LessonDescription": "Learn basic reading strategies",
    "RequiredAbility": -1.0,
    "GradeLevel": 4,
    "LessonType": "Reading",
    "CompletionStatus": "NotStarted",
    "Score": null,
    "AttemptsCount": 0,
    "LastAttemptDate": null,
    "CompletionDate": null
  }
]
```

---

## 7. Get Game Data

**Endpoint:** `GET /get_game_data.php?GameType=SentenceScramble&GradeLevel=4&Count=10`

**cURL Test:**
```bash
curl "http://localhost/api/get_game_data.php?GameType=SentenceScramble&GradeLevel=4&Count=10"
```

**Success Response (200):**
```json
{
  "GameType": "SentenceScramble",
  "GradeLevel": 4,
  "ItemCount": 5,
  "Items": [
    {
      "ItemID": 1,
      "ScrambledWords": ["homework", "diligently", "finished", "her", "Maria"],
      "CorrectSentence": "Maria diligently finished her homework.",
      "DifficultyLevel": "Easy",
      "Hint": "5 words"
    }
  ]
}
```

**Valid Game Types:**
- `SentenceScramble` - Syntax practice
- `TimedTrail` - Mixed skills (spelling, grammar, pronunciation)

---

## 8. Save Game Result

**Endpoint:** `POST /save_game_result.php`

**Request:**
```json
{
  "SessionID": 2,
  "StudentID": 1,
  "GameType": "SentenceScramble",
  "Score": 850,
  "AccuracyPercentage": 90.0,
  "TimeCompleted": 45,
  "XPEarned": 100,
  "StreakAchieved": 5
}
```

**cURL Test:**
```bash
curl -X POST http://localhost/api/save_game_result.php \
  -H "Content-Type: application/json" \
  -d '{
    "SessionID": 2,
    "StudentID": 1,
    "GameType": "SentenceScramble",
    "Score": 850,
    "AccuracyPercentage": 90.0,
    "TimeCompleted": 45,
    "XPEarned": 100,
    "StreakAchieved": 5
  }'
```

**Success Response (201):**
```json
{
  "success": true,
  "XPEarned": 100,
  "TotalXP": 250,
  "CurrentStreak": 5,
  "LongestStreak": 10,
  "NewBadges": [],
  "message": "Game result saved successfully"
}
```

---

## 9. Update Student Ability (Manual)

**Endpoint:** `POST /update_ability.php`

**Request:**
```json
{
  "StudentID": 1,
  "AbilityScore": 0.5
}
```

**cURL Test:**
```bash
curl -X POST http://localhost/api/update_ability.php \
  -H "Content-Type: application/json" \
  -d '{
    "StudentID": 1,
    "AbilityScore": 0.5
  }'
```

**Success Response (200):**
```json
{
  "success": true,
  "StudentID": 1,
  "CurrentAbility": 0.5
}
```

---

## 🧪 Complete Test Flow

### Step 1: Login
```bash
curl -X POST http://localhost/api/login.php \
  -H "Content-Type: application/json" \
  -d '{"email": "maria.santos@student.com", "password": "password123"}'
```
**→ Save StudentID from response**

### Step 2: Create Session
```bash
curl -X POST http://localhost/api/create_session.php \
  -H "Content-Type: application/json" \
  -d '{"StudentID": 1, "SessionType": "PreAssessment"}'
```
**→ Save SessionID from response**

### Step 3: Get Questions
```bash
curl -X POST http://localhost/api/get_preassessment_items.php
```
**→ Save ItemIDs**

### Step 4: Submit Responses
```bash
curl -X POST http://localhost/api/submit_responses.php \
  -H "Content-Type: application/json" \
  -d '{
    "StudentID": 1,
    "SessionID": 1,
    "Responses": [
      {"ItemID": 6, "SelectedOption": "B", "Correct": true, "TimeTakenSec": 10},
      {"ItemID": 9, "SelectedOption": "B", "Correct": true, "TimeTakenSec": 15}
    ]
  }'
```

### Step 5: Check Progress
```bash
curl http://localhost/api/get_student_progress.php?StudentID=1
```

---

## 🔧 Troubleshooting

### "Database connection failed"
1. Check SQL Server is running
2. Verify connection string in `src/db.php`
3. Test: `sqlcmd -S localhost -U sa -P YourPassword123`

### "Call to undefined function sqlsrv_connect()"
```bash
# Install SQL Server drivers for PHP
sudo pecl install sqlsrv pdo_sqlsrv
php -m | grep sqlsrv
```

### CORS Errors from Android
- Ensure `.htaccess` is loaded (Apache)
- Check `Access-Control-Allow-Origin` headers
- Verify `cleartext traffic` allowed in Android

### 401 Unauthorized
- Check email/password match database
- Verify `IsActive = 1` in Students table

---

## 📊 Response Codes

| Code | Meaning |
|------|---------|
| 200 | Success |
| 201 | Created (new resource) |
| 400 | Bad Request (invalid input) |
| 401 | Unauthorized (invalid credentials) |
| 404 | Not Found (resource doesn't exist) |
| 405 | Method Not Allowed (wrong HTTP method) |
| 500 | Server Error (check logs) |

---

## 🔐 Security Notes

**Development:**
- Cleartext HTTP is allowed
- Error details are shown
- CORS allows all origins

**Production:**
- ⚠️ Use HTTPS only
- ⚠️ Hash passwords with `password_hash()`
- ⚠️ Restrict CORS to specific domains
- ⚠️ Disable error display
- ⚠️ Enable rate limiting

---

## 📝 Sample Data

**Students:**
- maria.santos@student.com / password123
- juan.delacruz@student.com / password123
- ana.reyes@student.com / password123

**Teachers:**
- elena.torres@teacher.com / password123
- carlos.mendoza@teacher.com / password123

---

## 🚀 Quick Test Script

Save as `test_api.sh`:

```bash
#!/bin/bash
BASE_URL="http://localhost/api"

echo "1. Testing database connection..."
curl -s "$BASE_URL/test_db.php" | jq .

echo -e "\n2. Testing login..."
curl -s -X POST "$BASE_URL/login.php" \
  -H "Content-Type: application/json" \
  -d '{"email":"maria.santos@student.com","password":"password123"}' | jq .

echo -e "\n3. Creating session..."
curl -s -X POST "$BASE_URL/create_session.php" \
  -H "Content-Type: application/json" \
  -d '{"StudentID":1,"SessionType":"PreAssessment"}' | jq .

echo -e "\n4. Getting assessment items..."
curl -s -X POST "$BASE_URL/get_preassessment_items.php" | jq '. | length'

echo -e "\nAll tests complete!"
```

Run: `bash test_api.sh`

---

**Need help? Check the main [README.md](../../README.md) or [SETUP_GUIDE.md](../../SETUP_GUIDE.md)**
