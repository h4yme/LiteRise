# LiteRise System Documentation - Part 3
## Tutorial Guide for React Native + MySQL Rebuild

**For:** Developers rebuilding LiteRise from scratch
**Timeline:** 2 weeks (14 days)
**Stack:** React Native + MySQL + REST API
**Target:** Filipino Grade 3 students learning English

---

## Table of Contents

1. [Quick Start: What is LiteRise?](#quick-start-what-is-literise)
2. [The IRT Matrix: Simple Tutorial](#the-irt-matrix-simple-tutorial)
3. [2-Week Implementation Timeline](#2-week-implementation-timeline)
4. [Module Priorities](#module-priorities)
5. [React Native + MySQL Setup Guide](#react-native--mysql-setup-guide)
6. [Quick Reference Checklist](#quick-reference-checklist)

---

## Quick Start: What is LiteRise?

### The Big Picture

LiteRise is an **adaptive English learning app** for Filipino Grade 3 students. Think of it like a smart tutor that:

1. **Tests the student** to find their level (Placement Test)
2. **Teaches them** at their exact level (Adaptive Lessons)
3. **Makes learning fun** with games (Gamification)
4. **Checks understanding** with quizzes (Assessment)
5. **Adjusts automatically** based on performance (IRT System)

### The Magic: How It Works

```
Student logs in
    ↓
Takes 25-question Placement Test (IRT calculates ability: θ)
    ↓
Gets placed in: Beginner / Intermediate / Advanced
    ↓
Learns with 3-Phase Flow: LESSON → GAME → QUIZ
    ↓
System adjusts difficulty based on quiz scores
    ↓
Student progresses through 5 modules, 13 nodes each
```

### Key Numbers to Remember

- **25 questions** in Placement Test (5 per category)
- **5 modules** total (Grammar, Vocabulary, Reading, Writing, Listening)
- **13 nodes** per module (12 lessons + 1 final assessment)
- **3 phases** per lesson (Lesson → Game → Quiz)
- **3 lives** in games
- **-3 to +3** theta range (student ability)
- **0.3** learning rate for theta updates
- **70%** passing score for quizzes

---

## The IRT Matrix: Simple Tutorial

### What is IRT? (Explained Like You're 10)

**IRT = Item Response Theory**

Imagine you're a teacher trying to find out how good a student is at English. You could:

❌ **Bad way:** Give everyone the same test → some kids find it too easy, some too hard
✅ **Good way:** Give different questions based on how they're doing → everyone gets the "just right" challenge

**That's IRT!** It's a smart math system that:
1. Figures out the student's ability level (θ, pronounced "theta")
2. Picks questions that are perfect for their level
3. Updates their ability score based on right/wrong answers

---

### The IRT Formula (Don't Panic Edition)

Here's the main formula. It looks scary, but I'll break it down:

```
P(θ) = c + (1 - c) / (1 + e^(-a(θ - b)))
```

**What this means in plain English:**

> "What's the probability that a student with ability θ will answer this question correctly?"

**The variables:**

| Variable | Name | What It Means | Range |
|----------|------|---------------|-------|
| **θ** (theta) | Student ability | How good the student is | -3 to +3 |
| **a** | Discrimination | How well the question separates good/bad students | 0.5 to 2.5 |
| **b** | Difficulty | How hard the question is | -3 to +3 |
| **c** | Guessing | Chance of random correct guess (¼ for 4-choice) | 0.25 |

---

### Step-by-Step Example: Understanding Theta

Let's follow a student named **Maria** through the placement test:

#### Starting Point
- Maria starts at θ = 0.0 (neutral, we don't know her level yet)

#### Question 1: Easy Grammar Question
- **Difficulty (b):** -1.0 (easy)
- **Discrimination (a):** 1.5 (good at separating students)
- **Guessing (c):** 0.25 (4 choices)

**Calculate probability:**
```
P(0) = 0.25 + (1 - 0.25) / (1 + e^(-1.5(0 - (-1))))
     = 0.25 + 0.75 / (1 + e^(-1.5))
     = 0.25 + 0.75 / (1 + 0.223)
     = 0.25 + 0.75 / 1.223
     = 0.25 + 0.613
     = 0.863 (86.3% chance of getting it right)
```

**Maria gets it CORRECT ✓**

Now we update her theta:

```
Δθ = α × a × (y - P(θ))
   = 0.3 × 1.5 × (1 - 0.863)
   = 0.3 × 1.5 × 0.137
   = 0.062

θ_new = 0.0 + 0.062 = 0.062
```

Maria's new ability: **θ = 0.062** (slightly above neutral)

#### Question 2: Medium Vocabulary Question
- **Difficulty (b):** 0.5 (medium-hard)
- **Discrimination (a):** 2.0 (very good question)
- **Current θ:** 0.062

**Calculate probability:**
```
P(0.062) = 0.25 + 0.75 / (1 + e^(-2.0(0.062 - 0.5)))
         = 0.25 + 0.75 / (1 + e^(0.876))
         = 0.25 + 0.75 / (1 + 2.402)
         = 0.25 + 0.75 / 3.402
         = 0.25 + 0.220
         = 0.470 (47% chance)
```

**Maria gets it WRONG ✗**

Update theta:

```
Δθ = 0.3 × 2.0 × (0 - 0.470)
   = 0.6 × (-0.470)
   = -0.282

θ_new = 0.062 + (-0.282) = -0.220
```

Maria's new ability: **θ = -0.220** (slightly below neutral)

#### After 25 Questions...

Let's say Maria ends with **θ = -0.8**

**Placement Decision:**
- θ < -0.5 → **Beginner**
- -0.5 ≤ θ < 0.5 → Intermediate
- θ ≥ 0.5 → Advanced

Maria gets placed in **Beginner** level.

---

### The IRT Matrix: Visual Guide

Here's how the system picks questions:

```
┌─────────────────────────────────────────────────────┐
│         IRT Question Selection Process              │
└─────────────────────────────────────────────────────┘

Step 1: Student starts at θ = 0.0
        │
        ├─→ System has 250 questions in database
        │   Each has: a (discrimination), b (difficulty), c (guessing)
        │
Step 2: Calculate "Fisher Information" for each question
        │
        │   Fisher Information = a² × P(θ) × (1 - P(θ))
        │
        │   This tells us: "How much will this question teach us
        │   about the student's ability?"
        │
        ├─→ High Fisher Info = Good question for this student
        │
Step 3: Pick question with HIGHEST Fisher Information
        │   that student hasn't seen yet
        │
Step 4: Student answers → Update θ
        │
        └─→ Repeat until 25 questions done
```

---

### Fisher Information Example

Why do we use Fisher Information? Let me show you:

**Maria's current θ = -0.5** (beginner level)

We have 3 questions to choose from:

| Question | Difficulty (b) | Discrimination (a) | Fisher Info |
|----------|----------------|-------------------|-------------|
| A | -2.0 (very easy) | 1.5 | 0.45 |
| B | -0.5 (perfect match) | 2.0 | **4.00** ← Best! |
| C | +1.5 (too hard) | 1.8 | 0.23 |

**Why B wins:**
- Question A is too easy → Maria will probably get it right, we learn nothing
- Question C is too hard → Maria will probably get it wrong, we learn nothing
- Question B is just right → 50/50 chance, we learn the most!

**Formula:**
```
Fisher Info (B) = 2.0² × P(-0.5) × (1 - P(-0.5))
                = 4.0 × 0.5 × 0.5
                = 1.0 (normalized value)
```

---

### The Complete IRT Workflow

Here's the full cycle:

```
┌──────────────────────────────────────────────────────┐
│  1. PLACEMENT TEST (25 Questions)                    │
├──────────────────────────────────────────────────────┤
│  Start: θ = 0.0                                      │
│                                                      │
│  For each question (1 to 25):                        │
│    a) Calculate Fisher Info for all unused questions │
│    b) Pick question with max Fisher Info             │
│    c) Student answers                                │
│    d) Update θ using gradient ascent                 │
│                                                      │
│  End: θ = final ability score                        │
│                                                      │
│  Place in level:                                     │
│    • θ < -0.5 → Beginner                            │
│    • -0.5 ≤ θ < 0.5 → Intermediate                  │
│    • θ ≥ 0.5 → Advanced                             │
└──────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────┐
│  2. ADAPTIVE LEARNING (Lessons & Quizzes)            │
├──────────────────────────────────────────────────────┤
│  Student completes: LESSON → GAME → QUIZ             │
│                                                      │
│  Quiz Result:                                        │
│    • Score ≥ 70% → Next lesson (normal progression)  │
│    • Score < 70% → Intervention lesson (easier)      │
│                                                      │
│  Adaptive Branching:                                 │
│    1. Check quiz score                               │
│    2. If struggling → Load easier content            │
│    3. If mastering → Keep normal difficulty          │
│    4. Track progress in database                     │
└──────────────────────────────────────────────────────┘
```

---

### Real-World IRT Example: Full Scenario

Let's see the complete journey of **Pedro**, a Grade 3 student:

#### Day 1: Placement Test

Pedro opens the app and takes the placement test.

**Questions 1-5 (Grammar Category):**

| Q# | Difficulty | Pedro's θ before | Answer | θ after | Explanation |
|----|-----------|------------------|--------|---------|-------------|
| 1 | 0.0 | 0.000 | ✓ Correct | +0.180 | Easy start, θ goes up |
| 2 | 0.5 | 0.180 | ✓ Correct | +0.420 | Getting harder, still correct |
| 3 | 1.0 | 0.420 | ✗ Wrong | +0.250 | Too hard, θ drops |
| 4 | 0.3 | 0.250 | ✓ Correct | +0.380 | Back to medium, correct |
| 5 | 0.6 | 0.380 | ✗ Wrong | +0.200 | Struggling with harder ones |

**Questions 6-10 (Vocabulary):** θ fluctuates between 0.1 and 0.3
**Questions 11-15 (Reading):** θ stays around 0.2
**Questions 16-20 (Writing):** θ drops to -0.1
**Questions 21-25 (Listening):** θ ends at **+0.15**

**Final Placement:** θ = 0.15 → **Intermediate Level**

---

#### Week 1: Module 1 - Grammar Fundamentals

Pedro starts Node 1: "Nouns and Pronouns"

**LESSON Phase:**
- System loads content tagged "intermediate"
- Pedro learns about common vs. proper nouns
- Interactive examples with Filipino context

**GAME Phase:**
- Plays "Phonics Ninja" - slicing words to identify nouns
- Scores 850 points (good performance)

**QUIZ Phase:**
- 10 questions about nouns/pronouns
- Pedro scores **8/10 = 80%**

**System Decision:**
- 80% ≥ 70% threshold → **PASS**
- Next lesson: Node 2 "Action Words" (normal difficulty)
- No intervention needed

---

#### Week 2: Module 1, Node 5 - Sentence Structure

**LESSON Phase:**
- Intermediate content on subject-verb-object

**GAME Phase:**
- "Synonym Sprint" - running game collecting sentence parts

**QUIZ Phase:**
- 10 questions about sentence structure
- Pedro scores **5/10 = 50%**

**System Decision:**
- 50% < 70% threshold → **INTERVENTION NEEDED**
- System loads: Node 5-A "Sentence Structure - Review"
- Content difficulty: Beginner level (one step down)
- Pedro retakes concept with simpler examples

**After Intervention:**
- Pedro retakes quiz
- Scores **8/10 = 80%** → PASS
- Continues to Node 6 (normal difficulty restored)

---

### Database Tables for IRT (MySQL Schema)

Here's what you need to store:

#### Table: `QuestionBank`

| Column | Type | Description |
|--------|------|-------------|
| QuestionID | INT PRIMARY KEY | Unique ID |
| QuestionText | TEXT | "What is the plural of 'child'?" |
| Category | VARCHAR(50) | Grammar, Vocabulary, Reading, Writing, Listening |
| Difficulty_b | DECIMAL(5,2) | -3.00 to +3.00 |
| Discrimination_a | DECIMAL(5,2) | 0.50 to 2.50 |
| Guessing_c | DECIMAL(3,2) | Usually 0.25 |
| OptionA | VARCHAR(200) | "childs" |
| OptionB | VARCHAR(200) | "children" (correct) |
| OptionC | VARCHAR(200) | "childes" |
| OptionD | VARCHAR(200) | "child" |
| CorrectAnswer | CHAR(1) | 'B' |

#### Table: `PlacementTest`

| Column | Type | Description |
|--------|------|-------------|
| TestID | INT PRIMARY KEY | Unique test instance |
| StudentID | INT | Foreign key |
| StartTheta | DECIMAL(5,2) | Always 0.00 |
| FinalTheta | DECIMAL(5,2) | After 25 questions |
| PlacementLevel | VARCHAR(20) | Beginner/Intermediate/Advanced |
| DateTaken | DATETIME | When test was completed |
| QuestionsJSON | JSON | Array of 25 question IDs asked |

#### Table: `PlacementAnswers`

| Column | Type | Description |
|--------|------|-------------|
| AnswerID | INT PRIMARY KEY | Unique answer |
| TestID | INT | Foreign key |
| QuestionID | INT | Which question |
| StudentAnswer | CHAR(1) | 'A', 'B', 'C', or 'D' |
| IsCorrect | BOOLEAN | 1 or 0 |
| ThetaBefore | DECIMAL(5,2) | θ before this question |
| ThetaAfter | DECIMAL(5,2) | θ after this question |
| FisherInfo | DECIMAL(8,4) | Why this question was chosen |
| Timestamp | DATETIME | When answered |

---

## 2-Week Implementation Timeline

### Overview: Critical Path

```
Week 1: Foundation + IRT System
Week 2: Learning Flow + Testing
```

---

### Week 1 Breakdown

#### **Day 1-2: Database & Authentication** ⭐⭐⭐ (Priority 1)

**Tasks:**
- [ ] Set up MySQL database on server
- [ ] Create all tables (see MySQL schema below)
- [ ] Seed QuestionBank with 250 questions
  - 50 questions per category
  - Mix of difficulties: -2, -1, 0, +1, +2
  - Set a, b, c parameters for each
- [ ] Create REST API endpoints:
  - `POST /api/auth/register` - New student signup
  - `POST /api/auth/login` - Student login
  - `GET /api/auth/session` - Check session
- [ ] Test with Postman/Insomnia

**Deliverable:** Working login system + populated database

**Testing:**
```bash
# Test registration
curl -X POST http://yourapi.com/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Student","age":8,"school":"Manila Elementary"}'

# Should return: { "studentId": 1, "token": "..." }
```

---

#### **Day 3-4: IRT Engine** ⭐⭐⭐ (Priority 1)

**Tasks:**
- [ ] Implement IRT calculation functions:
  - `calculateProbability(theta, a, b, c)` → P(θ)
  - `updateTheta(currentTheta, a, b, studentAnswer, expectedProb)` → new θ
  - `calculateFisherInfo(theta, a, b, c)` → Fisher Information
  - `selectNextQuestion(theta, usedQuestionIds, category)` → best question
- [ ] Create API endpoint:
  - `POST /api/placement/start` - Initialize test (θ = 0)
  - `GET /api/placement/next-question` - Get adaptive question
  - `POST /api/placement/submit-answer` - Submit answer, update θ
  - `POST /api/placement/complete` - Finalize test, determine level
- [ ] Test IRT calculations with known values

**Deliverable:** Working IRT engine with API

**Testing:**
```javascript
// Test IRT calculation
const theta = 0.5;
const a = 1.5;
const b = 0.3;
const c = 0.25;

const prob = calculateProbability(theta, a, b, c);
console.log(prob); // Should be ~0.62

const newTheta = updateTheta(theta, a, b, 1, prob); // Correct answer
console.log(newTheta); // Should be ~0.62 (increased)
```

---

#### **Day 5: React Native App Setup** ⭐⭐⭐ (Priority 1)

**Tasks:**
- [ ] Initialize React Native project: `npx react-native init LiteRise`
- [ ] Install dependencies:
  ```bash
  npm install @react-navigation/native @react-navigation/stack
  npm install axios
  npm install @react-native-async-storage/async-storage
  npm install react-native-gesture-handler react-native-reanimated
  ```
- [ ] Set up navigation structure:
  - SplashScreen
  - LoginScreen
  - RegisterScreen
  - PlacementTestScreen
  - DashboardScreen
- [ ] Create API service layer: `services/api.js`
- [ ] Create auth context: `contexts/AuthContext.js`

**Deliverable:** App skeleton with navigation

---

#### **Day 6-7: Placement Test UI** ⭐⭐⭐ (Priority 1)

**Tasks:**
- [ ] Build PlacementTestScreen component
- [ ] Features:
  - Question counter (1/25)
  - Progress bar
  - Question text display
  - 4 option buttons (A, B, C, D)
  - Next button
  - Timer (optional)
- [ ] Connect to IRT API
- [ ] Handle 25-question flow
- [ ] Show results screen with placement level
- [ ] Save result to AsyncStorage

**Deliverable:** Fully functional placement test

**Screen Flow:**
```
PlacementIntroScreen
   ↓ (Start Test)
PlacementQuestionScreen (repeat 25 times)
   ↓ (Submit last answer)
PlacementResultScreen
   ↓ (Continue)
DashboardScreen
```

---

### Week 2 Breakdown

#### **Day 8-9: Learning Content System** ⭐⭐ (Priority 2)

**Tasks:**
- [ ] Create `ModuleContent` table in MySQL
- [ ] Seed with 5 modules × 13 nodes = 65 content entries
- [ ] Create API endpoints:
  - `GET /api/modules` - Get all 5 modules
  - `GET /api/modules/:id/nodes` - Get 13 nodes for module
  - `GET /api/lessons/:nodeId/content` - Get lesson content (adaptive by level)
  - `POST /api/quiz/submit` - Submit quiz, check score, handle branching
- [ ] Build LessonScreen component
- [ ] Build QuizScreen component

**Deliverable:** Students can view lessons and take quizzes

---

#### **Day 10: Adaptive Branching Logic** ⭐⭐ (Priority 2)

**Tasks:**
- [ ] Implement intervention system:
  - Check quiz score
  - If < 70%: Load easier content (level - 1)
  - If ≥ 70%: Continue normal progression
- [ ] Create `StudentProgress` tracking:
  - Current module, current node
  - Last quiz score
  - Intervention count
- [ ] Update navigation to handle:
  - Normal flow: Node X → Node X+1
  - Intervention flow: Node X → Node X-Intervention → Node X (retry) → Node X+1

**Deliverable:** Working adaptive difficulty system

---

#### **Day 11: Game Integration** ⭐ (Priority 3)

**Tasks:**
- [ ] Build ONE game (Phonics Ninja)
  - Touch/swipe mechanics
  - 45-second timer
  - Score tracking
  - Lives system (3 hearts)
- [ ] Integrate into learning flow:
  - Lesson → Game → Quiz
  - Pass node_id to game
  - Return to quiz after game ends
- [ ] API endpoint: `POST /api/games/score` - Save game score

**Deliverable:** One playable game in the flow

**Note:** Other games (Synonym Sprint, Word Explosion) can be added post-launch

---

#### **Day 12: Dashboard & Progress Tracking** ⭐⭐ (Priority 2)

**Tasks:**
- [ ] Build DashboardScreen:
  - Welcome message with student name
  - 5 module cards
  - Progress indicators (X/13 nodes completed)
  - Current level badge (Beginner/Intermediate/Advanced)
  - XP and streak counters
- [ ] Create `Gamification` table:
  - TotalXP
  - CurrentStreak
  - Badges earned
- [ ] Show locked/unlocked modules
- [ ] Add settings button (logout, profile)

**Deliverable:** Functional dashboard showing progress

---

#### **Day 13: Testing & Bug Fixes** ⭐⭐⭐ (Priority 1)

**Tasks:**
- [ ] End-to-end testing:
  1. Register new student
  2. Complete placement test (25 questions)
  3. Verify correct placement level
  4. Complete 1 full lesson-game-quiz cycle
  5. Test intervention (fail quiz intentionally)
  6. Check progress saves correctly
- [ ] Fix critical bugs
- [ ] Test on both iOS and Android
- [ ] Performance optimization (API response times)

**Deliverable:** Stable, tested app

---

#### **Day 14: Polish & Deployment** ⭐ (Priority 3)

**Tasks:**
- [ ] UI polish:
  - Add loading spinners
  - Add error messages
  - Add success animations
  - Check all colors/fonts
- [ ] Deploy API to production server
- [ ] Build Android APK
- [ ] Build iOS IPA (if applicable)
- [ ] Create user documentation
- [ ] Final testing on production

**Deliverable:** Production-ready app

---

## Module Priorities

### Priority 1: Must-Have (Core System) ⭐⭐⭐

**Cannot launch without these:**

1. **Authentication**
   - Student registration
   - Student login
   - Session management

2. **IRT Placement Test**
   - 25 adaptive questions
   - Theta calculation
   - Fisher Information question selection
   - Placement level determination (Beginner/Intermediate/Advanced)

3. **Database**
   - QuestionBank with 250 questions
   - Student profiles
   - Placement test results
   - Progress tracking

4. **Basic Learning Flow**
   - View lesson content
   - Take quiz
   - Progress to next lesson

5. **API Layer**
   - All critical endpoints working
   - Error handling
   - Authentication middleware

**Estimated Time:** 8 days (Days 1-6, Day 13)

---

### Priority 2: Important (Adaptive System) ⭐⭐

**Needed for full functionality:**

1. **Adaptive Branching**
   - Quiz score checking
   - Intervention lessons for struggling students
   - Difficulty adjustment

2. **5 Modules × 13 Nodes**
   - All content loaded
   - Module ladder UI
   - Node progression tracking

3. **Dashboard**
   - Progress visualization
   - Module cards
   - Stats display

4. **Content Management**
   - Lesson content for all 3 levels (Beginner/Intermediate/Advanced)
   - Quiz questions for all nodes
   - Adaptive content selection

**Estimated Time:** 4 days (Days 8-10, Day 12)

---

### Priority 3: Nice-to-Have (Enhanced Experience) ⭐

**Can be added post-launch:**

1. **Games**
   - Phonics Ninja (basic version for Day 11)
   - Synonym Sprint (post-launch)
   - Word Explosion (post-launch)
   - Other revolutionary games (post-launch)

2. **Gamification**
   - XP system
   - Badges
   - Streaks
   - Leaderboards

3. **Advanced Features**
   - Speech recognition (Azure Speech API)
   - Pronunciation scoring
   - Parent dashboard
   - Teacher portal

4. **Polish**
   - Animations
   - Sound effects
   - Haptic feedback
   - Dark mode

**Estimated Time:** 2 days (Day 11, Day 14) + post-launch work

---

### Priority 4: Future Enhancements 🚀

**After successful launch:**

1. Analytics dashboard
2. A/B testing for content
3. Social features (friend challenges)
4. Offline mode
5. Multiple language support
6. Accessibility features
7. Admin panel for content management

---

## React Native + MySQL Setup Guide

### MySQL Database Schema (Simplified)

Here's the complete schema for your backend:

```sql
-- ============================================
-- STUDENTS & AUTHENTICATION
-- ============================================

CREATE TABLE Students (
    StudentID INT PRIMARY KEY AUTO_INCREMENT,
    Name VARCHAR(100) NOT NULL,
    Age INT,
    School VARCHAR(200),
    PasswordHash VARCHAR(255), -- Use bcrypt
    CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    LastLogin DATETIME
);

CREATE INDEX idx_student_name ON Students(Name);

-- ============================================
-- PLACEMENT TEST SYSTEM
-- ============================================

CREATE TABLE QuestionBank (
    QuestionID INT PRIMARY KEY AUTO_INCREMENT,
    QuestionText TEXT NOT NULL,
    Category VARCHAR(50), -- Grammar, Vocabulary, Reading, Writing, Listening
    Difficulty_b DECIMAL(5,2), -- -3.00 to +3.00
    Discrimination_a DECIMAL(5,2), -- 0.50 to 2.50
    Guessing_c DECIMAL(3,2) DEFAULT 0.25,
    OptionA VARCHAR(200),
    OptionB VARCHAR(200),
    OptionC VARCHAR(200),
    OptionD VARCHAR(200),
    CorrectAnswer CHAR(1), -- 'A', 'B', 'C', or 'D'
    ExplanationText TEXT,
    CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_category ON QuestionBank(Category);
CREATE INDEX idx_difficulty ON QuestionBank(Difficulty_b);

CREATE TABLE PlacementTests (
    TestID INT PRIMARY KEY AUTO_INCREMENT,
    StudentID INT,
    StartTheta DECIMAL(5,2) DEFAULT 0.00,
    FinalTheta DECIMAL(5,2),
    PlacementLevel VARCHAR(20), -- 'Beginner', 'Intermediate', 'Advanced'
    QuestionsAsked JSON, -- Array of QuestionIDs
    DateTaken DATETIME DEFAULT CURRENT_TIMESTAMP,
    CompletedAt DATETIME,
    FOREIGN KEY (StudentID) REFERENCES Students(StudentID)
);

CREATE TABLE PlacementAnswers (
    AnswerID INT PRIMARY KEY AUTO_INCREMENT,
    TestID INT,
    QuestionID INT,
    StudentAnswer CHAR(1),
    IsCorrect BOOLEAN,
    ThetaBefore DECIMAL(5,2),
    ThetaAfter DECIMAL(5,2),
    FisherInformation DECIMAL(8,4),
    AnsweredAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (TestID) REFERENCES PlacementTests(TestID),
    FOREIGN KEY (QuestionID) REFERENCES QuestionBank(QuestionID)
);

-- ============================================
-- LEARNING CONTENT
-- ============================================

CREATE TABLE Modules (
    ModuleID INT PRIMARY KEY AUTO_INCREMENT,
    ModuleName VARCHAR(100), -- 'Grammar Fundamentals', 'Vocabulary Building', etc.
    Description TEXT,
    IconURL VARCHAR(255),
    SortOrder INT -- 1 to 5
);

CREATE TABLE Nodes (
    NodeID INT PRIMARY KEY AUTO_INCREMENT,
    ModuleID INT,
    NodeNumber INT, -- 1 to 13 (12 lessons + 1 final)
    Title VARCHAR(200),
    LearningObjectives TEXT,
    IsFinalAssessment BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (ModuleID) REFERENCES Modules(ModuleID)
);

CREATE TABLE LessonContent (
    ContentID INT PRIMARY KEY AUTO_INCREMENT,
    NodeID INT,
    DifficultyLevel VARCHAR(20), -- 'Beginner', 'Intermediate', 'Advanced'
    ContentJSON JSON, -- Full lesson content
    CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (NodeID) REFERENCES Nodes(NodeID)
);

CREATE INDEX idx_node_difficulty ON LessonContent(NodeID, DifficultyLevel);

CREATE TABLE QuizQuestions (
    QuizQuestionID INT PRIMARY KEY AUTO_INCREMENT,
    NodeID INT,
    QuestionText TEXT,
    OptionA VARCHAR(200),
    OptionB VARCHAR(200),
    OptionC VARCHAR(200),
    OptionD VARCHAR(200),
    CorrectAnswer CHAR(1),
    Explanation TEXT,
    FOREIGN KEY (NodeID) REFERENCES Nodes(NodeID)
);

-- ============================================
-- STUDENT PROGRESS TRACKING
-- ============================================

CREATE TABLE StudentProgress (
    ProgressID INT PRIMARY KEY AUTO_INCREMENT,
    StudentID INT,
    NodeID INT,
    Status VARCHAR(20), -- 'not_started', 'in_progress', 'completed'
    LatestQuizScore DECIMAL(5,2), -- Percentage (0.00 to 100.00)
    CompletedAt DATETIME,
    NeedsIntervention BOOLEAN DEFAULT FALSE,
    InterventionCompleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (StudentID) REFERENCES Students(StudentID),
    FOREIGN KEY (NodeID) REFERENCES Nodes(NodeID)
);

CREATE INDEX idx_student_progress ON StudentProgress(StudentID, NodeID);

CREATE TABLE QuizAttempts (
    AttemptID INT PRIMARY KEY AUTO_INCREMENT,
    StudentID INT,
    NodeID INT,
    Score DECIMAL(5,2),
    TotalQuestions INT,
    CorrectAnswers INT,
    AnswersJSON JSON, -- Array of {questionId, answer, isCorrect}
    AttemptedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (StudentID) REFERENCES Students(StudentID),
    FOREIGN KEY (NodeID) REFERENCES Nodes(NodeID)
);

-- ============================================
-- GAMIFICATION
-- ============================================

CREATE TABLE StudentGamification (
    GamificationID INT PRIMARY KEY AUTO_INCREMENT,
    StudentID INT UNIQUE,
    TotalXP INT DEFAULT 0,
    CurrentStreak INT DEFAULT 0,
    LongestStreak INT DEFAULT 0,
    LastActivityDate DATE,
    FOREIGN KEY (StudentID) REFERENCES Students(StudentID)
);

CREATE TABLE Badges (
    BadgeID INT PRIMARY KEY AUTO_INCREMENT,
    BadgeName VARCHAR(100),
    Description TEXT,
    IconURL VARCHAR(255),
    RequirementType VARCHAR(50), -- 'xp_milestone', 'streak', 'module_complete', etc.
    RequirementValue INT
);

CREATE TABLE StudentBadges (
    StudentBadgeID INT PRIMARY KEY AUTO_INCREMENT,
    StudentID INT,
    BadgeID INT,
    EarnedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (StudentID) REFERENCES Students(StudentID),
    FOREIGN KEY (BadgeID) REFERENCES Badges(BadgeID)
);

-- ============================================
-- GAMES
-- ============================================

CREATE TABLE GameScores (
    ScoreID INT PRIMARY KEY AUTO_INCREMENT,
    StudentID INT,
    GameName VARCHAR(50), -- 'PhonicsNinja', 'SynonymSprint', etc.
    NodeID INT, -- Which lesson this game was played for
    Score INT,
    Duration INT, -- Seconds played
    PlayedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (StudentID) REFERENCES Students(StudentID),
    FOREIGN KEY (NodeID) REFERENCES Nodes(NodeID)
);
```

---

### React Native Project Structure

```
LiteRise/
├── src/
│   ├── screens/
│   │   ├── auth/
│   │   │   ├── SplashScreen.js
│   │   │   ├── LoginScreen.js
│   │   │   └── RegisterScreen.js
│   │   ├── placement/
│   │   │   ├── PlacementIntroScreen.js
│   │   │   ├── PlacementQuestionScreen.js
│   │   │   └── PlacementResultScreen.js
│   │   ├── learning/
│   │   │   ├── DashboardScreen.js
│   │   │   ├── ModuleLadderScreen.js
│   │   │   ├── LessonScreen.js
│   │   │   └── QuizScreen.js
│   │   └── games/
│   │       ├── PhonicsNinjaScreen.js
│   │       ├── SynonymSprintScreen.js
│   │       └── WordExplosionScreen.js
│   ├── components/
│   │   ├── common/
│   │   │   ├── Button.js
│   │   │   ├── Card.js
│   │   │   ├── ProgressBar.js
│   │   │   └── LoadingSpinner.js
│   │   ├── quiz/
│   │   │   ├── QuestionCard.js
│   │   │   └── OptionButton.js
│   │   └── dashboard/
│   │       ├── ModuleCard.js
│   │       ├── StatsWidget.js
│   │       └── ProgressChart.js
│   ├── services/
│   │   ├── api.js              # Axios instance
│   │   ├── authService.js      # Login, register
│   │   ├── placementService.js # IRT test API calls
│   │   ├── learningService.js  # Lessons, quizzes
│   │   └── irtEngine.js        # Client-side IRT calculations
│   ├── contexts/
│   │   ├── AuthContext.js      # User authentication state
│   │   └── ProgressContext.js  # Student progress state
│   ├── navigation/
│   │   ├── AppNavigator.js     # Root navigator
│   │   ├── AuthStack.js        # Login/Register
│   │   └── MainStack.js        # Dashboard/Learning
│   ├── utils/
│   │   ├── constants.js        # API URLs, colors, etc.
│   │   ├── storage.js          # AsyncStorage helpers
│   │   └── validation.js       # Form validation
│   └── styles/
│       ├── colors.js
│       ├── typography.js
│       └── globalStyles.js
├── App.js
├── package.json
└── README.md
```

---

### Key Code Examples

#### 1. IRT Engine (Client-Side)

**File:** `src/services/irtEngine.js`

```javascript
// IRT calculation functions for React Native

/**
 * Calculate probability of correct answer using 3PL model
 * @param {number} theta - Student ability (-3 to 3)
 * @param {number} a - Item discrimination (0.5 to 2.5)
 * @param {number} b - Item difficulty (-3 to 3)
 * @param {number} c - Guessing parameter (usually 0.25)
 * @returns {number} Probability (0 to 1)
 */
export const calculateProbability = (theta, a, b, c = 0.25) => {
  const exponent = -a * (theta - b);
  const probability = c + ((1 - c) / (1 + Math.exp(exponent)));
  return probability;
};

/**
 * Update theta using gradient ascent
 * @param {number} currentTheta - Current ability estimate
 * @param {number} a - Item discrimination
 * @param {number} b - Item difficulty
 * @param {number} studentAnswer - 1 if correct, 0 if incorrect
 * @param {number} expectedProb - P(θ) from calculateProbability
 * @param {number} learningRate - Alpha (default 0.3)
 * @returns {number} New theta value
 */
export const updateTheta = (
  currentTheta,
  a,
  b,
  studentAnswer,
  expectedProb,
  learningRate = 0.3
) => {
  const delta = learningRate * a * (studentAnswer - expectedProb);
  let newTheta = currentTheta + delta;

  // Clamp between -3 and 3
  newTheta = Math.max(-3, Math.min(3, newTheta));

  return newTheta;
};

/**
 * Calculate Fisher Information
 * @param {number} theta - Student ability
 * @param {number} a - Item discrimination
 * @param {number} b - Item difficulty
 * @param {number} c - Guessing parameter
 * @returns {number} Fisher Information value
 */
export const calculateFisherInfo = (theta, a, b, c = 0.25) => {
  const prob = calculateProbability(theta, a, b, c);
  const q = 1 - prob;

  // Fisher Information formula
  const fisherInfo = (a * a * q * (prob - c) * (prob - c)) / (prob * (1 - c) * (1 - c));

  return fisherInfo;
};

/**
 * Select next question with maximum Fisher Information
 * @param {number} theta - Current student ability
 * @param {Array} questionBank - Array of available questions
 * @param {Array} usedQuestionIds - IDs of already asked questions
 * @returns {Object} Best question to ask next
 */
export const selectNextQuestion = (theta, questionBank, usedQuestionIds = []) => {
  let maxFisherInfo = -1;
  let bestQuestion = null;

  for (const question of questionBank) {
    // Skip already used questions
    if (usedQuestionIds.includes(question.QuestionID)) {
      continue;
    }

    const fisherInfo = calculateFisherInfo(
      theta,
      question.Discrimination_a,
      question.Difficulty_b,
      question.Guessing_c
    );

    if (fisherInfo > maxFisherInfo) {
      maxFisherInfo = fisherInfo;
      bestQuestion = { ...question, fisherInfo };
    }
  }

  return bestQuestion;
};

/**
 * Determine placement level from final theta
 * @param {number} finalTheta - Final ability estimate
 * @returns {string} 'Beginner', 'Intermediate', or 'Advanced'
 */
export const determinePlacementLevel = (finalTheta) => {
  if (finalTheta < -0.5) {
    return 'Beginner';
  } else if (finalTheta < 0.5) {
    return 'Intermediate';
  } else {
    return 'Advanced';
  }
};
```

---

#### 2. Placement Test API Service

**File:** `src/services/placementService.js`

```javascript
import api from './api';
import {
  calculateProbability,
  updateTheta,
  selectNextQuestion,
  determinePlacementLevel
} from './irtEngine';

/**
 * Start a new placement test
 * Returns initial test data
 */
export const startPlacementTest = async (studentId) => {
  try {
    const response = await api.post('/placement/start', {
      studentId
    });

    return {
      testId: response.data.testId,
      currentTheta: 0.0,
      questionsAsked: [],
      currentQuestion: 1,
      totalQuestions: 25
    };
  } catch (error) {
    console.error('Error starting placement test:', error);
    throw error;
  }
};

/**
 * Get next adaptive question
 */
export const getNextQuestion = async (testId, currentTheta, questionsAsked, category) => {
  try {
    const response = await api.get('/placement/next-question', {
      params: {
        testId,
        currentTheta,
        questionsAsked: JSON.stringify(questionsAsked),
        category
      }
    });

    return response.data.question;
  } catch (error) {
    console.error('Error getting next question:', error);
    throw error;
  }
};

/**
 * Submit answer and update theta
 */
export const submitAnswer = async (
  testId,
  questionId,
  studentAnswer,
  currentTheta,
  questionParams
) => {
  try {
    // Calculate expected probability
    const expectedProb = calculateProbability(
      currentTheta,
      questionParams.a,
      questionParams.b,
      questionParams.c
    );

    // Determine if correct
    const isCorrect = (studentAnswer === questionParams.correctAnswer) ? 1 : 0;

    // Update theta
    const newTheta = updateTheta(
      currentTheta,
      questionParams.a,
      questionParams.b,
      isCorrect,
      expectedProb
    );

    // Submit to backend
    const response = await api.post('/placement/submit-answer', {
      testId,
      questionId,
      studentAnswer,
      isCorrect,
      thetaBefore: currentTheta,
      thetaAfter: newTheta
    });

    return {
      isCorrect,
      newTheta,
      feedback: response.data.feedback
    };
  } catch (error) {
    console.error('Error submitting answer:', error);
    throw error;
  }
};

/**
 * Complete placement test and get final result
 */
export const completePlacementTest = async (testId, finalTheta) => {
  try {
    const placementLevel = determinePlacementLevel(finalTheta);

    const response = await api.post('/placement/complete', {
      testId,
      finalTheta,
      placementLevel
    });

    return {
      placementLevel,
      finalTheta,
      message: response.data.message
    };
  } catch (error) {
    console.error('Error completing placement test:', error);
    throw error;
  }
};
```

---

#### 3. Placement Test Screen

**File:** `src/screens/placement/PlacementQuestionScreen.js`

```javascript
import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ActivityIndicator } from 'react-native';
import { getNextQuestion, submitAnswer } from '../../services/placementService';
import ProgressBar from '../../components/common/ProgressBar';

const PlacementQuestionScreen = ({ route, navigation }) => {
  const { testId, studentId } = route.params;

  const [currentQuestion, setCurrentQuestion] = useState(1);
  const [theta, setTheta] = useState(0.0);
  const [question, setQuestion] = useState(null);
  const [selectedAnswer, setSelectedAnswer] = useState(null);
  const [loading, setLoading] = useState(true);
  const [questionsAsked, setQuestionsAsked] = useState([]);

  const totalQuestions = 25;
  const categories = ['Grammar', 'Vocabulary', 'Reading', 'Writing', 'Listening'];

  useEffect(() => {
    loadNextQuestion();
  }, [currentQuestion]);

  const loadNextQuestion = async () => {
    try {
      setLoading(true);

      // Determine category (5 questions per category)
      const categoryIndex = Math.floor((currentQuestion - 1) / 5);
      const category = categories[categoryIndex];

      const nextQuestion = await getNextQuestion(
        testId,
        theta,
        questionsAsked,
        category
      );

      setQuestion(nextQuestion);
      setSelectedAnswer(null);
      setLoading(false);
    } catch (error) {
      console.error('Error loading question:', error);
      setLoading(false);
    }
  };

  const handleAnswerSelect = (answer) => {
    setSelectedAnswer(answer);
  };

  const handleNext = async () => {
    if (!selectedAnswer) return;

    try {
      setLoading(true);

      // Submit answer and update theta
      const result = await submitAnswer(
        testId,
        question.QuestionID,
        selectedAnswer,
        theta,
        {
          a: question.Discrimination_a,
          b: question.Difficulty_b,
          c: question.Guessing_c,
          correctAnswer: question.CorrectAnswer
        }
      );

      // Update theta
      setTheta(result.newTheta);

      // Add to asked questions
      setQuestionsAsked([...questionsAsked, question.QuestionID]);

      // Check if test is complete
      if (currentQuestion >= totalQuestions) {
        // Navigate to results
        navigation.navigate('PlacementResult', {
          finalTheta: result.newTheta,
          testId
        });
      } else {
        // Load next question
        setCurrentQuestion(currentQuestion + 1);
      }
    } catch (error) {
      console.error('Error submitting answer:', error);
      setLoading(false);
    }
  };

  if (loading || !question) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color="#6C63FF" />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      {/* Progress */}
      <View style={styles.progressSection}>
        <Text style={styles.questionCounter}>
          Question {currentQuestion} of {totalQuestions}
        </Text>
        <ProgressBar
          progress={currentQuestion / totalQuestions}
          color="#6C63FF"
        />
      </View>

      {/* Question */}
      <View style={styles.questionSection}>
        <Text style={styles.category}>{question.Category}</Text>
        <Text style={styles.questionText}>{question.QuestionText}</Text>
      </View>

      {/* Options */}
      <View style={styles.optionsSection}>
        {['A', 'B', 'C', 'D'].map((option) => (
          <TouchableOpacity
            key={option}
            style={[
              styles.optionButton,
              selectedAnswer === option && styles.optionButtonSelected
            ]}
            onPress={() => handleAnswerSelect(option)}
          >
            <Text style={styles.optionLabel}>{option}</Text>
            <Text style={styles.optionText}>{question[`Option${option}`]}</Text>
          </TouchableOpacity>
        ))}
      </View>

      {/* Next Button */}
      <TouchableOpacity
        style={[
          styles.nextButton,
          !selectedAnswer && styles.nextButtonDisabled
        ]}
        onPress={handleNext}
        disabled={!selectedAnswer}
      >
        <Text style={styles.nextButtonText}>
          {currentQuestion < totalQuestions ? 'Next Question' : 'Complete Test'}
        </Text>
      </TouchableOpacity>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F5F7FA',
    padding: 20,
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  progressSection: {
    marginBottom: 30,
  },
  questionCounter: {
    fontSize: 16,
    fontWeight: '600',
    color: '#333',
    marginBottom: 10,
  },
  questionSection: {
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    padding: 20,
    marginBottom: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  category: {
    fontSize: 14,
    fontWeight: '600',
    color: '#6C63FF',
    marginBottom: 10,
    textTransform: 'uppercase',
  },
  questionText: {
    fontSize: 18,
    fontWeight: '500',
    color: '#333',
    lineHeight: 26,
  },
  optionsSection: {
    flex: 1,
  },
  optionButton: {
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 2,
    borderColor: '#E0E0E0',
  },
  optionButtonSelected: {
    borderColor: '#6C63FF',
    backgroundColor: '#F0EFFF',
  },
  optionLabel: {
    fontSize: 18,
    fontWeight: '700',
    color: '#6C63FF',
    width: 40,
  },
  optionText: {
    fontSize: 16,
    color: '#333',
    flex: 1,
  },
  nextButton: {
    backgroundColor: '#6C63FF',
    borderRadius: 12,
    padding: 16,
    alignItems: 'center',
    marginTop: 20,
  },
  nextButtonDisabled: {
    backgroundColor: '#CCCCCC',
  },
  nextButtonText: {
    fontSize: 18,
    fontWeight: '700',
    color: '#FFFFFF',
  },
});

export default PlacementQuestionScreen;
```

---

#### 4. Backend API Endpoint (Node.js + Express + MySQL)

**File:** `backend/routes/placement.js`

```javascript
const express = require('express');
const router = express.Router();
const mysql = require('mysql2/promise');

// Database connection pool
const pool = mysql.createPool({
  host: 'localhost',
  user: 'literise_user',
  password: 'your_password',
  database: 'literise_db',
  waitForConnections: true,
  connectionLimit: 10,
});

/**
 * Start a new placement test
 * POST /api/placement/start
 */
router.post('/start', async (req, res) => {
  const { studentId } = req.body;

  try {
    const connection = await pool.getConnection();

    // Create new test record
    const [result] = await connection.execute(
      'INSERT INTO PlacementTests (StudentID, StartTheta) VALUES (?, ?)',
      [studentId, 0.0]
    );

    const testId = result.insertId;

    connection.release();

    res.json({
      success: true,
      testId,
      message: 'Placement test started'
    });
  } catch (error) {
    console.error('Error starting test:', error);
    res.status(500).json({ success: false, error: error.message });
  }
});

/**
 * Get next adaptive question
 * GET /api/placement/next-question
 */
router.get('/next-question', async (req, res) => {
  const { testId, currentTheta, questionsAsked, category } = req.query;
  const theta = parseFloat(currentTheta);
  const usedIds = JSON.parse(questionsAsked || '[]');

  try {
    const connection = await pool.getConnection();

    // Get all questions from category not yet asked
    let query = 'SELECT * FROM QuestionBank WHERE Category = ?';
    const params = [category];

    if (usedIds.length > 0) {
      query += ` AND QuestionID NOT IN (${usedIds.join(',')})`;
    }

    const [questions] = await connection.execute(query, params);

    // Calculate Fisher Information for each question
    let maxFisherInfo = -1;
    let bestQuestion = null;

    for (const q of questions) {
      const a = q.Discrimination_a;
      const b = q.Difficulty_b;
      const c = q.Guessing_c;

      // Calculate P(θ)
      const exponent = -a * (theta - b);
      const prob = c + ((1 - c) / (1 + Math.exp(exponent)));
      const qVal = 1 - prob;

      // Fisher Information
      const fisherInfo = (a * a * qVal * Math.pow(prob - c, 2)) /
                         (prob * Math.pow(1 - c, 2));

      if (fisherInfo > maxFisherInfo) {
        maxFisherInfo = fisherInfo;
        bestQuestion = q;
      }
    }

    connection.release();

    res.json({
      success: true,
      question: bestQuestion
    });
  } catch (error) {
    console.error('Error getting next question:', error);
    res.status(500).json({ success: false, error: error.message });
  }
});

/**
 * Submit answer and record result
 * POST /api/placement/submit-answer
 */
router.post('/submit-answer', async (req, res) => {
  const { testId, questionId, studentAnswer, isCorrect, thetaBefore, thetaAfter } = req.body;

  try {
    const connection = await pool.getConnection();

    // Get question to calculate Fisher Info
    const [questions] = await connection.execute(
      'SELECT * FROM QuestionBank WHERE QuestionID = ?',
      [questionId]
    );

    const question = questions[0];
    const a = question.Discrimination_a;
    const b = question.Difficulty_b;
    const c = question.Guessing_c;

    // Calculate Fisher Information
    const exponent = -a * (thetaBefore - b);
    const prob = c + ((1 - c) / (1 + Math.exp(exponent)));
    const qVal = 1 - prob;
    const fisherInfo = (a * a * qVal * Math.pow(prob - c, 2)) /
                       (prob * Math.pow(1 - c, 2));

    // Record answer
    await connection.execute(
      `INSERT INTO PlacementAnswers
       (TestID, QuestionID, StudentAnswer, IsCorrect, ThetaBefore, ThetaAfter, FisherInformation)
       VALUES (?, ?, ?, ?, ?, ?, ?)`,
      [testId, questionId, studentAnswer, isCorrect, thetaBefore, thetaAfter, fisherInfo]
    );

    connection.release();

    res.json({
      success: true,
      feedback: isCorrect ? 'Correct!' : 'Incorrect',
      message: 'Answer recorded'
    });
  } catch (error) {
    console.error('Error submitting answer:', error);
    res.status(500).json({ success: false, error: error.message });
  }
});

/**
 * Complete placement test
 * POST /api/placement/complete
 */
router.post('/complete', async (req, res) => {
  const { testId, finalTheta, placementLevel } = req.body;

  try {
    const connection = await pool.getConnection();

    // Update test record
    await connection.execute(
      `UPDATE PlacementTests
       SET FinalTheta = ?, PlacementLevel = ?, CompletedAt = NOW()
       WHERE TestID = ?`,
      [finalTheta, placementLevel, testId]
    );

    // Update student's placement level
    await connection.execute(
      `UPDATE Students s
       JOIN PlacementTests pt ON s.StudentID = pt.StudentID
       SET s.PlacementLevel = ?
       WHERE pt.TestID = ?`,
      [placementLevel, testId]
    );

    connection.release();

    res.json({
      success: true,
      message: 'Placement test completed',
      placementLevel,
      finalTheta
    });
  } catch (error) {
    console.error('Error completing test:', error);
    res.status(500).json({ success: false, error: error.message });
  }
});

module.exports = router;
```

---

## Quick Reference Checklist

### Pre-Development Setup

- [ ] **Server Setup**
  - [ ] MySQL 8.0+ installed
  - [ ] Node.js 16+ installed
  - [ ] Domain/IP for API access
  - [ ] SSL certificate (optional but recommended)

- [ ] **Development Environment**
  - [ ] React Native CLI installed
  - [ ] Android Studio (for Android)
  - [ ] Xcode (for iOS, Mac only)
  - [ ] VS Code or preferred IDE

- [ ] **Database Preparation**
  - [ ] Run schema SQL file
  - [ ] Seed QuestionBank with 250 questions
  - [ ] Seed Modules table (5 modules)
  - [ ] Seed Nodes table (65 nodes total)
  - [ ] Create sample lesson content

---

### Week 1 Checklist

**Day 1-2: Foundation**
- [ ] MySQL database created
- [ ] All tables created successfully
- [ ] QuestionBank seeded with questions
- [ ] Registration API endpoint working
- [ ] Login API endpoint working
- [ ] Session management implemented
- [ ] Tested with Postman

**Day 3-4: IRT System**
- [ ] `calculateProbability()` function implemented
- [ ] `updateTheta()` function implemented
- [ ] `calculateFisherInfo()` function implemented
- [ ] `selectNextQuestion()` function implemented
- [ ] `/placement/start` endpoint working
- [ ] `/placement/next-question` endpoint working
- [ ] `/placement/submit-answer` endpoint working
- [ ] `/placement/complete` endpoint working
- [ ] Verified theta updates correctly with test cases

**Day 5: React Native Setup**
- [ ] React Native project initialized
- [ ] All dependencies installed
- [ ] Navigation structure set up
- [ ] API service layer created
- [ ] AuthContext implemented
- [ ] App runs on emulator/device

**Day 6-7: Placement Test UI**
- [ ] PlacementIntroScreen created
- [ ] PlacementQuestionScreen created
- [ ] PlacementResultScreen created
- [ ] Question flow (1-25) working
- [ ] Progress bar showing
- [ ] Options clickable
- [ ] Answer submission working
- [ ] Final result displaying correctly
- [ ] Placement level saved to storage

---

### Week 2 Checklist

**Day 8-9: Learning Content**
- [ ] ModuleContent table seeded
- [ ] `/modules` endpoint returning all 5 modules
- [ ] `/modules/:id/nodes` returning 13 nodes
- [ ] `/lessons/:nodeId/content` returning adaptive content
- [ ] LessonScreen displaying content
- [ ] QuizScreen displaying questions
- [ ] Quiz submission working

**Day 10: Adaptive Branching**
- [ ] Quiz score calculation correct
- [ ] 70% threshold check implemented
- [ ] Intervention content loading for failures
- [ ] Normal progression for passes
- [ ] StudentProgress table updating
- [ ] Navigation flow working correctly

**Day 11: Game Integration**
- [ ] Phonics Ninja game created
- [ ] Game mechanics working (touch/swipe)
- [ ] Timer (45 seconds) working
- [ ] Score tracking implemented
- [ ] Lives system (3 hearts) working
- [ ] Game integrated into LESSON → GAME → QUIZ flow
- [ ] Game score saved to database

**Day 12: Dashboard**
- [ ] DashboardScreen created
- [ ] 5 module cards displaying
- [ ] Progress indicators (X/13) correct
- [ ] Placement level badge showing
- [ ] XP and streak counters working
- [ ] Module lock/unlock logic working
- [ ] Settings button functional

**Day 13: Testing**
- [ ] Complete registration test passed
- [ ] Complete login test passed
- [ ] Full placement test (25 Q) passed
- [ ] Placement level assignment correct
- [ ] One lesson-game-quiz cycle passed
- [ ] Intervention flow tested
- [ ] Quiz failure → easier content verified
- [ ] Progress persistence verified
- [ ] Tested on both Android and iOS
- [ ] All critical bugs fixed

**Day 14: Polish & Deploy**
- [ ] Loading spinners added
- [ ] Error messages user-friendly
- [ ] Success animations added
- [ ] Colors/fonts consistent
- [ ] API deployed to production server
- [ ] Android APK built and tested
- [ ] iOS IPA built (if applicable)
- [ ] User documentation created
- [ ] Final production test passed

---

### Must-Have Features (Cannot Launch Without)

- [ ] Student registration
- [ ] Student login
- [ ] 25-question placement test with IRT
- [ ] Correct placement level determination
- [ ] View lesson content
- [ ] Take quizzes
- [ ] Progress to next lesson
- [ ] Progress persistence
- [ ] Database with all content
- [ ] API with all critical endpoints

---

### Nice-to-Have Features (Post-Launch OK)

- [ ] All 3 games fully implemented
- [ ] XP system
- [ ] Badges
- [ ] Streaks
- [ ] Leaderboards
- [ ] Speech recognition
- [ ] Animations and polish
- [ ] Sound effects
- [ ] Parent dashboard
- [ ] Teacher portal

---

## Important Notes for Developers

### 1. Understanding IRT is Critical

**Don't skip this!** The IRT system is the brain of LiteRise. Spend time understanding:
- How theta represents student ability
- Why Fisher Information picks the best questions
- How gradient ascent updates theta
- The difference between placement test (IRT) and adaptive lessons (branching)

### 2. Database First

Before writing any app code, make sure:
- All tables are created
- QuestionBank is fully seeded with realistic questions
- Each question has correct a, b, c parameters
- Test data exists for development

### 3. Test the IRT Engine Independently

Before integrating into the app:
```javascript
// Test case
const theta = 0.0;
const a = 1.5;
const b = 0.5;
const c = 0.25;

const prob = calculateProbability(theta, a, b, c);
console.log('Probability:', prob); // Should be ~0.42

const newTheta = updateTheta(theta, a, b, 1, prob);
console.log('New theta:', newTheta); // Should increase
```

### 4. API Response Times Matter

Students will lose patience if:
- Questions take > 2 seconds to load
- API calls timeout
- App feels sluggish

Optimize:
- Database queries (add indexes!)
- API payload size (don't send unnecessary data)
- Client-side caching

### 5. Edge Cases to Handle

- [ ] Student closes app mid-placement test → Resume from last question
- [ ] Network error during quiz submission → Retry mechanism
- [ ] Student gets 0% on quiz → Still provide intervention, don't crash
- [ ] Student completes all 13 nodes → Unlock next module
- [ ] Student tries to skip to locked module → Show "Complete Module X first"

### 6. Security Considerations

- [ ] Hash passwords with bcrypt (never store plain text!)
- [ ] Use JWT or session tokens for authentication
- [ ] Validate all API inputs (prevent SQL injection)
- [ ] Rate limit API endpoints (prevent abuse)
- [ ] Use HTTPS in production (no HTTP!)

### 7. Content Strategy

For each node, you need:
- **Beginner content:** Simple examples, slower pace, more scaffolding
- **Intermediate content:** Standard Grade 3 level
- **Advanced content:** Challenging examples, faster pace, enrichment

Example:
**Node 1: Nouns**
- Beginner: "A noun is a person, place, or thing. Examples: dog, school, Maria"
- Intermediate: "Nouns name people, places, things, or ideas. Common vs. Proper nouns"
- Advanced: "Abstract nouns, collective nouns, noun phrases in sentences"

### 8. Tracking Progress

Every student action should update the database:
- Question answered → PlacementAnswers table
- Quiz completed → QuizAttempts table
- Lesson finished → StudentProgress table
- Game played → GameScores table

This data is valuable for:
- Showing student progress
- Parent/teacher dashboards
- Analytics and improvement
- Debugging issues

---

## Troubleshooting Common Issues

### Issue: Theta Not Updating

**Symptoms:** Theta stays at 0.0 after multiple questions

**Causes:**
1. Learning rate (α) is 0
2. Discrimination (a) is 0
3. Expected probability = observed response (no information gain)

**Fix:**
```javascript
// Verify parameters
console.log('Learning rate:', 0.3); // Should be 0.3
console.log('Discrimination (a):', question.Discrimination_a); // Should be 0.5-2.5
console.log('Delta:', delta); // Should NOT be 0
```

---

### Issue: All Questions Too Easy/Hard

**Symptoms:** Student gets 100% or 0% on placement test

**Causes:**
1. Question difficulties (b) not distributed properly
2. Fisher Information calculation wrong
3. All questions from one difficulty level

**Fix:**
```sql
-- Check difficulty distribution
SELECT
  ROUND(Difficulty_b, 1) as Difficulty,
  COUNT(*) as Count
FROM QuestionBank
GROUP BY ROUND(Difficulty_b, 1)
ORDER BY Difficulty;

-- Should see questions spread from -2 to +2
```

---

### Issue: Adaptive Branching Not Working

**Symptoms:** Students always get same difficulty regardless of quiz score

**Causes:**
1. Quiz score not saving correctly
2. Content not tagged by difficulty level
3. Logic checking wrong column

**Fix:**
```javascript
// Verify quiz score is saving
console.log('Quiz score:', quizScore); // Should be 0-100

// Verify difficulty level is determined
const difficulty = quizScore >= 70 ? 'normal' : 'easier';
console.log('Loading difficulty:', difficulty);

// Verify content query
SELECT * FROM LessonContent
WHERE NodeID = ? AND DifficultyLevel = ?
```

---

### Issue: App Crashes on Placement Test

**Causes:**
1. QuestionBank empty
2. Network timeout
3. JSON parsing error
4. Division by zero in IRT calculation

**Fix:**
```javascript
// Add error handling
try {
  const question = await getNextQuestion(...);
  if (!question) {
    throw new Error('No question returned');
  }
} catch (error) {
  console.error('Error:', error);
  Alert.alert('Error', 'Failed to load question. Please try again.');
}
```

---

## Final Tips for Success

### 1. Start Simple, Iterate

Don't try to build everything at once:
- Week 1: Get IRT placement working perfectly
- Week 2: Get basic learning flow working
- Post-launch: Add games, gamification, polish

### 2. Use Real Student Data

Test with actual Grade 3 students:
- Are questions too hard/easy?
- Is vocabulary age-appropriate?
- Do they understand instructions?
- Is the UI intuitive for 8-year-olds?

### 3. Monitor Performance

Track these metrics:
- Average placement test completion time (should be 10-15 minutes)
- Quiz pass rate (should be 60-80%)
- Intervention rate (should be 20-30%)
- Daily active users
- Lesson completion rate

### 4. Document As You Go

Future you (and other developers) will thank you:
- Comment complex IRT calculations
- Document API endpoints (use Swagger/Postman)
- Create README for setup instructions
- Write deployment guide

### 5. Backup Everything

- Daily database backups
- Version control for code (Git)
- API logs
- Student progress data

---

## Summary: The Most Important Things

If you only remember 5 things:

1. **IRT Formula:** `P(θ) = c + (1 - c) / (1 + e^(-a(θ - b)))`
   - Use this to calculate question probability
   - Use Fisher Information to pick best question
   - Update theta with gradient ascent

2. **3-Phase Flow:** LESSON → GAME → QUIZ
   - Student learns concept
   - Plays game to practice
   - Takes quiz to prove mastery
   - System adapts based on quiz score

3. **Adaptive Branching:**
   - Quiz ≥ 70% → Continue normally
   - Quiz < 70% → Intervention (easier content)
   - Always give students another chance to succeed

4. **Database First:**
   - 250 questions with correct a, b, c parameters
   - 65 nodes of content (5 modules × 13 nodes)
   - 3 difficulty levels per node
   - Proper indexes for fast queries

5. **Test, Test, Test:**
   - Unit test IRT calculations
   - Integration test API endpoints
   - User test with real Grade 3 students
   - Don't skip Day 13 testing!

---

## Conclusion

You now have everything you need to rebuild LiteRise in 2 weeks. The key is to focus on the **must-have features** first (Priority 1), then add nice-to-haves later.

**Remember:** The IRT system is what makes LiteRise special. A student who is beginner level gets beginner questions. An advanced student gets advanced questions. Everyone learns at their perfect pace.

Good luck! 🚀

---

**Document Version:** 1.0
**Last Updated:** January 31, 2026
**For Questions:** Contact the LiteRise development team
