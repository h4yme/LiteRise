# 🚀 2-3 Day Implementation Plan - Adaptive Learning System

**Target:** Complete adaptive branching system integration
**Timeline:** 2-3 days maximum
**Current:** You have MSSQL + PHP API + IRT system
**Goal:** 13-node modules with adaptive branching

---

## 📅 Day 1: Database & API (Backend)

### Morning (4 hours) - Database Schema

**Task 1.1: Run SQL Schema Updates** ⏱️ 30 min
```bash
# Execute adaptive_schema_update.sql on your MSSQL database
sqlcmd -S DESKTOP-PEM6F9E\SQLEXPRESS -d LiteRiseDB -i adaptive_schema_update.sql
```

**What it does:**
- ✅ Adds 13-node module structure (12 lessons + 1 assessment)
- ✅ Creates `Modules`, `LessonBranches`, `StudentBranches` tables
- ✅ Updates `Lessons` with Quarter, GameType, Intervention/Enrichment thresholds
- ✅ Creates stored procedures for adaptive logic
- ✅ Seeds Module 1: Phonics and Word Study

**Task 1.2: Verify Schema** ⏱️ 15 min
```sql
-- Run these verification queries
SELECT * FROM Modules WHERE ModuleID = 1;
SELECT * FROM Lessons WHERE ModuleID = 1 ORDER BY LessonNumber;
SELECT * FROM LessonBranches;
EXEC SP_GetModuleStructure @StudentID = 1, @ModuleID = 1;
```

### Afternoon (4 hours) - PHP API Endpoints

**Task 1.3: Create New API Endpoints** ⏱️ 2 hours

Create these files in `/LiteRiseAPI/`:

1. **`get_module_structure.php`** - Returns 13 nodes with branching info
2. **`update_quiz_score.php`** - Updates score & checks branching
3. **`get_lesson_branches.php`** - Returns intervention/enrichment content
4. **`complete_branch.php`** - Marks branch as completed

**Task 1.4: Test API Endpoints** ⏱️ 1 hour

```bash
# Test with Postman or curl
curl -X POST https://your-api.com/get_module_structure.php \
  -H "Content-Type: application/json" \
  -d '{"student_id": 1, "module_id": 1}'
```

**Task 1.5: Deploy API Updates** ⏱️ 30 min
- Push to GitHub
- Deploy to hosting

---

## 📅 Day 2: Android Integration (Frontend)

### Morning (4 hours) - Data Models & API Client

**Task 2.1: Update Android Data Models** ⏱️ 1 hour

Update these models:
- `Lesson.java` - Add Quarter, LessonNumber fields
- Create `Module.java` - New model for modules
- Create `LessonBranch.java` - For intervention/enrichment
- Create `ModuleAssessment.java` - For node 13

**Task 2.2: Create API Service** ⏱️ 1.5 hours

```java
// ApiService.java
@POST("get_module_structure.php")
Call<ModuleStructureResponse> getModuleStructure(
    @Body ModuleRequest request
);

@POST("update_quiz_score.php")
Call<BranchingDecision> updateQuizScore(
    @Body QuizScoreRequest request
);

@POST("get_lesson_branches.php")
Call<BranchesResponse> getLessonBranches(
    @Body BranchRequest request
);
```

**Task 2.3: Update SQLite Cache** ⏱️ 1 hour

Keep SQLite for offline caching:
```java
// Update LessonDatabase.java
public void cacheModuleStructure(Module module, List<Lesson> lessons);
public void cacheBranches(int lessonId, List<LessonBranch> branches);
```

### Afternoon (4 hours) - UI Updates

**Task 2.4: Update ModuleLadderActivity** ⏱️ 2 hours

Show 13 nodes instead of 15:
```java
private void loadModuleStructure() {
    // Call API to get 13 nodes (12 lessons + 1 assessment)
    apiService.getModuleStructure(moduleId, studentId)
        .enqueue(new Callback<ModuleStructureResponse>() {
            @Override
            public void onResponse(...) {
                displayNodes(response.lessons); // 12 lessons
                displayAssessmentNode(response.assessment); // Node 13
            }
        });
}
```

**Task 2.5: Implement Branching Nodes** ⏱️ 2 hours

Add intervention/enrichment branches:
```java
private void showBranchingNodes(Lesson lesson) {
    if (lesson.needsIntervention()) {
        // Show intervention nodes to the left/right
        addInterventionBranch(lesson);
    }
    if (lesson.hasEnrichment()) {
        // Show enrichment nodes as optional
        addEnrichmentBranch(lesson);
    }
}
```

---

## 📅 Day 3: Lesson Flow & Testing

### Morning (4 hours) - Lesson Flow (Content → Quiz → Game)

**Task 3.1: Redesign Lesson Activity** ⏱️ 2 hours

Change from 3 tabs to sequential flow:

```java
// ModuleLessonActivity.java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Step 1: Show Interactive Content
    showInteractiveContent();
}

private void showInteractiveContent() {
    // Interactive content view
    // When completed → moveToQuiz()
}

private void moveToQuiz() {
    // Knowledge check quiz
    // When completed → checkBranching(score)
}

private void checkBranching(int quizScore) {
    apiService.updateQuizScore(lessonId, quizScore)
        .enqueue(new Callback<BranchingDecision>() {
            @Override
            public void onResponse(...) {
                if (decision.needsIntervention()) {
                    showInterventionRequired();
                } else if (decision.hasEnrichment()) {
                    showEnrichmentOption();
                } else {
                    moveToGame();
                }
            }
        });
}

private void moveToGame() {
    // Launch game based on gameType
    // After game → markLessonComplete()
}
```

**Task 3.2: Create Intervention Activity** ⏱️ 1.5 hours

Simple activity for intervention content:
```java
// InterventionActivity.java
- Shows extra practice content
- Simpler exercises
- After completion → retry quiz
```

**Task 3.3: Create Enrichment Activity** ⏱️ 30 min

Optional challenge content:
```java
// EnrichmentActivity.java
- Shows advanced content
- Optional (can skip)
- Extra XP rewards
```

### Afternoon (4 hours) - Testing & Bug Fixes

**Task 3.4: Test Full Flow** ⏱️ 2 hours

Test scenarios:
1. ✅ **Beginner (θ < -1):**
   - Low quiz score → Intervention unlocks → Must complete → Retry quiz → Game
2. ✅ **Intermediate (θ ≈ 0):**
   - Medium quiz score → Proceed to game
3. ✅ **Advanced (θ > 1):**
   - High quiz score → Enrichment option → Game

**Task 3.5: Fix Bugs** ⏱️ 1.5 hours

Common issues:
- Game routing not working → Check gameType constants match
- Intervention not showing → Check SQL threshold logic
- Quiz score not saving → Check API response

**Task 3.6: Final Testing** ⏱️ 30 min

```bash
# Test complete user journey
1. Student opens Phonics module
2. Sees 13 nodes (12 lessons + assessment)
3. Clicks Lesson 1 → Interactive content → Quiz
4. Scores 55% → Intervention branch appears
5. Completes intervention → Retries quiz → Scores 70%
6. Proceeds to Word Hunt game → Earns XP
7. Lesson 2 unlocks
```

---

## 📋 Detailed File Changes

### SQL Files (Day 1)
```
✅ adaptive_schema_update.sql (CREATED - run on your database)
```

### PHP API Files (Day 1)
```
✅ get_module_structure.php (NEW)
✅ update_quiz_score.php (NEW)
✅ get_lesson_branches.php (NEW)
✅ complete_branch.php (NEW)
✅ get_lessons.php (UPDATE - use new SP_GetModuleStructure)
```

### Android Models (Day 2)
```
✅ models/Module.java (NEW)
✅ models/LessonBranch.java (NEW)
✅ models/ModuleAssessment.java (NEW)
✅ models/BranchingDecision.java (NEW)
✅ models/Lesson.java (UPDATE - add Quarter, LessonNumber, etc.)
```

### Android Activities (Day 2-3)
```
✅ ModuleLadderActivity.java (UPDATE - 13 nodes + branching)
✅ ModuleLessonActivity.java (REDESIGN - Content→Quiz→Game flow)
✅ InterventionActivity.java (NEW)
✅ EnrichmentActivity.java (NEW)
✅ ModuleAssessmentActivity.java (NEW - Node 13)
```

### Android API (Day 2)
```
✅ network/ApiService.java (UPDATE - new endpoints)
✅ network/ModuleApiCalls.java (NEW - module-specific calls)
✅ database/LessonDatabase.java (UPDATE - cache new structure)
```

---

## 🎯 Success Criteria

### By End of Day 1:
- ✅ Database has 13-node structure
- ✅ Stored procedures working
- ✅ API endpoints returning correct data
- ✅ Module 1 fully seeded with game types

### By End of Day 2:
- ✅ Android app fetches 13 nodes
- ✅ Ladder shows 12 lessons + 1 assessment
- ✅ Branching nodes appear based on quiz scores
- ✅ Game badges visible on nodes

### By End of Day 3:
- ✅ Lesson flow: Content → Quiz → Game works
- ✅ Intervention/Enrichment branches functional
- ✅ Students can complete full lesson cycle
- ✅ Progress saves to database
- ✅ XP and badges awarded correctly

---

## 🚨 Priority Tasks (If Time is Limited)

### Must Have (2 days minimum):
1. ✅ Database schema update
2. ✅ API endpoints for module structure
3. ✅ Android UI showing 13 nodes
4. ✅ Basic quiz score → branching logic
5. ✅ Game routing working

### Nice to Have (3rd day):
1. Intervention activity (can use simple dialog for MVP)
2. Enrichment content (can skip in v1)
3. Module assessment (can add later)
4. Animations and polish

---

## 📞 Quick Reference

### Test Credentials
```
Student ID: 1 (or your test student)
Module ID: 1 (Phonics and Word Study)
```

### API Base URL
```
https://your-api.com/
```

### Database Connection
```
Server: DESKTOP-PEM6F9E\SQLEXPRESS
Database: LiteRiseDB
```

### Key Constants
```java
// Android
INTERVENTION_THRESHOLD = 60
ENRICHMENT_THRESHOLD = 85
TOTAL_LESSONS = 12
TOTAL_NODES = 13
```

---

## 🔧 Troubleshooting

### Issue: Games not launching
**Fix:** Ensure `GameType` column populated:
```sql
UPDATE Lessons SET GameType = 'word_hunt' WHERE LessonID = 101;
```

### Issue: Intervention not showing
**Fix:** Check quiz score threshold:
```sql
SELECT QuizScore, InterventionThreshold
FROM StudentProgress sp
JOIN Lessons l ON sp.LessonID = l.LessonID
WHERE StudentID = 1;
```

### Issue: API returns empty
**Fix:** Check stored procedure:
```sql
EXEC SP_GetModuleStructure @StudentID = 1, @ModuleID = 1;
```

---

## ✅ Next Steps After 3 Days

Once core system works:
1. Create content for remaining modules (2-5)
2. Build interactive content components
3. Add animations and polish
4. Teacher dashboard for viewing branches
5. Analytics on intervention effectiveness

---

**Ready to start? Begin with Day 1, Task 1.1! 🚀**
