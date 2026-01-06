# Adaptive Assessment System - Deployment Guide

## Overview
This guide will help you deploy the IRT-based adaptive assessment system for LiteRise. Follow these steps in order.

---

## Prerequisites
- ✅ XAMPP installed with Apache and SQL Server running
- ✅ SQL Server with LiteRise database
- ✅ Android Studio with LiteRise project

---

## Step 1: Deploy Database Schema

### 1.1 Run Assessment Items Schema

1. Open **SQL Server Management Studio (SSMS)**
2. Connect to your LiteRise database
3. Open the file: `api/db/assessment_items_schema.sql`
4. Execute the entire script (F5)

**What this does:**
- Creates `AssessmentItems` table with IRT parameters (difficulty, discrimination, guessing)
- Creates `StudentResponses` table for tracking all answers
- Creates `SP_GetNextAdaptiveQuestion` stored procedure for adaptive question selection
- Creates `SP_RecordStudentResponse` stored procedure for recording answers
- Creates indexes for performance

**Verify it worked:**
```sql
-- Check if tables exist
SELECT * FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_NAME IN ('AssessmentItems', 'StudentResponses');

-- Check if stored procedures exist
SELECT * FROM INFORMATION_SCHEMA.ROUTINES
WHERE ROUTINE_NAME IN ('SP_GetNextAdaptiveQuestion', 'SP_RecordStudentResponse');
```

### 1.2 Load Sample Questions

1. Still in SSMS, open the file: `api/db/sample_assessment_items.sql`
2. Execute the entire script (F5)

**What this does:**
- Inserts 36 sample questions across 4 categories:
  - 9 Oral Language questions (difficulty: -1.8 to 1.8)
  - 9 Word Knowledge questions (difficulty: -2.0 to 1.7)
  - 9 Reading Comprehension questions (difficulty: -1.9 to 1.9)
  - 9 Language Structure questions (difficulty: -1.8 to 2.0)

**Verify it worked:**
```sql
-- Check question count
SELECT Category, COUNT(*) as QuestionCount
FROM dbo.AssessmentItems
GROUP BY Category;

-- Should show:
-- Oral Language: 9
-- Word Knowledge: 9
-- Reading Comprehension: 9
-- Language Structure: 9
```

---

## Step 2: Deploy API Files

### 2.1 Copy API Endpoints

Copy the following files from the `api/` folder to your XAMPP `htdocs/api/` directory:

```bash
# From your LiteRise project folder:
cp api/get_next_question.php C:/xampp/htdocs/api/
cp api/submit_answer.php C:/xampp/htdocs/api/
```

**Files to copy:**
- ✅ `get_next_question.php` - Fetches next adaptive question
- ✅ `submit_answer.php` - Records student answers (UPDATED with server-side correctness)

### 2.2 Verify API Files

**Test get_next_question.php:**

Open in browser or Postman:
```
POST http://localhost/api/get_next_question.php
Headers:
  Content-Type: application/json
  Authorization: Bearer <your_test_token>

Body:
{
  "student_id": 27,
  "session_id": 1736149200,
  "current_theta": 0.0,
  "assessment_type": "PreAssessment",
  "category": "Oral Language"
}
```

**Expected response:**
```json
{
  "success": true,
  "question": {
    "item_id": 1,
    "category": "Oral Language",
    "subcategory": "Basic Vocabulary",
    "skill_area": "Common Objects",
    "question_text": "What do you use to write on paper?",
    "question_type": "MultipleChoice",
    "option_a": "Pencil",
    "option_b": "Spoon",
    "option_c": "Shoe",
    "option_d": "Ball",
    "difficulty": -1.8,
    "discrimination": 1.2,
    "estimated_time": 20
  },
  "progress": {
    "questions_answered": 0,
    "accuracy": 0.0,
    "current_theta": 0.0
  }
}
```

**Test submit_answer.php:**

```
POST http://localhost/api/submit_answer.php
Headers:
  Content-Type: application/json
  Authorization: Bearer <your_test_token>

Body:
{
  "student_id": 27,
  "item_id": 1,
  "session_id": 1736149200,
  "assessment_type": "PreAssessment",
  "selected_answer": "Pencil",
  "student_theta": 0.0,
  "response_time": 12,
  "question_number": 1,
  "device_info": "realme RMX3286, Android 13"
}
```

**Expected response:**
```json
{
  "success": true,
  "response_id": 1234,
  "is_correct": true,
  "feedback": {
    "message": "Correct! Great job! 🎉",
    "expected_probability": 0.965,
    "new_theta_estimate": 0.1
  }
}
```

---

## Step 3: Build and Test Android App

### 3.1 Rebuild the Android App

1. Open Android Studio
2. Select **Build > Clean Project**
3. Select **Build > Rebuild Project**
4. Wait for Gradle sync to complete

### 3.2 Update API Base URL (if needed)

If your server IP changed, update the base URL in:
`app/src/main/java/com/example/literise/api/ApiClient.java`

```java
private static final String BASE_URL = "http://YOUR_SERVER_IP/api/";
```

### 3.3 Run the App

1. Connect your Android device or start emulator
2. Click **Run** (Shift+F10)
3. Login with a test account
4. Start the placement test

---

## Step 4: Test the Adaptive Flow

### 4.1 Start a Fresh Assessment

1. **Login** with a test student account (or create new student)
2. Go through **Welcome Onboarding**
3. **Start Placement Test**

### 4.2 Verify Adaptive Behavior

**What to check:**

✅ **Question Loading:**
- Questions should load from API (not hardcoded)
- You should see questions from database
- Check Android Logcat for: `"Requesting next question - Theta: X.X, Category: XXX"`

✅ **Category Filtering:**
- Category 1 questions should be "Oral Language"
- Category 2 should be "Word Knowledge"
- Category 3 should be "Reading Comprehension"
- Category 4 should be "Language Structure"

✅ **Difficulty Adaptation:**
- Answer questions correctly → Next questions should get harder
- Answer questions incorrectly → Next questions should get easier
- Check Leo hints - they should reference difficulty

✅ **Answer Submission:**
- Check Logcat for: `"Answer submitted - New Theta: X.X"`
- Check SQL Server for new rows in `StudentResponses` table:
```sql
SELECT TOP 10 * FROM dbo.StudentResponses
ORDER BY RespondedAt DESC;
```

✅ **Progress Tracking:**
- Progress bar should update
- Questions should count up to 25
- Assessment should complete normally

### 4.3 Database Verification

After completing the test, verify data was saved:

```sql
-- Check how many responses were recorded
SELECT
    COUNT(*) as TotalResponses,
    SUM(CASE WHEN IsCorrect = 1 THEN 1 ELSE 0 END) as CorrectAnswers,
    AVG(ResponseTime) as AvgResponseTime
FROM dbo.StudentResponses
WHERE StudentID = <your_test_student_id>;

-- Check theta progression
SELECT
    QuestionNumber,
    ItemID,
    IsCorrect,
    StudentThetaAtTime,
    ResponseTime
FROM dbo.StudentResponses
WHERE StudentID = <your_test_student_id>
ORDER BY QuestionNumber;
```

---

## Step 5: Troubleshooting

### Issue: "Error loading question: Failed to fetch question: 401"

**Solution:**
- Check that you're logged in
- Verify JWT token is valid
- Check API authentication headers

### Issue: "Error loading question: Network error"

**Solution:**
- Verify XAMPP Apache is running
- Check server IP address in ApiClient.java
- Test API directly in browser
- Check firewall settings

### Issue: Questions don't match categories

**Solution:**
- Verify sample_assessment_items.sql was executed
- Check that category names match exactly ("Oral Language", not "oral language")
- Run this query to check:
```sql
SELECT Category, COUNT(*) FROM dbo.AssessmentItems GROUP BY Category;
```

### Issue: "Error submitting answer: Failed to submit answer: 500"

**Solution:**
- Check XAMPP Apache error logs: `C:\xampp\apache\logs\error.log`
- Verify stored procedure exists:
```sql
SELECT * FROM INFORMATION_SCHEMA.ROUTINES WHERE ROUTINE_NAME = 'SP_RecordStudentResponse';
```
- Check SQL Server is running

### Issue: App crashes on question load

**Solution:**
- Check Android Logcat for stack trace
- Verify all model classes have proper getters
- Make sure PlacementQuestion has all required fields

---

## Step 6: Next Steps

Once the adaptive assessment is working:

### Option A: Add More Questions

Add more questions to `AssessmentItems` table:
```sql
INSERT INTO dbo.AssessmentItems (
    Category, Subcategory, SkillArea,
    QuestionText, QuestionType,
    OptionA, OptionB, OptionC, OptionD, CorrectAnswer,
    DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel
) VALUES (
    'Oral Language', 'Advanced Vocabulary', 'Synonyms',
    'Which word means the same as "happy"?', 'MultipleChoice',
    'Joyful', 'Sad', 'Angry', 'Tired', 'A',
    0.5, 1.4, 0.25, 2
);
```

### Option B: Implement Pronunciation System

Follow `ADAPTIVE_IMPLEMENTATION_PLAN.md` Phase 3:
- Add pronunciation fields to database
- Integrate Google Cloud Speech API
- Add pronunciation questions
- Build PronunciationHelper.java

### Option C: Add ML Model for Better Theta Estimation

- Export StudentResponses data
- Train ML model for ability estimation
- Replace simple theta calculation with ML predictions

---

## Success Checklist

Before moving to the next phase, verify:

- ✅ AssessmentItems table exists with 36+ questions
- ✅ StudentResponses table exists
- ✅ Stored procedures created successfully
- ✅ API endpoints return valid JSON responses
- ✅ Android app loads questions from API
- ✅ Questions adapt based on student performance
- ✅ All answers saved to StudentResponses table
- ✅ Assessment completes successfully
- ✅ Results screen shows accurate theta and placement level

---

## Support

If you encounter issues:

1. Check `ADAPTIVE_IMPLEMENTATION_PLAN.md` for detailed architecture
2. Review Android Logcat for error messages
3. Check XAMPP Apache error logs
4. Verify SQL Server connection and stored procedures

---

**Last Updated:** 2026-01-06
**Status:** Ready for Deployment
