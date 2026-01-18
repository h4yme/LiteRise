# Android App Structure Analysis - LiteRise

**Analysis Date:** 2026-01-18
**Purpose:** Document current Android app architecture before adaptive learning implementation
**Branch Analyzed:** master
**Timeline:** 2-3 day implementation plan

---

## 📱 Executive Summary

### Current Architecture
- **Pattern:** MVC-like with API-driven data layer
- **API Client:** Retrofit 2 + OkHttp
- **Local Storage:** SQLite (LessonDatabase) + SharedPreferences (SessionManager)
- **UI Framework:** Material Design 3 + Custom Views
- **Language:** Java
- **Min SDK:** API 21 (Android 5.0)

### Key Features Implemented
- ✅ **IRT-based Placement Test:** 28-question adaptive assessment
- ✅ **Module System:** 5 Key Stage 1 modules with priority ordering
- ✅ **Game Activities:** Word Hunt, Sentence Scramble, Dialogue Reading, etc.
- ✅ **Gamification:** XP, streaks, badges
- ✅ **Pronunciation Check:** Speech recognition integration
- ✅ **Progress Tracking:** Lesson completion, quiz scores, game results

### What Needs to Change for Adaptive System
- ⚠️ **Module Structure:** Currently 15 lessons → Change to 13 nodes (12 lessons + 1 assessment)
- ⚠️ **Lesson Flow:** 3-tab structure (Content/Practice/Quiz) → Sequential (Content → Quiz → Game)
- ❌ **Branching System:** No intervention/enrichment pathways → Add branching activities
- ⚠️ **Data Models:** Need Quarter, LessonNumber fields in Lesson model
- ❌ **Branching Models:** Need Module, LessonBranch, BranchingDecision models

---

## 📦 Package Structure

```
com.example.literise/
├── activities/               # All activity classes
│   ├── games/                # Game-specific activities
│   │   ├── WordHuntActivity.java
│   │   ├── SentenceScrambleActivity.java
│   │   ├── DialogueReadingActivity.java
│   │   ├── FillInTheBlanksActivity.java
│   │   ├── PictureMatchActivity.java
│   │   └── StorySequencingActivity.java
│   ├── BaseActivity.java            # Base class for all activities
│   ├── DashboardActivity.java       # Main dashboard with module list
│   ├── ModuleLadderActivity.java    # ⚠️ Ladder view (15 nodes → needs 13)
│   ├── ModuleLessonActivity.java    # ⚠️ Lesson view (3 tabs → needs redesign)
│   ├── LessonActivity.java          # Old lesson activity
│   ├── PlacementTestActivity.java   # IRT placement test
│   ├── PlacementIntroActivity.java  # Placement intro
│   ├── PlacementResultActivity.java # Placement results
│   ├── AssessmentResultsActivity.java
│   ├── LoginActivity.java
│   ├── RegisterActivity.java
│   ├── SettingsActivity.java
│   └── WelcomeOnboardingActivity.java
│
├── adapters/                 # RecyclerView adapters
│   ├── ModuleAdapter.java           # Dashboard module grid
│   ├── QuestionAdapter.java         # Quiz question list
│   ├── OnboardingSlideAdapter.java
│   ├── RegisterPagerAdapter.java
│   └── TutorialPagerAdapter.java
│
├── api/                      # Retrofit API layer
│   ├── ApiService.java              # ✅ API endpoints (needs 4 new endpoints)
│   ├── ApiClient.java               # Retrofit client setup
│   └── AuthInterceptor.java         # JWT token interceptor
│
├── content/                  # ⚠️ Content providers
│   └── Module1ContentProvider.java  # Hardcoded lesson content for Module 1
│
├── database/                 # Local database
│   ├── LessonDatabase.java          # SQLite helper (offline cache)
│   ├── SessionManager.java          # SharedPreferences wrapper
│   └── QuestionBankHelper.java      # Question bank storage
│
├── fragments/                # UI fragments
│   ├── RegisterStep1Fragment.java   # Multi-step registration
│   ├── RegisterStep2Fragment.java
│   ├── RegisterStep3Fragment.java
│   ├── RegisterStep4Fragment.java
│   └── TutorialFragment.java
│
├── helpers/                  # Helper classes
│   ├── AdaptiveQuestionHelper.java  # IRT question selection
│   └── PronunciationHelper.java     # Speech recognition
│
├── ml/                       # Machine learning
│   └── PlacementMLPredictor.java    # Placement test ML model
│
├── models/                   # Data models (POJOs)
│   ├── Lesson.java                  # ⚠️ Needs Quarter, LessonNumber fields
│   ├── LearningModule.java          # Module model (5 modules)
│   ├── Question.java                # Quiz question
│   ├── Students.java                # Student profile
│   ├── Badge.java                   # Badge model
│   ├── GameSession.java
│   ├── (30+ other models for API requests/responses)
│   └── ...
│
├── utils/                    # Utility classes
│   ├── IRTCalculator.java           # ✅ IRT 3PL calculations
│   ├── IRTEngine.java               # IRT engine
│   ├── GamificationManager.java     # XP, badges, streaks
│   ├── ModulePriorityManager.java   # Module ordering by placement
│   ├── ModuleOrderingHelper.java
│   ├── SessionLogger.java           # Activity logging
│   ├── TextToSpeechHelper.java      # TTS integration
│   ├── SpeechRecognitionHelper.java # STT integration
│   ├── SoundEffectsHelper.java
│   ├── MusicManager.java
│   ├── CustomToast.java
│   ├── Constants.java               # App constants
│   └── AppConfig.java               # Configuration
│
├── views/                    # Custom views
│   ├── LeoDialogueView.java         # Leo mascot dialogue view
│   └── ...
│
└── MainActivity.java                # Entry point
```

---

## 🔌 API Integration (ApiService.java)

### Current API Endpoints

#### Authentication
```java
@POST("login.php")
Call<Students> login(@Body Students student);

@POST("register.php")
Call<RegisterResponse> register(@Body RegisterRequest request);

@POST("forgot_password.php")
Call<ForgotPasswordResponse> forgotPassword(@Body ForgotPasswordRequest request);
```

#### IRT Assessment
```java
@POST("get_preassessment_items.php")
Call<PreAssessmentResponse> getPreAssessmentItems();

@POST("get_next_question.php")  // IRT-based next question
Call<AdaptiveQuestionResponse> getNextAdaptiveQuestion(@Body AdaptiveQuestionRequest request);

@POST("submit_answer.php")  // IRT-based answer submission
Call<SubmitAnswerResponse> submitAnswer(@Body SubmitAnswerRequest request);

@POST("submit_responses.php")
Call<SubmitResponseResult> submitResponses(@Body SubmitRequest request);

@POST("update_ability.php")  // Update student theta
Call<Void> updateAbility(@Body Students student);
```

#### Game Content & Results
```java
@POST("get_word_hunt.php")
Call<WordHuntResponse> getWordHuntWords(@Query("count") int count, @Query("student_id") int studentId);

@POST("get_scramble_sentences.php")
Call<ScrambleSentenceResponse> getScrambleSentences(@Query("count") int count, @Query("lesson_id") int lessonId);

@POST("save_game_results.php")
Call<SaveGameResultResponse> saveGameResult(@Body SaveGameResultRequest request);
```

#### Progress & Session Tracking
```java
@GET("get_lesson_progress.php")
Call<LessonProgressResponse> getLessonProgress(@Query("student_id") int studentId, @Query("lesson_id") int lessonId);

@POST("log_session.php")
Call<LogSessionResponse> logSession(@Body LogSessionRequest request);

@POST("save_placement_result.php")
Call<SavePlacementResultResponse> savePlacementResult(@Body SavePlacementResultRequest request);

@GET("get_placement_progress.php")
Call<PlacementProgressResponse> getPlacementProgress(@Query("student_id") int studentId);
```

#### Speech Recognition
```java
@POST("check_pronunciation.php")
Call<PronunciationResponse> checkPronunciation(@Body PronunciationRequest request);
```

### ❌ Missing API Endpoints for Adaptive System
```java
// NEED TO ADD (Day 2):

@POST("get_module_structure.php")
Call<ModuleStructureResponse> getModuleStructure(@Body ModuleRequest request);
// Returns: 13 nodes (12 lessons + 1 assessment), student progress, branching status

@POST("update_quiz_score.php")
Call<BranchingDecision> updateQuizScore(@Body QuizScoreRequest request);
// Returns: 'intervention_required', 'enrichment_unlocked', or 'proceed_standard'

@POST("get_lesson_branches.php")
Call<BranchesResponse> getLessonBranches(@Body BranchRequest request);
// Returns: Intervention/enrichment content for a lesson

@POST("complete_branch.php")
Call<CompleteBranchResponse> completeBranch(@Body CompleteBranchRequest request);
// Marks branch as completed, awards XP
```

---

## 📊 Key Data Models

### 1. **Lesson.java** ⚠️ (Needs extension)

**Current Structure:**
```java
public class Lesson {
    // Game types
    public static final String GAME_SENTENCE_SCRAMBLE = "sentence_scramble";
    public static final String GAME_TIMED_TRAIL = "timed_trail";
    public static final String GAME_WORD_HUNT = "word_hunt";
    public static final String GAME_SHADOW_READ = "shadow_read";
    public static final String GAME_MINIMAL_PAIRS = "minimal_pairs";
    public static final String GAME_TRADITIONAL = "traditional";

    private int lessonId;
    private int moduleId;
    private int lessonNumber;           // ✅ Already has this (1-15)
    private String title;
    private String tier;                // "Foundation", "Intermediate", "Advanced"
    private String description;
    private String gameType;            // ✅ Already has game type
    private String content;
    private List<String> learningObjectives;
    private List<Question> practiceQuestions;
    private List<Question> quizQuestions;
    private int xpReward;
    private int estimatedMinutes;

    // Progress tracking
    private boolean isUnlocked;
    private boolean isCompleted;
    private int practiceScore;
    private int quizScore;              // ✅ Already tracks quiz score
    private int attempts;
    private long completedTimestamp;
}
```

**Needs to Add for Adaptive System:**
```java
// ADD THESE FIELDS:
private int quarter;                    // 1-4 (for quarterly distribution)
private int interventionThreshold;      // Default 60
private int enrichmentThreshold;        // Default 85
private boolean hasIntervention;        // Does this lesson have intervention branch?
private boolean hasEnrichment;          // Does this lesson have enrichment branch?
private String interventionStatus;      // "locked", "unlocked", "completed"
private String enrichmentStatus;        // "locked", "unlocked", "completed"
```

**Status:** ⚠️ Model exists but needs 7 new fields

---

### 2. **LearningModule.java** ✅ (Already exists, good foundation)

```java
public class LearningModule {
    private int moduleId;
    private String title;               // e.g., "Phonics and Word Study"
    private String subtitle;
    private String domain;              // Key Stage 1 domain
    private int level;                  // Student's current level
    private int totalLevels;            // ⚠️ Currently 5, needs to be 13 for adaptive
    private boolean isLocked;
    private double performanceScore;    // From placement test
    private int priorityOrder;          // Display order
    private String gradientStart;       // UI gradient colors
    private String gradientEnd;
    private String iconResName;
}
```

**Needs to Update:**
- Change `totalLevels` from 5 → 13 (12 lessons + 1 assessment)

---

### 3. ❌ **Missing Models for Adaptive System**

Need to create these new models:

#### **Module.java** (New)
```java
public class Module {
    private int moduleId;
    private String moduleName;
    private int totalLessons;           // 12
    private int assessmentId;           // LessonID of assessment (Node 13)
    private int moduleOrder;
    private int gradeLevel;
    private boolean isActive;

    // Student progress
    private int completedLessons;
    private int currentLessonId;
    private double progressPercentage;
}
```

#### **LessonBranch.java** (New)
```java
public class LessonBranch {
    private int branchId;
    private int parentLessonId;
    private String branchType;          // "intervention" or "enrichment"
    private String title;
    private String description;
    private String contentData;         // JSON content
    private float requiredAbility;

    // Student progress
    private String status;              // "locked", "unlocked", "completed"
    private int score;
    private long unlockedAt;
    private long completedAt;
}
```

#### **BranchingDecision.java** (New - API response)
```java
public class BranchingDecision {
    private boolean success;
    private String decision;            // "intervention_required", "enrichment_unlocked", "proceed_standard"
    private String message;
    private List<LessonBranch> unlockedBranches;
    private int nextLessonId;
    private int xpAwarded;
}
```

#### **ModuleStructureResponse.java** (New - API response)
```java
public class ModuleStructureResponse {
    private boolean success;
    private Module module;
    private List<Lesson> lessons;       // 12 lessons
    private Lesson assessment;          // Node 13
    private StudentProgress studentProgress;
}
```

---

## 🎮 Current Game Activities

### Implemented Games ✅
1. **WordHuntActivity** - Word search grid game
2. **SentenceScrambleActivity** - Sentence ordering game
3. **DialogueReadingActivity** - Read-aloud dialogue with karaoke text
4. **FillInTheBlanksActivity** - Fill-in-the-blank sentences
5. **PictureMatchActivity** - Match pictures to words
6. **StorySequencingActivity** - Sequence story events

### Game Flow (Current)
```
ModuleLadderActivity (15 nodes)
    ↓ Click node
ModuleLessonActivity (3 tabs: Content | Practice | Quiz)
    ↓ Click "Play Game" button
WordHuntActivity / SentenceScrambleActivity / etc.
    ↓ Complete game
Results → XP awarded → Back to ladder
```

### Game Flow (Adaptive - Target)
```
ModuleLadderActivity (13 nodes)
    ↓ Click node
Interactive Content Activity (NEW - engaging content)
    ↓ Auto-advance
Quiz Activity (5-10 questions)
    ↓ Submit quiz → API call to update_quiz_score.php
    ├─ If score < 60%: Unlock Intervention → InterventionActivity (NEW)
    │                   ↓ Complete → Retry Quiz
    ├─ If score >= 85%: Unlock Enrichment → EnrichmentActivity (NEW - optional)
    └─ Else: Proceed
Game Activity (word_hunt, sentence_scramble, etc.)
    ↓ Complete game
Results → Mark lesson complete → Next lesson unlocks
```

---

## 🎯 Key Activities Analysis

### 1. **DashboardActivity.java** ✅ (Good, minimal changes)

**Purpose:** Main dashboard showing:
- Student stats (XP, streaks, badges)
- Leo mascot with motivational messages
- Module grid (5 modules ordered by placement test results)
- Bottom navigation

**What it does:**
- Loads student data from `SessionManager`
- Uses `ModulePriorityManager` to order modules by placement performance
- Displays modules in `RecyclerView` with `ModuleAdapter`
- Click module → Opens `ModuleLadderActivity`

**Changes needed:** ✅ None (already works with adaptive system)

---

### 2. **ModuleLadderActivity.java** ⚠️ (Needs update: 15 → 13 nodes)

**Purpose:** Displays lesson nodes in vertical ladder/path view

**Current Implementation:**
```java
private int totalLessons = 15;  // ⚠️ Needs to change to 13
private int currentLesson = 1;

private void displayLessonNodes() {
    // Creates 15 lesson nodes in zigzag pattern
    for (int i = totalLessons; i >= 1; i--) {
        // Create node view
        // Position: left/center/right alternating
        // Status: locked/unlocked/completed
        // Click: openLesson(lessonId)
    }
}

private void openLesson(int lessonNumber) {
    Intent intent = new Intent(this, ModuleLessonActivity.class);
    intent.putExtra("lesson_id", getLessonId(lessonNumber));
    intent.putExtra("module_id", moduleId);
    startActivity(intent);
}
```

**What needs to change:**
- `totalLessons = 15` → `totalLessons = 13`
- Node rendering:
  - Nodes 1-12: Regular lessons with game type badges
  - Node 13: Module assessment (different icon/color)
- Add branching node visualization:
  - If lesson has intervention unlocked: Show intervention branch node (left side)
  - If lesson has enrichment unlocked: Show enrichment branch node (right side)
- Fetch structure from API: Call `get_module_structure.php` instead of hardcoded

**Changes Required:**
```java
// Change from:
private int totalLessons = 15;

// To:
private int totalLessons = 12;  // 12 lessons
private boolean showAssessment = true;  // + 1 assessment node

// Add method to load from API:
private void loadModuleStructure() {
    apiService.getModuleStructure(moduleId, studentId)
        .enqueue(new Callback<ModuleStructureResponse>() {
            @Override
            public void onResponse(...) {
                displayNodes(response.lessons);
                displayAssessmentNode(response.assessment);
                displayBranchingNodes(response.lessons);
            }
        });
}
```

---

### 3. **ModuleLessonActivity.java** ⚠️ (MAJOR REDESIGN NEEDED)

**Current Implementation:** 3-tab structure
```java
TabLayout: Content | Practice | Quiz

Tab 1 - Content:
    - HTML-formatted lesson content
    - Learning objectives
    - Scrollable text view

Tab 2 - Practice:
    - 10 practice questions
    - Non-graded, immediate feedback

Tab 3 - Quiz:
    - 5-10 graded questions
    - Submit → Save quiz score
    - If passed (≥70%): Show "Play Game" button
```

**What needs to change:** ❌ **COMPLETE REDESIGN**

**New Flow (Adaptive System):**
```java
// Sequential flow instead of tabs:

Step 1: Interactive Content
    - NOT just static text
    - Interactive elements (drag-drop, animations, etc.)
    - Mini-activities embedded in content
    - "Continue" button when done

Step 2: Quiz (Knowledge Check)
    - 5-10 questions
    - Submit → API call: update_quiz_score.php
    - Response: BranchingDecision
        - If intervention_required:
            → Show intervention message
            → Start InterventionActivity
            → After intervention: Retry quiz
        - If enrichment_unlocked:
            → Show enrichment option (optional)
            → Can skip or do EnrichmentActivity
        - Else:
            → Proceed to game

Step 3: Game (Reinforcement)
    - Based on gameType field
    - Launch appropriate game activity
    - Award XP on completion
    - Mark lesson as complete
```

**New Activity Structure Needed:**
```java
// ModuleLessonActivity becomes a state machine:

enum LessonState {
    INTERACTIVE_CONTENT,
    QUIZ,
    INTERVENTION,
    ENRICHMENT,
    GAME,
    COMPLETED
}

private LessonState currentState = INTERACTIVE_CONTENT;

@Override
protected void onCreate(Bundle savedInstanceState) {
    // Load lesson data
    // Start with INTERACTIVE_CONTENT
    showInteractiveContent();
}

private void showInteractiveContent() {
    // Load interactive content
    // When done: moveToQuiz()
}

private void moveToQuiz() {
    currentState = QUIZ;
    // Show quiz questions
}

private void submitQuiz(int score) {
    // API call: update_quiz_score.php
    apiService.updateQuizScore(lessonId, studentId, score)
        .enqueue(new Callback<BranchingDecision>() {
            @Override
            public void onResponse(...) {
                handleBranchingDecision(response.decision);
            }
        });
}

private void handleBranchingDecision(BranchingDecision decision) {
    switch (decision.getDecision()) {
        case "intervention_required":
            showInterventionRequired();
            break;
        case "enrichment_unlocked":
            showEnrichmentOption();
            break;
        case "proceed_standard":
            moveToGame();
            break;
    }
}
```

---

### 4. ❌ **InterventionActivity.java** (NEW - Need to create)

**Purpose:** Remedial content for students who scored < 60% on quiz

**What it should do:**
- Load intervention content from API (`get_lesson_branches.php`)
- Present simplified explanations and practice
- Simpler vocabulary, more scaffolding
- Shorter exercises
- Auto-advance after completion
- Mark intervention as complete → Unlock quiz retry

**Structure:**
```java
public class InterventionActivity extends AppCompatActivity {
    private LessonBranch interventionBranch;
    private int lessonId;
    private int studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        lessonId = getIntent().getIntExtra("lesson_id", 0);
        studentId = SessionManager.getStudentId();

        loadInterventionContent();
    }

    private void loadInterventionContent() {
        apiService.getLessonBranches(studentId, lessonId)
            .enqueue(new Callback<BranchesResponse>() {
                @Override
                public void onResponse(...) {
                    interventionBranch = response.getInterventionBranch();
                    displayContent(interventionBranch.getContentData());
                }
            });
    }

    private void completeIntervention() {
        apiService.completeBranch(studentId, interventionBranch.getBranchId(), score)
            .enqueue(new Callback<CompleteBranchResponse>() {
                @Override
                public void onResponse(...) {
                    // Return to lesson with intervention completed
                    setResult(RESULT_OK);
                    finish();
                }
            });
    }
}
```

---

### 5. ❌ **EnrichmentActivity.java** (NEW - Need to create)

**Purpose:** Advanced content for students who scored ≥ 85% on quiz

**What it should do:**
- Load enrichment content from API
- Present Key Stage 2 preview content
- More challenging exercises
- Optional (can skip)
- Award extra XP if completed

**Structure:** Similar to InterventionActivity but:
- More complex vocabulary
- Advanced concepts
- Connects to Key Stage 2 content
- "Skip" button available (optional)

---

## 🗄️ Local Database (SQLite)

### Current Implementation: `LessonDatabase.java`

**Purpose:** Offline caching of lesson data

**Tables (Current):**
```sql
CREATE TABLE lessons (
    lesson_id INTEGER PRIMARY KEY,
    module_id INTEGER,
    title TEXT,
    tier TEXT,
    content TEXT,
    quiz_questions TEXT  -- JSON
);

CREATE TABLE student_progress (
    student_id INTEGER,
    lesson_id INTEGER,
    is_completed INTEGER,
    quiz_score INTEGER,
    PRIMARY KEY (student_id, lesson_id)
);
```

**What needs to change:**
```sql
-- Add columns to lessons table:
ALTER TABLE lessons ADD COLUMN quarter INTEGER;
ALTER TABLE lessons ADD COLUMN lesson_number INTEGER;
ALTER TABLE lessons ADD COLUMN game_type TEXT;
ALTER TABLE lessons ADD COLUMN intervention_threshold INTEGER DEFAULT 60;
ALTER TABLE lessons ADD COLUMN enrichment_threshold INTEGER DEFAULT 85;

-- Create new table for branches:
CREATE TABLE lesson_branches (
    branch_id INTEGER PRIMARY KEY,
    parent_lesson_id INTEGER,
    branch_type TEXT,  -- 'intervention' or 'enrichment'
    title TEXT,
    content_data TEXT  -- JSON
);

-- Create new table for student branch progress:
CREATE TABLE student_branches (
    student_id INTEGER,
    branch_id INTEGER,
    status TEXT,  -- 'locked', 'unlocked', 'completed'
    score INTEGER,
    PRIMARY KEY (student_id, branch_id)
);
```

---

## 🎨 UI/UX Flow (Current vs. Adaptive)

### Current Flow
```
Splash Screen
    ↓
Login / Register
    ↓
Placement Test (28 questions, IRT-based)
    ↓
Placement Results → Modules ordered by performance
    ↓
Dashboard (Module grid)
    ↓ Click module
Module Ladder (15 nodes in zigzag pattern)
    ↓ Click node
Lesson Activity (3 tabs: Content | Practice | Quiz)
    ↓ Complete quiz
    ↓ If passed: "Play Game" button appears
Game Activity (Word Hunt, Sentence Scramble, etc.)
    ↓
Results → XP awarded → Back to ladder
```

### Adaptive Flow (Target)
```
Splash Screen
    ↓
Login / Register
    ↓
Placement Test (28 questions, IRT-based) ✅ Same
    ↓
Placement Results ✅ Same
    ↓
Dashboard (Module grid) ✅ Same
    ↓ Click module
Module Ladder (13 nodes: 12 lessons + 1 assessment) ⚠️ Changed
    ↓ Click lesson node
Interactive Content Activity (NEW - engaging, not static) ❌ New
    ↓ Auto-advance
Quiz Activity (5-10 questions)
    ↓ Submit quiz
    ↓ API call: update_quiz_score.php ❌ New endpoint
    ├─ If score < 60%:
    │   Intervention Activity (NEW) ❌ New
    │       ↓ Complete
    │   Retry Quiz
    ├─ If score >= 85%:
    │   Enrichment Option (NEW - optional) ❌ New
    │       ↓ Can skip or complete
    └─ Else: Proceed
Game Activity ✅ Same games
    ↓ Complete
Mark Lesson Complete → Next Lesson Unlocks
```

---

## 📝 Implementation Checklist for Android

### Day 2: Android Models & API Integration

#### ✅ Update Existing Models
- [ ] **Lesson.java** - Add fields:
  ```java
  private int quarter;
  private int interventionThreshold;
  private int enrichmentThreshold;
  private boolean hasIntervention;
  private boolean hasEnrichment;
  private String interventionStatus;
  private String enrichmentStatus;
  ```

- [ ] **LearningModule.java** - Update:
  ```java
  private int totalLevels = 13;  // Change from 5
  ```

#### ❌ Create New Models
- [ ] **Module.java** - Server-side module model
- [ ] **LessonBranch.java** - Intervention/enrichment branch
- [ ] **BranchingDecision.java** - API response model
- [ ] **ModuleStructureResponse.java** - 13-node structure response
- [ ] **QuizScoreRequest.java** - Quiz score submission
- [ ] **BranchesResponse.java** - Branches for a lesson
- [ ] **CompleteBranchRequest.java** - Branch completion

#### ✅ Update API Service
- [ ] Add to `ApiService.java`:
  ```java
  @POST("get_module_structure.php")
  Call<ModuleStructureResponse> getModuleStructure(@Body ModuleRequest request);

  @POST("update_quiz_score.php")
  Call<BranchingDecision> updateQuizScore(@Body QuizScoreRequest request);

  @POST("get_lesson_branches.php")
  Call<BranchesResponse> getLessonBranches(@Body BranchRequest request);

  @POST("complete_branch.php")
  Call<CompleteBranchResponse> completeBranch(@Body CompleteBranchRequest request);
  ```

#### ⚠️ Update Activities
- [ ] **ModuleLadderActivity.java:**
  - Change `totalLessons = 15` → `totalLessons = 12`
  - Add assessment node (Node 13)
  - Add `loadModuleStructure()` method to fetch from API
  - Add branching node visualization

- [ ] **ModuleLessonActivity.java:** (MAJOR REDESIGN)
  - Remove TabLayout (3 tabs)
  - Implement state machine: Content → Quiz → Branching → Game
  - Add `submitQuiz()` with API call
  - Add `handleBranchingDecision()` logic

#### ❌ Create New Activities
- [ ] **InterventionActivity.java** - Remedial content
- [ ] **EnrichmentActivity.java** - Advanced content
- [ ] **ModuleAssessmentActivity.java** - Node 13 assessment (optional for v1)

#### ✅ Update Database
- [ ] **LessonDatabase.java:**
  - Add new columns to lessons table
  - Create lesson_branches table
  - Create student_branches table
  - Add cache methods: `cacheModuleStructure()`, `cacheBranches()`

---

## 🔧 Current Issues to Fix

### Issue 1: Game Routing Bug (Unresolved)
**Problem:** All lessons returning `gameType = "traditional"` despite `setGameType()` calls
**Location:** `ModuleLadderActivity:getLessonGameType()`, `Module1ContentProvider`
**Status:** ❌ **Still broken** (discussed in previous session)

**Diagnosis:**
- `Lesson.java` has correct game type constants
- `Module1ContentProvider.createLesson1()` calls `setGameType(GAME_WORD_HUNT)`
- Debug logs added but issue persists even after clean rebuild
- **Likely cause:** APK caching or serialization issue

**Fix needed:**
1. Verify `Module1ContentProvider.getAllLessons()` returns lessons with correct game types
2. Check if lessons are being recreated somewhere (losing game type)
3. May need to rebuild from scratch or clear Android Studio cache

---

## 🎯 Success Criteria

### End of Day 2 (Android)
- [ ] Lesson.java has 7 new fields
- [ ] 7 new model classes created
- [ ] 4 new API methods in ApiService.java
- [ ] ModuleLadderActivity fetches 13-node structure from API
- [ ] Ladder displays 12 lessons + 1 assessment
- [ ] Game badges visible on nodes (fix game routing bug)
- [ ] Branching nodes appear (intervention/enrichment)

### End of Day 3 (Integration)
- [ ] ModuleLessonActivity redesigned (Content → Quiz → Game)
- [ ] InterventionActivity working
- [ ] EnrichmentActivity working
- [ ] Complete adaptive flow functional:
  - Student scores < 60% → Intervention unlocks
  - Student scores ≥ 85% → Enrichment unlocks
  - Student completes intervention → Can retry quiz
  - Student completes game → Lesson marked complete
- [ ] Progress saves to database
- [ ] XP and badges awarded correctly

---

## 📊 App Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     Android Application                     │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌─────────────────┐    ┌────────────────────────────────┐  │
│  │   Activities    │    │        Models (POJOs)          │  │
│  │                 │    │                                 │  │
│  │ • Dashboard     │    │ • Lesson (⚠️ needs update)     │  │
│  │ • ModuleLadder  │◄───┤ • LearningModule               │  │
│  │   (⚠️ 15→13)    │    │ • Module (❌ create new)       │  │
│  │ • ModuleLesson  │    │ • LessonBranch (❌ new)        │  │
│  │   (⚠️ redesign) │    │ • BranchingDecision (❌ new)   │  │
│  │ • Intervention  │    │ • Question                      │  │
│  │   (❌ new)      │    │ • Students                      │  │
│  │ • Enrichment    │    │ • Badge, GameSession, etc.     │  │
│  │   (❌ new)      │    │                                 │  │
│  │ • Games         │    └────────────────────────────────┘  │
│  └────────┬────────┘                                         │
│           │                                                  │
│           ▼                                                  │
│  ┌─────────────────┐    ┌────────────────────────────────┐  │
│  │   ApiService    │───►│     Retrofit + OkHttp          │  │
│  │                 │    │                                 │  │
│  │ • login()       │    │ • AuthInterceptor (JWT)        │  │
│  │ • getNextQ()    │    │ • JSON Converters              │  │
│  │ • submitAns()   │    │ • Base URL: LiteRiseAPI        │  │
│  │ • getModuleStr()│    │                                 │  │
│  │   (❌ new)      │    └────────────┬───────────────────┘  │
│  │ • updateQuizSc()│                 │                      │
│  │   (❌ new)      │                 ▼                      │
│  └─────────────────┘    ┌────────────────────────────────┐  │
│                         │    PHP API (LiteRiseAPI)       │  │
│                         │                                 │  │
│  ┌─────────────────┐    │ • login.php                    │  │
│  │  Local Storage  │    │ • get_lessons.php              │  │
│  │                 │    │ • submit_answer.php            │  │
│  │ • LessonDB      │    │ • get_module_structure.php     │  │
│  │   (SQLite)      │    │   (❌ create)                  │  │
│  │   - Offline     │    │ • update_quiz_score.php        │  │
│  │     cache       │    │   (❌ create)                  │  │
│  │                 │    │ • get_lesson_branches.php      │  │
│  │ • SessionMgr    │    │   (❌ create)                  │  │
│  │   (SharedPref)  │    │ • complete_branch.php          │  │
│  │   - Student ID  │    │   (❌ create)                  │  │
│  │   - Auth token  │    │                                 │  │
│  └─────────────────┘    └────────────┬───────────────────┘  │
│                                      │                      │
│                                      ▼                      │
│                         ┌────────────────────────────────┐  │
│                         │   MSSQL Database (LiteRiseDB)  │  │
│                         │                                 │  │
│                         │ • Students (CurrentAbility)    │  │
│                         │ • Lessons (⚠️ add 6 columns)   │  │
│                         │ • Modules (❌ create)          │  │
│                         │ • LessonBranches (❌ create)   │  │
│                         │ • StudentBranches (❌ create)  │  │
│                         │ • StudentProgress              │  │
│                         │ • GameResults                  │  │
│                         │ • IRT: Items, Responses        │  │
│                         └────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

---

**Generated:** 2026-01-18
**Status:** Ready for 2-3 day implementation
**Next Step:** Day 1 - Database migration + PHP API endpoints
