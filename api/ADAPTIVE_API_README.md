# LiteRise Adaptive Learning API Endpoints

**Created:** 2026-01-18
**Version:** 2.0 (Adaptive System)
**Purpose:** Backend API endpoints for 13-node adaptive module system

---

## 🚀 New Endpoints for Adaptive System

### 1. **GET Module Structure**
**File:** `get_module_structure.php`
**Method:** POST
**Authentication:** Required (JWT)

**Description:**
Returns the complete 13-node structure for a module with student progress:
- 12 lessons (3 per quarter)
- 1 module assessment (node 13)
- Student's proficiency level
- Branching status (intervention/enrichment unlocked/completed)

**Request:**
```json
{
  "student_id": 1,
  "module_id": 1
}
```

**Response:**
```json
{
  "success": true,
  "module": {
    "ModuleID": 1,
    "ModuleName": "Phonics and Word Study",
    "TotalLessons": 12,
    "CompletedLessons": 3,
    "ProgressPercentage": 25,
    "CurrentLessonID": 104,
    "IsActive": true
  },
  "lessons": [
    {
      "LessonID": 101,
      "LessonNumber": 1,
      "Quarter": 1,
      "Title": "Sight Words: The Basics",
      "Description": "Learn to recognize common sight words",
      "GameType": "word_hunt",
      "RequiredAbility": -1.0,
      "InterventionThreshold": 60,
      "EnrichmentThreshold": 85,
      "XPReward": 20,
      "IsUnlocked": true,
      "IsCompleted": true,
      "QuizScore": 85,
      "CompletedAt": "2026-01-15 10:30:00",
      "HasIntervention": true,
      "HasEnrichment": true,
      "InterventionStatus": "not_unlocked",
      "EnrichmentStatus": "completed"
    }
    // ... 11 more lessons
  ],
  "assessment": {
    "LessonID": 113,
    "Title": "Module 1 Assessment",
    "Description": "Test all skills from Module 1",
    "IsUnlocked": false,
    "IsCompleted": false,
    "RequiredLessons": 12,
    "Score": null
  },
  "proficiency": {
    "CurrentAbility": 0.5,
    "ProficiencyLevel": "Intermediate",
    "Theta": 0.5
  }
}
```

**Uses Stored Procedure:** `SP_GetModuleStructure`

---

### 2. **Update Quiz Score**
**File:** `update_quiz_score.php`
**Method:** POST
**Authentication:** Required (JWT)

**Description:**
Updates quiz score and determines branching decision:
- **Score < 60%:** Unlock intervention branch (required)
- **Score >= 85%:** Unlock enrichment branch (optional)
- **Score 60-84%:** Proceed normally to game

**Request:**
```json
{
  "student_id": 1,
  "lesson_id": 101,
  "quiz_score": 75,
  "time_spent": 180,
  "total_questions": 10,
  "correct_answers": 7
}
```

**Response (Standard):**
```json
{
  "success": true,
  "decision": "proceed_standard",
  "message": "Great job! You scored 75%. Now let's play the game!",
  "quiz_score": 75,
  "next_step": "game",
  "unlocked_branches": [],
  "xp_awarded": 15,
  "lesson_locked": false,
  "thresholds": {
    "intervention": 60,
    "enrichment": 85
  }
}
```

**Response (Intervention Required):**
```json
{
  "success": true,
  "decision": "intervention_required",
  "message": "You scored 45%. Let's review this lesson together!",
  "quiz_score": 45,
  "next_step": "intervention",
  "unlocked_branches": [
    {
      "BranchID": 1,
      "BranchType": "intervention",
      "Title": "Sight Words Review",
      "Description": "Practice the words we just learned"
    }
  ],
  "xp_awarded": 5,
  "lesson_locked": true,
  "thresholds": {
    "intervention": 60,
    "enrichment": 85
  }
}
```

**Response (Enrichment Unlocked):**
```json
{
  "success": true,
  "decision": "enrichment_unlocked",
  "message": "Excellent! You scored 90%. You've unlocked an enrichment challenge!",
  "quiz_score": 90,
  "next_step": "game",
  "unlocked_branches": [
    {
      "BranchID": 2,
      "BranchType": "enrichment",
      "Title": "Advanced Sight Words",
      "Description": "Challenge yourself with Key Stage 2 words"
    }
  ],
  "xp_awarded": 25,
  "lesson_locked": false,
  "thresholds": {
    "intervention": 60,
    "enrichment": 85
  }
}
```

**Uses Stored Procedure:** `SP_UpdateQuizScore`

**XP Awards:**
- Score < 60%: +5 XP
- Score 60-84%: +15 XP
- Score >= 85%: +25 XP

---

### 3. **Get Lesson Branches**
**File:** `get_lesson_branches.php`
**Method:** POST
**Authentication:** Required (JWT)

**Description:**
Returns intervention and enrichment branches for a lesson with student progress

**Request:**
```json
{
  "student_id": 1,
  "lesson_id": 101
}
```

**Response:**
```json
{
  "success": true,
  "lesson": {
    "LessonID": 101,
    "Title": "Sight Words: The Basics",
    "QuizScore": 55,
    "CompletionStatus": "InProgress"
  },
  "intervention": {
    "BranchID": 1,
    "BranchType": "intervention",
    "Title": "Sight Words Review",
    "Description": "Practice the words we just learned",
    "ContentData": {
      "activities": [
        {
          "type": "flash_cards",
          "words": ["the", "a", "to", "is"]
        },
        {
          "type": "matching_game",
          "pairs": 5
        }
      ]
    },
    "RequiredAbility": -1.5,
    "Status": "unlocked",
    "Score": null,
    "UnlockedAt": "2026-01-18 10:30:00",
    "CompletedAt": null
  },
  "enrichment": {
    "BranchID": 2,
    "BranchType": "enrichment",
    "Title": "Advanced Sight Words",
    "Description": "Challenge yourself with Key Stage 2 words",
    "ContentData": {
      "activities": [
        {
          "type": "complex_sentences",
          "difficulty": "ks2"
        }
      ]
    },
    "RequiredAbility": 1.5,
    "Status": "locked",
    "Score": null,
    "UnlockedAt": null,
    "CompletedAt": null
  },
  "has_intervention": true,
  "has_enrichment": true
}
```

**Uses Stored Procedure:** `SP_GetLessonBranches`

**Branch Status Values:**
- `"locked"` - Not yet available
- `"unlocked"` - Available to start
- `"completed"` - Finished

---

### 4. **Complete Branch**
**File:** `complete_branch.php`
**Method:** POST
**Authentication:** Required (JWT)

**Description:**
Marks an intervention or enrichment branch as completed
- **Intervention:** Unlocks quiz retry
- **Enrichment:** Awards bonus XP + possible badge

**Request:**
```json
{
  "student_id": 1,
  "branch_id": 1,
  "score": 85,
  "time_spent": 300,
  "activities_completed": 5
}
```

**Response (Intervention):**
```json
{
  "success": true,
  "message": "Intervention completed! You can now retry the quiz.",
  "branch": {
    "BranchID": 1,
    "BranchType": "intervention",
    "Title": "Sight Words Review",
    "Score": 85,
    "CompletedAt": "2026-01-18 11:00:00"
  },
  "xp_awarded": 10,
  "total_xp": 1350,
  "next_action": "retry_quiz",
  "parent_lesson_id": 101,
  "quiz_unlocked": true
}
```

**Response (Enrichment with Badge):**
```json
{
  "success": true,
  "message": "Enrichment completed! Great work!",
  "branch": {
    "BranchID": 2,
    "BranchType": "enrichment",
    "Title": "Advanced Sight Words",
    "Score": 95,
    "CompletedAt": "2026-01-18 11:00:00"
  },
  "xp_awarded": 30,
  "total_xp": 1380,
  "next_action": "continue_lesson",
  "parent_lesson_id": 101,
  "badge_earned": {
    "BadgeID": 5,
    "BadgeName": "Enrichment Explorer",
    "BadgeDescription": "Completed your first enrichment activity"
  }
}
```

**Uses Stored Procedure:** `SP_CompleteBranch`

**XP Awards (Intervention):**
- Always +10 XP

**XP Awards (Enrichment):**
- Score >= 90%: +30 XP
- Score >= 75%: +20 XP
- Score < 75%: +15 XP

**Badge Unlock:**
- First enrichment completion: "Enrichment Explorer" badge

---

## 🗄️ Required Database Changes

Before using these endpoints, run the database migration:

```bash
sqlcmd -S DESKTOP-PEM6F9E\SQLEXPRESS -d LiteRiseDB -i adaptive_schema_update.sql
```

This creates:
- **3 new tables:** Modules, LessonBranches, StudentBranches
- **6 new columns** in Lessons table
- **4 new stored procedures:** SP_GetModuleStructure, SP_UpdateQuizScore, SP_GetLessonBranches, SP_CompleteBranch

---

## 🔐 Authentication

All endpoints require JWT authentication via the `Authorization` header:

```
Authorization: Bearer <jwt_token>
```

The `requireAuth()` function validates:
1. Token is present
2. Token is valid (not expired)
3. Student ID matches authenticated user

---

## 📊 Adaptive Flow Diagram

```
Student starts lesson
    ↓
Interactive Content Activity
    ↓
Quiz (10 questions)
    ↓
POST to update_quiz_score.php
    ↓
    ├─ Score < 60%:
    │   ├─ Response: decision="intervention_required"
    │   ├─ Unlock intervention branch
    │   ├─ POST to get_lesson_branches.php
    │   ├─ Show InterventionActivity
    │   ├─ POST to complete_branch.php
    │   └─ Retry quiz
    │
    ├─ Score >= 85%:
    │   ├─ Response: decision="enrichment_unlocked"
    │   ├─ Unlock enrichment branch (optional)
    │   ├─ Student can skip or complete enrichment
    │   └─ Proceed to game
    │
    └─ Score 60-84%:
        ├─ Response: decision="proceed_standard"
        └─ Proceed to game
```

---

## 🧪 Testing the Endpoints

### Test with cURL

**1. Get Module Structure:**
```bash
curl -X POST http://localhost/LiteRiseAPI/get_module_structure.php \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"student_id": 1, "module_id": 1}'
```

**2. Update Quiz Score (Intervention):**
```bash
curl -X POST http://localhost/LiteRiseAPI/update_quiz_score.php \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"student_id": 1, "lesson_id": 101, "quiz_score": 45}'
```

**3. Get Lesson Branches:**
```bash
curl -X POST http://localhost/LiteRiseAPI/get_lesson_branches.php \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"student_id": 1, "lesson_id": 101}'
```

**4. Complete Branch:**
```bash
curl -X POST http://localhost/LiteRiseAPI/complete_branch.php \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"student_id": 1, "branch_id": 1, "score": 85}'
```

---

## 🐛 Error Responses

All endpoints return standardized error responses:

```json
{
  "success": false,
  "error": "Error message here",
  "details": "Detailed technical information (dev only)"
}
```

**Common HTTP Status Codes:**
- `200` - Success
- `400` - Bad request (missing/invalid parameters)
- `401` - Unauthorized (no token or invalid token)
- `403` - Forbidden (trying to access another student's data)
- `404` - Not found (student/module/lesson doesn't exist)
- `500` - Server error (database error, exception)

---

## 📝 Implementation Checklist

### Day 1: Backend (Complete)
- [x] Create `get_module_structure.php`
- [x] Create `update_quiz_score.php`
- [x] Create `get_lesson_branches.php`
- [x] Create `complete_branch.php`
- [ ] Run `adaptive_schema_update.sql` on database
- [ ] Test all endpoints with Postman/cURL

### Day 2: Android Integration
- [ ] Add 4 new methods to `ApiService.java`
- [ ] Create request/response models
- [ ] Update `ModuleLadderActivity` to call `get_module_structure.php`
- [ ] Update `ModuleLessonActivity` quiz submission to call `update_quiz_score.php`

### Day 3: Testing
- [ ] Test complete flow: Content → Quiz → Branching → Game
- [ ] Test intervention path (quiz < 60%)
- [ ] Test enrichment path (quiz >= 85%)
- [ ] Test standard path (quiz 60-84%)

---

## 🔗 Related Files

- **Database Schema:** `adaptive_schema_update.sql`
- **Analysis Documents:**
  - `DATABASE_STRUCTURE_ANALYSIS.md`
  - `ANDROID_APP_STRUCTURE_ANALYSIS.md`
- **Implementation Plan:** `2_3_DAY_IMPLEMENTATION_PLAN.md`

---

## 📞 Support

For issues or questions:
1. Check database migration completed successfully
2. Verify stored procedures exist: `SELECT * FROM INFORMATION_SCHEMA.ROUTINES WHERE ROUTINE_NAME LIKE 'SP_%Module%'`
3. Check API logs: `error_log()` statements in each endpoint
4. Verify authentication token is valid

---

**Last Updated:** 2026-01-18
**Status:** Ready for testing after database migration
