# Database Structure Analysis - Current vs. Adaptive System

**Analysis Date:** 2026-01-18
**Purpose:** Compare existing LiteRiseDB structure with adaptive learning requirements
**Timeline:** 2-3 day implementation plan

---

## Executive Summary

### Current State ✅
- **Database:** MSSQL Server (DESKTOP-PEM6F9E\SQLEXPRESS)
- **Database Name:** LiteRiseDB
- **IRT System:** Fully implemented with 3PL model
- **API Backend:** PHP with PDO SQL Server driver
- **Tables:** 15 tables covering students, lessons, games, IRT assessment
- **Stored Procedures:** 17 existing SPs for lessons, progress, IRT

### What's Missing ❌
- **Module System:** No Modules table, no ModuleID in Lessons
- **Quarterly Structure:** No Quarter, LessonNumber fields
- **Game Type Assignment:** GameType field not in Lessons table (exists in GameResults only)
- **Branching System:** No LessonBranches, StudentBranches tables
- **Thresholds:** No InterventionThreshold, EnrichmentThreshold fields
- **Module Assessment:** No assessment node tracking

### Action Required
Run `adaptive_schema_update.sql` to extend existing schema with adaptive learning features.

---

## 📊 Current Database Structure (from script28.sql)

### Core Tables

#### 1. **Students** ✅ (Excellent foundation for adaptive system)
```sql
CREATE TABLE [dbo].[Students] (
    [StudentID] [int] IDENTITY(1,1) PRIMARY KEY,
    [FirstName] [nvarchar](50) NOT NULL,
    [LastName] [nvarchar](50) NOT NULL,
    [Email] [nvarchar](100) NOT NULL UNIQUE,
    [Password] [nvarchar](255) NOT NULL,
    [GradeLevel] [int] NOT NULL,  -- ✅ CHECK: Grade 4-6
    [Section] [nvarchar](20) NULL,

    -- ✅ IRT Ability Tracking (CRITICAL for adaptive system)
    [InitialAbility] [float] DEFAULT 0.0,
    [CurrentAbility] [float] DEFAULT 0.0,  -- ✅ Used for proficiency classification

    -- ✅ Multi-domain Theta scores
    [ReadingTheta] [float] DEFAULT 0.0,
    [SpeakingTheta] [float] DEFAULT 0.0,
    [VocabularyTheta] [float] DEFAULT 0.0,
    [SyntaxTheta] [float] DEFAULT 0.0,

    -- ✅ Gamification
    [TotalXP] [int] DEFAULT 0,
    [CurrentStreak] [int] DEFAULT 0,
    [LongestStreak] [int] DEFAULT 0,

    -- Account
    [DateCreated] [datetime] DEFAULT GETDATE(),
    [LastLogin] [datetime] NULL,
    [IsActive] [bit] DEFAULT 1,
    [LastActivityDate] [date] NULL
)
```

**Status:** ✅ **Perfect for adaptive system**
- Already has `CurrentAbility` for IRT proficiency classification
- Multi-domain theta tracking (Reading, Speaking, Vocabulary, Syntax)
- Gamification fields (XP, Streaks)

---

#### 2. **Lessons** ⚠️ (Needs extension for adaptive modules)
```sql
CREATE TABLE [dbo].[Lessons] (
    [LessonID] [int] IDENTITY(1,1) PRIMARY KEY,
    [LessonTitle] [nvarchar](200) NOT NULL,
    [LessonDescription] [nvarchar](max) NULL,
    [LessonContent] [nvarchar](max) NULL,
    [RequiredAbility] [float] NULL,  -- ✅ Already uses IRT ability matching
    [GradeLevel] [int] NOT NULL,
    [LessonType] [nvarchar](50) NULL,  -- e.g., 'Reading', 'Vocabulary'
    [DateCreated] [datetime] DEFAULT GETDATE(),
    [IsActive] [bit] DEFAULT 1
)
```

**Missing Fields for Adaptive System:**
```sql
-- NEED TO ADD:
[ModuleID] [int] NULL                    -- Link to Modules table
[Quarter] [int] NULL                     -- 1-4 (for 3 lessons per quarter)
[LessonNumber] [int] NULL                -- 1-12 within module
[GameType] [varchar](50) NULL            -- word_hunt, sentence_scramble, etc.
[InterventionThreshold] [int] DEFAULT 60 -- Quiz score < 60% triggers intervention
[EnrichmentThreshold] [int] DEFAULT 85   -- Quiz score >= 85% unlocks enrichment
```

**Status:** ⚠️ **Table exists but needs 6 new columns**

---

#### 3. **StudentProgress** ✅ (Good for tracking lesson completion)
```sql
CREATE TABLE [dbo].[StudentProgress] (
    [ProgressID] [int] IDENTITY(1,1) PRIMARY KEY,
    [StudentID] [int] NULL,
    [LessonID] [int] NULL,
    [CompletionStatus] [nvarchar](20) NULL,  -- 'NotStarted', 'InProgress', 'Completed'
    [Score] [float] NULL,                    -- ✅ Can store quiz score
    [AttemptsCount] [int] DEFAULT 0,
    [LastAttemptDate] [datetime] NULL,
    [CompletionDate] [datetime] NULL,

    FOREIGN KEY ([LessonID]) REFERENCES [Lessons]([LessonID]),
    FOREIGN KEY ([StudentID]) REFERENCES [Students]([StudentID])
)
```

**Status:** ✅ **Perfect for tracking lesson progress**
- Already has `Score` field for quiz scores
- `CompletionStatus` tracks progress
- Can be used to check branching conditions

---

#### 4. **GameResults** ✅ (Already tracks game types!)
```sql
CREATE TABLE [dbo].[GameResults] (
    [GameResultID] [int] IDENTITY(1,1) PRIMARY KEY,
    [SessionID] [int] NULL,
    [StudentID] [int] NULL,
    [GameType] [nvarchar](50) NOT NULL,      -- ✅ Already stores game type!
    [Score] [int] NOT NULL,
    [AccuracyPercentage] [float] NULL,
    [TimeCompleted] [int] NULL,
    [XPEarned] [int] NULL,
    [StreakAchieved] [int] NULL,
    [DatePlayed] [datetime] DEFAULT GETDATE(),
    [LessonID] [int] NULL,                   -- ✅ Links to lesson

    FOREIGN KEY ([SessionID]) REFERENCES [TestSessions]([SessionID]),
    FOREIGN KEY ([StudentID]) REFERENCES [Students]([StudentID])
)
```

**Status:** ✅ **Perfect - already stores GameType per game played**
- Current game routing bug is Android-side, not database

---

#### 5. **LessonGameContent** ✅ (Stores game content)
```sql
CREATE TABLE [dbo].[LessonGameContent] (
    [ContentID] [int] IDENTITY(1,1) PRIMARY KEY,
    [LessonID] [int] NOT NULL,
    [GameType] [nvarchar](50) NOT NULL,
    [ContentText] [nvarchar](500) NOT NULL,
    [ContentData] [nvarchar](max) NULL,       -- JSON data for game
    [Difficulty] [float] DEFAULT 1.0,
    [Category] [nvarchar](100) NULL,
    [IsActive] [bit] DEFAULT 1,
    [CreatedDate] [datetime] DEFAULT GETDATE(),

    FOREIGN KEY ([LessonID]) REFERENCES [Lessons]([LessonID])
)
```

**Status:** ✅ **Good for storing game-specific content**

---

#### 6. **Items** ✅ (IRT assessment items with 3PL parameters)
```sql
CREATE TABLE [dbo].[Items] (
    [ItemID] [int] IDENTITY(1,1) PRIMARY KEY,
    [ItemText] [nvarchar](500) NOT NULL,
    [ItemType] [nvarchar](50) NOT NULL,
    [DifficultyLevel] [nvarchar](20) NOT NULL,
    [DifficultyParam] [float] NOT NULL,        -- b parameter
    [DiscriminationParam] [float] NOT NULL,    -- a parameter
    [GuessingParam] [float] NOT NULL,          -- c parameter
    [CorrectAnswer] [nvarchar](255) NOT NULL,
    [AnswerChoices] [nvarchar](max) NULL,
    [Phonetic] [nvarchar](100) NULL,
    [Definition] [nvarchar](500) NULL,
    [ImageURL] [nvarchar](255) NULL,
    [AudioURL] [nvarchar](255) NULL,
    [GradeLevel] [int] NOT NULL,
    [IsActive] [bit] NOT NULL DEFAULT 1,
    [CreatedAt] [datetime] NOT NULL DEFAULT GETDATE()
)
```

**Status:** ✅ **Full 3PL IRT implementation**

---

#### 7. **Responses** ✅ (IRT response tracking)
```sql
CREATE TABLE [dbo].[Responses] (
    [ResponseID] [int] IDENTITY(1,1) PRIMARY KEY,
    [SessionID] [int] NULL,
    [ItemID] [int] NULL,
    [StudentResponse] [nvarchar](500) NULL,
    [IsCorrect] [bit] NOT NULL,
    [TimeSpent] [int] NULL,
    [ThetaBeforeResponse] [float] NULL,    -- ✅ Ability before
    [ThetaAfterResponse] [float] NULL,     -- ✅ Ability after (updated)
    [Timestamp] [datetime] DEFAULT GETDATE(),

    FOREIGN KEY ([SessionID]) REFERENCES [TestSessions]([SessionID])
)
```

**Status:** ✅ **Tracks theta changes per response (IRT)**

---

#### 8. **TestSessions** ✅ (Assessment sessions)
```sql
CREATE TABLE [dbo].[TestSessions] (
    [SessionID] [int] IDENTITY(1,1) PRIMARY KEY,
    [StudentID] [int] NULL,
    [SessionType] [nvarchar](50) NOT NULL,
    [InitialTheta] [float] NULL,
    [FinalTheta] [float] NULL,
    [StartTime] [datetime] DEFAULT GETDATE(),
    [EndTime] [datetime] NULL,
    [TotalQuestions] [int] DEFAULT 0,
    [CorrectAnswers] [int] DEFAULT 0,
    [AccuracyPercentage] [float] NULL,
    [IsCompleted] [bit] DEFAULT 0,

    FOREIGN KEY ([StudentID]) REFERENCES [Students]([StudentID])
)
```

**Status:** ✅ **Tracks IRT assessment sessions**

---

#### 9. Other Supporting Tables ✅
- **Badges** - Badge definitions
- **StudentBadges** - Earned badges
- **ActivityLog** - Activity tracking
- **Teachers** - Teacher accounts
- **PronunciationAttempts** - Speech recognition tracking
- **PronunciationRecords** - Pronunciation scores
- **VocabularyWords** - Vocabulary bank

---

## 📋 Existing Stored Procedures

### IRT & Assessment
1. **SP_GetLessonsByAbility** - ✅ Returns lessons based on CurrentAbility
2. **SP_GetNextAdaptiveItem** - ✅ IRT-based next question selection
3. **SP_UpdateStudentAbility** - ✅ Updates theta after responses
4. **SP_GetPreAssessmentItems** - Pre-assessment items
5. **SP_StartPostAssessment** - Post-assessment initialization
6. **SP_SaveResponses** - Save IRT responses

### Progress Tracking
7. **SP_GetStudentProgress** - ✅ Returns student stats (used by API)
8. **SP_GetStudentDashboard** - Dashboard data
9. **SP_UpdateLessonProgress** - Update lesson completion
10. **SP_CreateTestSession** - Create test session
11. **SP_CompleteSession** - Complete test session

### Game Data
12. **SP_GetSentenceScrambleData** - Sentence scramble game content
13. **SP_GetTimedTrailData** - Timed trail game content
14. **SP_SaveGameResult** - ✅ Save game results

### Gamification
15. **SP_CheckBadgeUnlock** - Check badge conditions
16. **SP_GetImprovementReport** - Progress reports

### Authentication
17. **SP_StudentLogin** - Student login

---

## ❌ Missing Components for Adaptive System

### Missing Tables (Need to create)

#### 1. **Modules** Table
```sql
CREATE TABLE Modules (
    ModuleID INT IDENTITY(1,1) PRIMARY KEY,
    ModuleName NVARCHAR(255) NOT NULL,
    TotalLessons INT DEFAULT 12,
    AssessmentID INT NULL,  -- Node 13 (module assessment)
    ModuleOrder INT NOT NULL,
    GradeLevel INT NOT NULL,
    IsActive BIT DEFAULT 1,
    CreatedAt DATETIME DEFAULT GETDATE()
);
```

**Purpose:** Define modules (e.g., "Phonics and Word Study", "Reading Comprehension")

---

#### 2. **LessonBranches** Table
```sql
CREATE TABLE LessonBranches (
    BranchID INT IDENTITY(1,1) PRIMARY KEY,
    ParentLessonID INT NOT NULL,
    BranchType VARCHAR(20) NOT NULL,  -- 'intervention' or 'enrichment'
    Title NVARCHAR(255) NOT NULL,
    Description NVARCHAR(MAX) NULL,
    ContentData NVARCHAR(MAX) NULL,   -- JSON content
    RequiredAbility FLOAT NULL,
    CreatedAt DATETIME DEFAULT GETDATE(),

    FOREIGN KEY (ParentLessonID) REFERENCES Lessons(LessonID)
);
```

**Purpose:** Store intervention and enrichment branch content

---

#### 3. **StudentBranches** Table
```sql
CREATE TABLE StudentBranches (
    StudentBranchID INT IDENTITY(1,1) PRIMARY KEY,
    StudentID INT NOT NULL,
    BranchID INT NOT NULL,
    Status VARCHAR(20) DEFAULT 'locked',  -- 'locked', 'unlocked', 'completed'
    UnlockedAt DATETIME NULL,
    CompletedAt DATETIME NULL,
    Score INT NULL,

    FOREIGN KEY (StudentID) REFERENCES Students(StudentID),
    FOREIGN KEY (BranchID) REFERENCES LessonBranches(BranchID),
    UNIQUE (StudentID, BranchID)
);
```

**Purpose:** Track which branches each student has unlocked/completed

---

### Missing Stored Procedures (Need to create)

#### 1. **SP_GetModuleStructure**
```sql
CREATE OR ALTER PROCEDURE SP_GetModuleStructure
    @StudentID INT,
    @ModuleID INT
AS
BEGIN
    -- Returns:
    -- - 12 lessons with Quarter, LessonNumber, GameType
    -- - 1 assessment node (Node 13)
    -- - Student's progress on each node
    -- - Branching status (intervention/enrichment locked/unlocked)
END
```

**Purpose:** Return complete 13-node structure with student progress

---

#### 2. **SP_UpdateQuizScore**
```sql
CREATE OR ALTER PROCEDURE SP_UpdateQuizScore
    @StudentID INT,
    @LessonID INT,
    @QuizScore INT
AS
BEGIN
    -- 1. Update StudentProgress.Score
    -- 2. Check quiz score against thresholds
    -- 3. If < InterventionThreshold: Unlock intervention branch, return 'intervention_required'
    -- 4. If >= EnrichmentThreshold: Unlock enrichment branch, return 'enrichment_unlocked'
    -- 5. Else: Return 'proceed_standard'
END
```

**Purpose:** Update quiz score and determine branching decision

---

#### 3. **SP_GetLessonBranches**
```sql
CREATE OR ALTER PROCEDURE SP_GetLessonBranches
    @StudentID INT,
    @LessonID INT
AS
BEGIN
    -- Returns intervention/enrichment branches for a lesson
    -- Includes student's progress on each branch
END
```

**Purpose:** Get branch content and status

---

#### 4. **SP_CompleteBranch**
```sql
CREATE OR ALTER PROCEDURE SP_CompleteBranch
    @StudentID INT,
    @BranchID INT,
    @Score INT
AS
BEGIN
    -- Mark branch as completed
    -- Award XP
    -- Update StudentBranches status to 'completed'
END
```

**Purpose:** Mark branch completion

---

## 🔄 Comparison: Current vs. Required

| Feature | Current | Required | Status |
|---------|---------|----------|--------|
| **Student Ability Tracking** | ✅ CurrentAbility, Multi-domain Theta | Same | ✅ Ready |
| **IRT System** | ✅ 3PL model, Items, Responses | Same | ✅ Ready |
| **Lessons Table** | ⚠️ Basic structure | + ModuleID, Quarter, LessonNumber, GameType, Thresholds | ⚠️ Needs 6 columns |
| **Module System** | ❌ No Modules table | 13-node modules (12 lessons + 1 assessment) | ❌ Create table |
| **Branching System** | ❌ No branches | LessonBranches, StudentBranches tables | ❌ Create tables |
| **Game Type Assignment** | ⚠️ Only in GameResults | GameType in Lessons table | ⚠️ Add column |
| **Quiz Score Thresholds** | ❌ No thresholds | InterventionThreshold (60), EnrichmentThreshold (85) | ❌ Add columns |
| **Progress Tracking** | ✅ StudentProgress | Same | ✅ Ready |
| **API Endpoints** | ✅ get_lessons.php, get_student_progress.php | + 4 new endpoints | ⚠️ Need new endpoints |

---

## 📝 Implementation Checklist

### Day 1: Database & API (Backend)

#### ✅ Database Changes (Run adaptive_schema_update.sql)
- [ ] **Add columns to Lessons table:**
  - ModuleID, Quarter, LessonNumber
  - GameType
  - InterventionThreshold, EnrichmentThreshold

- [ ] **Create new tables:**
  - Modules
  - LessonBranches
  - StudentBranches

- [ ] **Create stored procedures:**
  - SP_GetModuleStructure
  - SP_UpdateQuizScore
  - SP_GetLessonBranches
  - SP_CompleteBranch

- [ ] **Seed Module 1:**
  - Insert "Phonics and Word Study" module
  - Insert 12 lessons with Quarter 1-4, GameTypes assigned
  - Insert intervention/enrichment branches

#### ✅ PHP API Changes
- [ ] **Create new API endpoints:**
  - `/api/get_module_structure.php` - Returns 13 nodes
  - `/api/update_quiz_score.php` - Updates score, checks branching
  - `/api/get_lesson_branches.php` - Returns branches
  - `/api/complete_branch.php` - Marks branch completed

- [ ] **Update existing endpoint:**
  - `/api/get_lessons.php` - Use SP_GetModuleStructure instead of SP_GetLessonsByAbility

---

### Day 2: Android Integration (Frontend)

#### ✅ Data Models
- [ ] Update `Lesson.java` - Add Quarter, LessonNumber fields
- [ ] Create `Module.java` - Module model
- [ ] Create `LessonBranch.java` - Branch model
- [ ] Create `BranchingDecision.java` - Branching response model

#### ✅ API Service
- [ ] Add endpoints to `ApiService.java`:
  ```java
  @POST("get_module_structure.php")
  Call<ModuleStructureResponse> getModuleStructure(@Body ModuleRequest request);

  @POST("update_quiz_score.php")
  Call<BranchingDecision> updateQuizScore(@Body QuizScoreRequest request);
  ```

#### ✅ UI Updates
- [ ] Update `ModuleLadderActivity.java` - Show 13 nodes
- [ ] Add branching node visualization (intervention/enrichment)
- [ ] Update SQLite cache for new structure

---

### Day 3: Lesson Flow & Testing

#### ✅ Lesson Flow Redesign
- [ ] Redesign `ModuleLessonActivity.java` - Content → Quiz → Game flow
- [ ] Create `InterventionActivity.java` - Intervention lessons
- [ ] Create `EnrichmentActivity.java` - Enrichment content

#### ✅ Testing
- [ ] Test Beginner path (θ < -1): Intervention unlocks
- [ ] Test Intermediate path (θ ≈ 0): Standard progression
- [ ] Test Advanced path (θ > 1): Enrichment unlocks
- [ ] Test complete module cycle: 12 lessons + assessment

---

## 🚀 Quick Start Commands

### 1. Run Database Migration
```bash
sqlcmd -S DESKTOP-PEM6F9E\SQLEXPRESS -d LiteRiseDB -i adaptive_schema_update.sql
```

### 2. Verify Schema Changes
```sql
-- Check new columns in Lessons
SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'Lessons';

-- Check new tables
SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_NAME IN ('Modules', 'LessonBranches', 'StudentBranches');

-- Check new stored procedures
SELECT ROUTINE_NAME FROM INFORMATION_SCHEMA.ROUTINES
WHERE ROUTINE_NAME LIKE 'SP_Get%Module%';
```

### 3. Test Module 1 Data
```sql
-- Should return Module 1: Phonics and Word Study
SELECT * FROM Modules WHERE ModuleID = 1;

-- Should return 12 lessons with GameTypes
SELECT LessonID, LessonNumber, Quarter, GameType, InterventionThreshold
FROM Lessons WHERE ModuleID = 1 ORDER BY LessonNumber;

-- Should return intervention/enrichment branches
SELECT * FROM LessonBranches WHERE ParentLessonID BETWEEN 101 AND 112;
```

---

## 📊 Database Size Estimates

### Current Database Size
- **Students:** ~1000 rows (test data)
- **Lessons:** ~50 rows (scattered lessons)
- **Items:** ~200 IRT items
- **GameResults:** ~5000 rows (game plays)

### After Adaptive Migration
- **Modules:** +5 rows (5 modules)
- **Lessons:** Update 50 existing + Add ~60 new = 110 total
- **LessonBranches:** +120 rows (2 branches per lesson × 60 lessons)
- **StudentBranches:** Dynamic (grows with student usage)

**Storage Impact:** < 5MB additional space

---

## ⚠️ Potential Issues & Solutions

### Issue 1: Existing Lessons Table Has Data
**Problem:** Can't drop and recreate Lessons table (has foreign keys)
**Solution:** Use `ALTER TABLE ADD COLUMN` (already in adaptive_schema_update.sql)

### Issue 2: LessonID Conflicts
**Problem:** Current lessons use random IDs, new system needs 101-112 for Module 1
**Solution:** adaptive_schema_update.sql uses `IDENTITY_INSERT` to force IDs

### Issue 3: API Compatibility
**Problem:** Old Android app versions expect old API format
**Solution:** Keep existing endpoints, add new ones, version API (v2/)

### Issue 4: Game Type Constants Mismatch
**Problem:** Android constants might not match database strings
**Solution:** Enforce constants in migration script:
```sql
CHECK (GameType IN ('word_hunt', 'sentence_scramble', 'timed_trail',
                    'shadow_read', 'minimal_pairs', 'traditional'))
```

---

## 📝 Next Immediate Steps

1. ✅ **Analyze current structure** (THIS DOCUMENT - COMPLETE)
2. **Run adaptive_schema_update.sql**
3. **Verify migration success**
4. **Create 4 new PHP API endpoints**
5. **Test endpoints with Postman**
6. **Update Android models**
7. **Update ModuleLadderActivity UI**

---

## 🎯 Success Metrics

### End of Day 1
- [ ] Database has Modules, LessonBranches, StudentBranches tables
- [ ] Lessons table has 6 new columns
- [ ] Module 1 seeded with 12 lessons
- [ ] 4 new stored procedures working
- [ ] 4 new API endpoints returning data

### End of Day 2
- [ ] Android app fetches 13-node structure
- [ ] Ladder shows 12 lessons + 1 assessment
- [ ] Game badges visible on nodes
- [ ] Branching nodes appear based on quiz scores

### End of Day 3
- [ ] Content → Quiz → Game flow works
- [ ] Intervention branches unlock correctly
- [ ] Enrichment branches unlock for high scores
- [ ] Complete lesson cycle functional
- [ ] Progress saves to database

---

**Generated:** 2026-01-18
**Status:** Ready for implementation
**Timeline:** 2-3 days
**Risk Level:** Low (extends existing schema, doesn't break current functionality)
