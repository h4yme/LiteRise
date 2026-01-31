# LiteRise System Rebuild - Priority Hierarchy

**Target Stack:** React Native + MySQL + REST API
**Timeline:** 2 weeks
**Purpose:** Adaptive English learning platform for Filipino Grade 3 students

---

## Table of Contents

1. [System Overview](#system-overview)
2. [Priority Hierarchy](#priority-hierarchy)
3. [Module Structure](#module-structure)
4. [IRT System Explained](#irt-system-explained)
5. [Implementation Priorities](#implementation-priorities)
6. [Content Requirements](#content-requirements)

---

## System Overview

### What is LiteRise?

LiteRise is an intelligent tutoring system that adapts to each student's ability level using **Item Response Theory (IRT)**. The system:

1. **Assesses** student ability through adaptive placement testing
2. **Teaches** concepts through multi-phase lessons (Lesson → Game → Quiz)
3. **Adapts** content difficulty based on performance
4. **Tracks** progress through 5 modules with 13 nodes each
5. **Motivates** through gamification (XP, badges, streaks)

### Core Philosophy

**Adaptive Learning:** Every student learns at their own pace
- Struggling students get easier content (intervention)
- Advanced students get challenging content (enrichment)
- No student is left behind or held back

**Three-Phase Learning Flow:**
```
LESSON (Learn the concept)
   ↓
GAME (Practice through play)
   ↓
QUIZ (Prove mastery)
   ↓
ADAPTIVE DECISION (Pass: continue / Fail: intervention)
```

---

## Priority Hierarchy

### Level 1: Foundation (Cannot Launch Without) 🔴

These are the **absolute essentials**. Without these, the system does not function.

```
Foundation Layer
├── Authentication System
│   ├── Student Registration
│   ├── Student Login
│   └── Session Management
│
├── Database Infrastructure
│   ├── Student Profiles
│   ├── Question Bank (250+ questions)
│   ├── Content Repository
│   └── Progress Tracking
│
├── IRT Placement System
│   ├── 25-Question Adaptive Test
│   ├── Theta Calculation Engine
│   ├── Fisher Information Question Selection
│   └── Level Determination (Beginner/Intermediate/Advanced)
│
└── Basic Learning Flow
    ├── View Lesson Content
    ├── Take Quiz
    ├── Check Score (Pass/Fail)
    └── Progress to Next Lesson
```

**Estimated Effort:** 60% of development time
**Success Criteria:** Student can register, take placement test, get placed correctly, complete one lesson

---

### Level 2: Adaptive Intelligence (Core Feature) 🟠

These features make LiteRise **adaptive** and differentiate it from basic learning apps.

```
Adaptive Layer
├── Intelligent Branching
│   ├── Quiz Score Analysis (70% threshold)
│   ├── Intervention System (Easier Content)
│   ├── Normal Progression
│   └── Difficulty Adjustment
│
├── Content Differentiation
│   ├── Beginner Level Content
│   ├── Intermediate Level Content
│   ├── Advanced Level Content
│   └── Dynamic Content Selection
│
├── Progress Intelligence
│   ├── Performance Tracking
│   ├── Struggle Detection
│   ├── Mastery Recognition
│   └── Adaptive Recommendations
│
└── Module System
    ├── 5 Core Modules
    ├── 13 Nodes per Module (12 lessons + 1 assessment)
    ├── Sequential Unlocking
    └── Prerequisite Management
```

**Estimated Effort:** 25% of development time
**Success Criteria:** Student who fails quiz gets easier content, student who passes continues normally

---

### Level 3: Engagement Layer (Important but Not Critical) 🟡

These features improve user experience and retention but aren't required for core functionality.

```
Engagement Layer
├── Dashboard & Visualization
│   ├── Progress Overview
│   ├── Module Cards
│   ├── Completion Indicators
│   ├── Current Level Display
│   └── Statistics Summary
│
├── Basic Gamification
│   ├── XP Points
│   ├── Streak Counter
│   ├── Progress Badges
│   └── Achievement Notifications
│
├── One Core Game
│   ├── Phonics Ninja (Recommended first)
│   ├── Game-Lesson Integration
│   ├── Score Tracking
│   └── Basic Mechanics
│
└── User Experience Polish
    ├── Loading States
    ├── Error Messages
    ├── Success Feedback
    └── Smooth Transitions
```

**Estimated Effort:** 10% of development time
**Success Criteria:** Students enjoy using the app, dashboard shows meaningful progress

---

### Level 4: Enhancement Features (Post-Launch) 🟢

These can be added after initial launch to improve the system.

```
Enhancement Layer
├── Advanced Gamification
│   ├── Full Badge System
│   ├── Leaderboards
│   ├── Daily Challenges
│   ├── Rewards Shop
│   └── Social Features
│
├── Additional Games
│   ├── Synonym Sprint
│   ├── Word Explosion
│   ├── Sentence Builder
│   ├── Story Weaver
│   └── Other Revolutionary Games
│
├── Advanced Features
│   ├── Speech Recognition
│   ├── Pronunciation Scoring
│   ├── Audio Lessons
│   ├── Video Content
│   └── Interactive Exercises
│
├── Analytics & Reporting
│   ├── Parent Dashboard
│   ├── Teacher Portal
│   ├── Progress Reports
│   ├── Performance Analytics
│   └── Intervention Reports
│
└── System Optimization
    ├── Offline Mode
    ├── Content Caching
    ├── Performance Tuning
    ├── Accessibility Features
    └── Multi-language Support
```

**Estimated Effort:** Future iterations
**Success Criteria:** Enhanced user engagement and broader accessibility

---

## Module Structure

### The 5 Core Modules

```
LiteRise Learning Modules
│
├── Module 1: Grammar Fundamentals
│   ├── Node 1: Nouns and Pronouns
│   ├── Node 2: Action Words (Verbs)
│   ├── Node 3: Describing Words (Adjectives)
│   ├── Node 4: Position Words (Prepositions)
│   ├── Node 5: Sentence Structure
│   ├── Node 6: Singular and Plural
│   ├── Node 7: Past and Present Tense
│   ├── Node 8: Question Formation
│   ├── Node 9: Sentence Types
│   ├── Node 10: Subject-Verb Agreement
│   ├── Node 11: Articles (a, an, the)
│   ├── Node 12: Conjunctions
│   └── Node 13: Final Assessment
│
├── Module 2: Vocabulary Building
│   ├── Node 1: Common Words (High-Frequency)
│   ├── Node 2: Word Families
│   ├── Node 3: Synonyms and Antonyms
│   ├── Node 4: Homophones
│   ├── Node 5: Context Clues
│   ├── Node 6: Compound Words
│   ├── Node 7: Prefixes
│   ├── Node 8: Suffixes
│   ├── Node 9: Root Words
│   ├── Node 10: Idioms and Expressions
│   ├── Node 11: Category Words (Animals, Food, etc.)
│   ├── Node 12: Multiple Meanings
│   └── Node 13: Final Assessment
│
├── Module 3: Reading Comprehension
│   ├── Node 1: Main Idea
│   ├── Node 2: Supporting Details
│   ├── Node 3: Sequence of Events
│   ├── Node 4: Cause and Effect
│   ├── Node 5: Compare and Contrast
│   ├── Node 6: Making Predictions
│   ├── Node 7: Drawing Conclusions
│   ├── Node 8: Character Analysis
│   ├── Node 9: Story Elements
│   ├── Node 10: Fact vs Opinion
│   ├── Node 11: Author's Purpose
│   ├── Node 12: Making Inferences
│   └── Node 13: Final Assessment
│
├── Module 4: Writing Skills
│   ├── Node 1: Complete Sentences
│   ├── Node 2: Capitalization Rules
│   ├── Node 3: Punctuation Basics
│   ├── Node 4: Paragraph Structure
│   ├── Node 5: Descriptive Writing
│   ├── Node 6: Narrative Writing
│   ├── Node 7: Opinion Writing
│   ├── Node 8: Writing Process
│   ├── Node 9: Editing and Revising
│   ├── Node 10: Dialogue Writing
│   ├── Node 11: Letter Writing
│   ├── Node 12: Creative Expression
│   └── Node 13: Final Assessment
│
└── Module 5: Listening & Speaking
    ├── Node 1: Following Directions
    ├── Node 2: Active Listening
    ├── Node 3: Pronunciation Practice
    ├── Node 4: Phonics Patterns (CVC, CVCC)
    ├── Node 5: Long and Short Vowels
    ├── Node 6: Blends and Digraphs
    ├── Node 7: Rhyming Words
    ├── Node 8: Syllable Counting
    ├── Node 9: Oral Presentation
    ├── Node 10: Conversation Skills
    ├── Node 11: Storytelling
    ├── Node 12: Listening Comprehension
    └── Node 13: Final Assessment
```

**Total Content:** 5 modules × 13 nodes = **65 learning units**

---

## IRT System Explained

### What is IRT?

**Item Response Theory (IRT)** is a mathematical framework for adaptive testing. Instead of giving everyone the same questions, IRT selects questions based on the student's current ability level.

### Key Concepts

#### 1. Theta (θ) - Student Ability

- **Range:** -3.0 to +3.0
- **Starting Point:** 0.0 (neutral, unknown ability)
- **Meaning:**
  - θ = -2.0 → Very low ability
  - θ = -0.5 → Below average (Beginner)
  - θ = 0.0 → Average
  - θ = +0.5 → Above average (Intermediate)
  - θ = +1.5 → High ability (Advanced)

#### 2. Question Parameters

Each question has three parameters:

**a - Discrimination (0.5 to 2.5)**
- How well the question separates strong from weak students
- Higher = better quality question
- Example: a = 1.5 means the question is good at measuring ability

**b - Difficulty (-3.0 to +3.0)**
- How hard the question is
- b = -1.0 → Easy question
- b = 0.0 → Medium question
- b = +1.5 → Hard question

**c - Guessing (usually 0.25)**
- Probability of random correct answer
- For 4-option multiple choice: c = 0.25 (25% chance)

#### 3. The IRT Formula (3-Parameter Logistic Model)

```
P(θ) = c + (1 - c) / (1 + e^(-a(θ - b)))
```

**What it calculates:**
"What is the probability that a student with ability θ will answer this question correctly?"

**Example:**
- Student: θ = 0.5 (intermediate)
- Question: a = 1.5, b = 0.3, c = 0.25
- Result: P(θ) = 0.62 → 62% chance of correct answer

#### 4. Fisher Information

```
Fisher Information = How much we'll learn from this question
```

- **High Fisher Info:** Question is perfect for this student (50/50 chance)
- **Low Fisher Info:** Question is too easy or too hard (we learn nothing)

**The system always picks the question with the highest Fisher Information**

#### 5. Theta Update (Gradient Ascent)

After each answer, we update the student's ability:

```
If answer is CORRECT → θ increases
If answer is WRONG → θ decreases
```

The change depends on:
- How unexpected the result was
- Question discrimination (a)
- Learning rate (α = 0.3)

### IRT Workflow

```
Step 1: Start Placement Test
   ↓
   θ = 0.0 (we don't know student's ability yet)

Step 2: Select First Question
   ↓
   Calculate Fisher Information for all questions in category
   Pick question with highest Fisher Info

Step 3: Student Answers
   ↓
   Calculate P(θ) = expected probability of correct answer
   Compare to actual answer (correct = 1, wrong = 0)

Step 4: Update Theta
   ↓
   Δθ = learning_rate × discrimination × (actual - expected)
   θ_new = θ_old + Δθ
   Clamp between -3 and +3

Step 5: Repeat
   ↓
   Do steps 2-4 for 25 questions total
   (5 questions per category × 5 categories)

Step 6: Determine Placement
   ↓
   If θ < -0.5 → Beginner
   If -0.5 ≤ θ < 0.5 → Intermediate
   If θ ≥ 0.5 → Advanced
```

### Real-World Example: Maria's Placement Test

**Starting:** θ = 0.0

**Question 1:** "What is the plural of 'child'?"
- Difficulty: b = -0.5 (easy)
- Discrimination: a = 1.5
- Expected probability: P(0.0) = 0.73 (73% chance)
- Maria's answer: CORRECT ✓
- Theta update: θ = 0.0 + 0.12 = **0.12**

**Question 2:** "Identify the verb in this sentence..."
- Difficulty: b = 0.8 (hard)
- Discrimination: a = 2.0
- Current θ: 0.12
- Expected probability: P(0.12) = 0.38 (38% chance)
- Maria's answer: WRONG ✗
- Theta update: θ = 0.12 - 0.45 = **-0.33**

**After 25 questions:** θ_final = **-0.6**

**Placement:** θ = -0.6 < -0.5 → **Beginner Level**

---

## Implementation Priorities

### Priority 1: Core System (Week 1 Focus) 🔴

```
Core System
│
├── 1.1 Database Setup
│   ├── MySQL installation and configuration
│   ├── Create all database tables
│   ├── Seed Question Bank (250 questions minimum)
│   │   ├── 50 Grammar questions
│   │   ├── 50 Vocabulary questions
│   │   ├── 50 Reading questions
│   │   ├── 50 Writing questions
│   │   └── 50 Listening questions
│   ├── Set IRT parameters (a, b, c) for each question
│   └── Seed basic student test data
│
├── 1.2 Authentication System
│   ├── Student registration (name, age, school)
│   ├── Login with credentials
│   ├── Session token management
│   ├── Password security (hashing)
│   └── Session persistence
│
├── 1.3 IRT Placement Engine
│   ├── Probability calculation: P(θ)
│   ├── Theta update algorithm
│   ├── Fisher Information calculation
│   ├── Question selection logic
│   ├── 25-question test flow
│   ├── Category rotation (5 questions × 5 categories)
│   ├── Final theta calculation
│   └── Level determination
│
├── 1.4 REST API Layer
│   ├── POST /auth/register
│   ├── POST /auth/login
│   ├── POST /placement/start
│   ├── GET /placement/next-question
│   ├── POST /placement/submit-answer
│   ├── POST /placement/complete
│   └── Error handling and validation
│
└── 1.5 Mobile App Foundation
    ├── React Native project setup
    ├── Navigation structure
    ├── Authentication screens (Login, Register)
    ├── Placement test screens
    │   ├── Introduction screen
    │   ├── Question screen (1-25)
    │   └── Results screen
    ├── API integration layer
    └── Local storage (AsyncStorage)
```

**Success Metric:** New student can register, take full 25-question placement test, receive correct placement level

---

### Priority 2: Learning System (Week 1-2 Transition) 🟠

```
Learning System
│
├── 2.1 Content Management
│   ├── Create 65 nodes of content (5 modules × 13 nodes)
│   ├── Write content for 3 difficulty levels per node
│   │   ├── Beginner version
│   │   ├── Intermediate version
│   │   └── Advanced version
│   ├── Create quiz questions for each node (10 questions per node)
│   └── Content organization and tagging
│
├── 2.2 Module & Node System
│   ├── Module list endpoint (GET /modules)
│   ├── Node ladder endpoint (GET /modules/:id/nodes)
│   ├── Lesson content endpoint (GET /lessons/:nodeId/content)
│   ├── Quiz questions endpoint (GET /quiz/:nodeId/questions)
│   ├── Module unlocking logic
│   └── Sequential progression rules
│
├── 2.3 Learning Flow Implementation
│   ├── Lesson screen (display content)
│   ├── Game integration point
│   ├── Quiz screen (10 questions)
│   ├── Quiz scoring system
│   ├── Pass/fail threshold (70%)
│   ├── Flow navigation (Lesson → Game → Quiz)
│   └── Completion tracking
│
├── 2.4 Adaptive Branching Intelligence
│   ├── Quiz score analysis
│   ├── Intervention detection (score < 70%)
│   ├── Difficulty adjustment logic
│   │   ├── Failed quiz → Load beginner content
│   │   ├── Passed quiz → Continue normal content
│   │   └── High score → Consider advanced content
│   ├── Intervention content loading
│   ├── Retry mechanism
│   └── Progress recovery
│
└── 2.5 Progress Tracking
    ├── Student progress database
    ├── Node completion status
    ├── Quiz score history
    ├── Current position tracking
    ├── Module completion percentage
    └── Progress save/load functionality
```

**Success Metric:** Student completes full learning cycle (Lesson → Game → Quiz), system adapts when student fails quiz

---

### Priority 3: User Experience (Week 2 Focus) 🟡

```
User Experience
│
├── 3.1 Dashboard
│   ├── Welcome screen with student name
│   ├── 5 module cards display
│   ├── Progress indicators (X/13 completed)
│   ├── Current placement level badge
│   ├── Overall progress visualization
│   ├── Next lesson recommendation
│   └── Settings/profile access
│
├── 3.2 Basic Gamification
│   ├── XP system
│   │   ├── Points for lesson completion
│   │   ├── Bonus for quiz passing
│   │   └── Total XP tracking
│   ├── Streak tracking
│   │   ├── Daily login streak
│   │   ├── Consecutive quiz passes
│   │   └── Longest streak record
│   ├── Basic badges
│   │   ├── First lesson complete
│   │   ├── Module complete
│   │   ├── Perfect quiz score
│   │   └── Week streak
│   └── Achievement notifications
│
├── 3.3 One Game Integration
│   ├── Phonics Ninja (recommended first game)
│   │   ├── Word slicing mechanic
│   │   ├── 45-second timer
│   │   ├── Score system
│   │   ├── Lives (3 hearts)
│   │   └── Pattern targeting
│   ├── Game-lesson connection
│   ├── Score submission
│   └── Return to quiz flow
│
└── 3.4 UI Polish
    ├── Loading spinners
    ├── Error message displays
    ├── Success animations
    ├── Smooth screen transitions
    ├── Progress indicators
    ├── Consistent styling
    └── Child-friendly design
```

**Success Metric:** Students enjoy using the app, dashboard clearly shows progress, one game is playable

---

### Priority 4: Future Enhancements 🟢

```
Future Enhancements
│
├── 4.1 Advanced Gamification
│   ├── Complete badge system (50+ badges)
│   ├── Leaderboards (class, school, national)
│   ├── Daily challenges
│   ├── Weekly quests
│   ├── Reward shop (avatars, themes)
│   └── Friend system
│
├── 4.2 Additional Games
│   ├── Synonym Sprint (endless runner)
│   ├── Word Explosion (match-3 puzzle)
│   ├── Sentence Builder (construction game)
│   ├── Story Weaver (narrative game)
│   ├── Grammar Guardian (tower defense)
│   └── Vocabulary Vault (memory game)
│
├── 4.3 Rich Media Features
│   ├── Azure Speech API integration
│   ├── Pronunciation assessment
│   ├── Text-to-speech for lessons
│   ├── Audio lesson content
│   ├── Video explanations
│   └── Interactive animations
│
├── 4.4 Analytics & Reporting
│   ├── Parent dashboard
│   │   ├── Child progress overview
│   │   ├── Strengths and weaknesses
│   │   ├── Time spent learning
│   │   └── Recommendations
│   ├── Teacher portal
│   │   ├── Class progress tracking
│   │   ├── Student comparison
│   │   ├── Intervention alerts
│   │   └── Content assignment
│   ├── Admin panel
│   │   ├── Content management
│   │   ├── User management
│   │   ├── System analytics
│   │   └── Configuration
│   └── Detailed reports
│       ├── Weekly progress report
│       ├── Module completion report
│       ├── IRT ability tracking over time
│       └── Learning pattern analysis
│
└── 4.5 Advanced Features
    ├── Offline mode with sync
    ├── Content caching
    ├── Accessibility features
    │   ├── Screen reader support
    │   ├── High contrast mode
    │   ├── Font size adjustment
    │   └── Dyslexia-friendly fonts
    ├── Multi-language support
    │   ├── Tagalog interface
    │   ├── Other Philippine languages
    │   └── Language switcher
    └── Performance optimization
        ├── Image lazy loading
        ├── Database query optimization
        ├── API response caching
        └── Code splitting
```

---

## Content Requirements

### Question Bank Requirements

**Total Questions Needed:** 250 minimum (300 recommended)

#### Distribution by Category

```
Question Bank Distribution
│
├── Grammar (50 questions)
│   ├── Easy (b = -2 to -1): 15 questions
│   ├── Medium (b = -0.5 to 0.5): 20 questions
│   └── Hard (b = 1 to 2): 15 questions
│
├── Vocabulary (50 questions)
│   ├── Easy (b = -2 to -1): 15 questions
│   ├── Medium (b = -0.5 to 0.5): 20 questions
│   └── Hard (b = 1 to 2): 15 questions
│
├── Reading Comprehension (50 questions)
│   ├── Easy (b = -2 to -1): 15 questions
│   ├── Medium (b = -0.5 to 0.5): 20 questions
│   └── Hard (b = 1 to 2): 15 questions
│
├── Writing Skills (50 questions)
│   ├── Easy (b = -2 to -1): 15 questions
│   ├── Medium (b = -0.5 to 0.5): 20 questions
│   └── Hard (b = 1 to 2): 15 questions
│
└── Listening & Speaking (50 questions)
    ├── Easy (b = -2 to -1): 15 questions
    ├── Medium (b = -0.5 to 0.5): 20 questions
    └── Hard (b = 1 to 2): 15 questions
```

#### IRT Parameter Guidelines

**Discrimination (a):**
- Poor questions: a < 1.0 (avoid these)
- Good questions: a = 1.0 to 1.5
- Excellent questions: a = 1.5 to 2.5
- Too steep: a > 2.5 (rare, use sparingly)

**Difficulty (b):**
- Very Easy: b = -2.5 to -1.5
- Easy: b = -1.5 to -0.5
- Medium: b = -0.5 to 0.5
- Hard: b = 0.5 to 1.5
- Very Hard: b = 1.5 to 2.5

**Guessing (c):**
- 4-option multiple choice: c = 0.25
- True/False: c = 0.50
- No guessing (constructed response): c = 0.00

### Lesson Content Requirements

**Total Nodes:** 65 (5 modules × 13 nodes)
**Versions per Node:** 3 (Beginner, Intermediate, Advanced)
**Total Content Pieces:** 195

#### Content Structure per Node

```
Each Node Contains:
│
├── Learning Objectives (What student will learn)
├── Lesson Content
│   ├── Beginner Version
│   │   ├── Simple explanations
│   │   ├── Many examples
│   │   ├── Step-by-step guidance
│   │   ├── Visual aids
│   │   └── Scaffolded practice
│   ├── Intermediate Version
│   │   ├── Standard explanations
│   │   ├── Adequate examples
│   │   ├── Clear structure
│   │   ├── Some visuals
│   │   └── Balanced practice
│   └── Advanced Version
│       ├── Concise explanations
│       ├── Complex examples
│       ├── Enrichment content
│       ├── Minimal scaffolding
│       └── Challenging practice
│
├── Game Integration
│   ├── Which game to play
│   ├── Target skills to practice
│   └── Success criteria
│
└── Quiz (10 questions)
    ├── Aligned to learning objectives
    ├── Mix of difficulty levels
    ├── Clear correct answers
    └── Explanations for wrong answers
```

### Content Writing Guidelines

**For Beginner Level:**
- Use simple, concrete language
- Short sentences (8-12 words)
- One concept at a time
- Many examples with Filipino context
- Visual support (images, diagrams)
- Repetition for reinforcement
- Encouraging tone

**For Intermediate Level:**
- Standard Grade 3 English
- Moderate sentence length (12-15 words)
- Connected concepts
- Relevant examples
- Some visual support
- Balance of instruction and practice
- Supportive tone

**For Advanced Level:**
- More sophisticated vocabulary
- Longer, complex sentences
- Multiple concepts connected
- Challenging examples
- Minimal visual support
- More independent practice
- Encouraging challenge tone

---

## Database Schema Essentials

### Core Tables

```
Database Structure
│
├── Students
│   ├── StudentID (Primary Key)
│   ├── Name
│   ├── Age
│   ├── School
│   ├── PasswordHash
│   ├── PlacementLevel (Beginner/Intermediate/Advanced)
│   ├── CurrentTheta (Current ability estimate)
│   └── DateRegistered
│
├── QuestionBank
│   ├── QuestionID (Primary Key)
│   ├── Category (Grammar/Vocabulary/Reading/Writing/Listening)
│   ├── QuestionText
│   ├── OptionA, OptionB, OptionC, OptionD
│   ├── CorrectAnswer
│   ├── Difficulty_b (IRT parameter)
│   ├── Discrimination_a (IRT parameter)
│   ├── Guessing_c (IRT parameter)
│   └── Explanation
│
├── PlacementTests
│   ├── TestID (Primary Key)
│   ├── StudentID (Foreign Key)
│   ├── StartTheta (0.0)
│   ├── FinalTheta
│   ├── PlacementLevel
│   ├── QuestionsAsked (JSON array of IDs)
│   ├── DateTaken
│   └── CompletedAt
│
├── PlacementAnswers
│   ├── AnswerID (Primary Key)
│   ├── TestID (Foreign Key)
│   ├── QuestionID (Foreign Key)
│   ├── StudentAnswer
│   ├── IsCorrect
│   ├── ThetaBefore
│   ├── ThetaAfter
│   └── FisherInformation
│
├── Modules
│   ├── ModuleID (Primary Key)
│   ├── ModuleName
│   ├── Description
│   ├── IconURL
│   └── SortOrder (1-5)
│
├── Nodes
│   ├── NodeID (Primary Key)
│   ├── ModuleID (Foreign Key)
│   ├── NodeNumber (1-13)
│   ├── Title
│   ├── LearningObjectives
│   └── IsFinalAssessment
│
├── LessonContent
│   ├── ContentID (Primary Key)
│   ├── NodeID (Foreign Key)
│   ├── DifficultyLevel (Beginner/Intermediate/Advanced)
│   ├── ContentJSON (Full lesson content)
│   └── CreatedAt
│
├── QuizQuestions
│   ├── QuizQuestionID (Primary Key)
│   ├── NodeID (Foreign Key)
│   ├── QuestionText
│   ├── OptionA, OptionB, OptionC, OptionD
│   ├── CorrectAnswer
│   └── Explanation
│
├── StudentProgress
│   ├── ProgressID (Primary Key)
│   ├── StudentID (Foreign Key)
│   ├── NodeID (Foreign Key)
│   ├── Status (not_started/in_progress/completed)
│   ├── LatestQuizScore
│   ├── NeedsIntervention
│   ├── InterventionCompleted
│   └── CompletedAt
│
├── QuizAttempts
│   ├── AttemptID (Primary Key)
│   ├── StudentID (Foreign Key)
│   ├── NodeID (Foreign Key)
│   ├── Score (percentage)
│   ├── TotalQuestions
│   ├── CorrectAnswers
│   ├── AnswersJSON (Array of answers)
│   └── AttemptedAt
│
├── StudentGamification
│   ├── GamificationID (Primary Key)
│   ├── StudentID (Foreign Key)
│   ├── TotalXP
│   ├── CurrentStreak
│   ├── LongestStreak
│   └── LastActivityDate
│
├── Badges
│   ├── BadgeID (Primary Key)
│   ├── BadgeName
│   ├── Description
│   ├── IconURL
│   └── RequirementType
│
├── StudentBadges
│   ├── StudentBadgeID (Primary Key)
│   ├── StudentID (Foreign Key)
│   ├── BadgeID (Foreign Key)
│   └── EarnedAt
│
└── GameScores
    ├── ScoreID (Primary Key)
    ├── StudentID (Foreign Key)
    ├── GameName
    ├── NodeID (Foreign Key)
    ├── Score
    ├── Duration
    └── PlayedAt
```

---

## API Endpoints Overview

### Authentication Endpoints

```
POST /api/auth/register
├── Input: name, age, school, password
├── Output: studentId, token
└── Purpose: Create new student account

POST /api/auth/login
├── Input: name, password
├── Output: studentId, token, placementLevel
└── Purpose: Student login

GET /api/auth/session
├── Input: token
├── Output: studentId, isValid
└── Purpose: Verify session
```

### Placement Test Endpoints

```
POST /api/placement/start
├── Input: studentId
├── Output: testId, startTheta
└── Purpose: Initialize new placement test

GET /api/placement/next-question
├── Input: testId, currentTheta, questionsAsked, category
├── Output: question (with a, b, c parameters)
└── Purpose: Get next adaptive question using Fisher Information

POST /api/placement/submit-answer
├── Input: testId, questionId, answer, thetaBefore, thetaAfter
├── Output: isCorrect, feedback
└── Purpose: Record answer and theta update

POST /api/placement/complete
├── Input: testId, finalTheta, placementLevel
├── Output: success, placementLevel
└── Purpose: Finalize test and set student level
```

### Learning Endpoints

```
GET /api/modules
├── Input: (none)
├── Output: array of 5 modules
└── Purpose: Get all learning modules

GET /api/modules/:moduleId/nodes
├── Input: moduleId
├── Output: array of 13 nodes
└── Purpose: Get module's lesson ladder

GET /api/lessons/:nodeId/content
├── Input: nodeId, studentLevel
├── Output: lesson content (adaptive by level)
└── Purpose: Get lesson content for student's level

GET /api/quiz/:nodeId/questions
├── Input: nodeId
├── Output: array of 10 quiz questions
└── Purpose: Get quiz for node

POST /api/quiz/submit
├── Input: studentId, nodeId, answers
├── Output: score, passed, needsIntervention
└── Purpose: Score quiz and determine next action
```

### Progress Endpoints

```
GET /api/progress/:studentId
├── Input: studentId
├── Output: all progress data
└── Purpose: Get student's complete progress

POST /api/progress/update
├── Input: studentId, nodeId, status, score
├── Output: success
└── Purpose: Update progress after lesson/quiz

GET /api/dashboard/:studentId
├── Input: studentId
├── Output: modules, progress, stats, nextLesson
└── Purpose: Get dashboard data
```

### Gamification Endpoints

```
GET /api/gamification/:studentId
├── Input: studentId
├── Output: xp, streaks, badges
└── Purpose: Get gamification data

POST /api/gamification/award-xp
├── Input: studentId, amount, reason
├── Output: newTotal
└── Purpose: Award XP points

POST /api/gamification/badge-earned
├── Input: studentId, badgeId
├── Output: success
└── Purpose: Award badge

POST /api/games/score
├── Input: studentId, gameName, nodeId, score
├── Output: success
└── Purpose: Record game score
```

---

## Success Metrics

### Level 1 Success Criteria (Foundation)

```
✓ Student Registration
  └── New student can create account in < 30 seconds

✓ Placement Test
  ├── All 25 questions delivered correctly
  ├── Questions adapt based on answers
  ├── Theta updates after each answer
  ├── Final theta calculated correctly
  └── Placement level assigned accurately

✓ Database
  ├── All tables created
  ├── 250+ questions loaded
  ├── IRT parameters set correctly
  └── Data persists properly

✓ Basic Learning
  ├── Student can view lesson
  ├── Student can take quiz
  └── Progress saves to database
```

### Level 2 Success Criteria (Adaptive)

```
✓ Adaptive Branching
  ├── Quiz < 70% triggers intervention
  ├── Intervention loads easier content
  ├── After intervention, student can retry
  └── Quiz ≥ 70% allows progression

✓ Content Differentiation
  ├── Beginner content loads for beginner students
  ├── Intermediate content loads for intermediate students
  ├── Advanced content loads for advanced students
  └── Content matches placement level

✓ Full Module System
  ├── All 5 modules visible
  ├── Each module shows 13 nodes
  ├── Modules unlock sequentially
  └── Progress tracked accurately
```

### Level 3 Success Criteria (Engagement)

```
✓ Dashboard
  ├── Shows all 5 modules
  ├── Displays current progress
  ├── Shows placement level
  └── Recommends next lesson

✓ Gamification
  ├── XP awards correctly
  ├── Streaks track daily activity
  ├── Badges unlock on achievements
  └── Notifications display

✓ One Game
  ├── Game is playable
  ├── Integrated in learning flow
  ├── Score saves to database
  └── Students enjoy playing
```

---

## Final Summary

### What Makes LiteRise Special

1. **IRT Adaptive Testing:** Unlike fixed tests, every student gets questions matched to their ability
2. **Intelligent Branching:** System detects struggling students and provides intervention automatically
3. **Three-Phase Learning:** Lesson → Game → Quiz creates engaging, effective learning
4. **MATATAG Aligned:** Content follows Philippine curriculum standards for Grade 3
5. **Gamified Experience:** XP, badges, and streaks keep students motivated

### The Minimum Viable Product (MVP)

To launch LiteRise with core functionality:

**Must Have:**
- Student accounts (register/login)
- 25-question IRT placement test
- Correct placement level assignment (Beginner/Intermediate/Advanced)
- At least Module 1 (13 nodes) with all 3 difficulty levels
- Lesson-Quiz flow (game can be placeholder)
- Basic progress tracking
- Simple dashboard

**Total Minimum Content:**
- 250 placement test questions
- 13 lesson nodes × 3 difficulty levels = 39 content pieces
- 130 quiz questions (10 per node × 13 nodes)

**Can Be Added Later:**
- Full games
- Advanced gamification
- Additional modules
- Parent/teacher dashboards
- Analytics and reporting

### The Priority Order

```
Week 1 → Build Foundation (Priority 1)
   IRT placement test working perfectly
   Database fully populated
   Authentication solid

Week 2 → Add Intelligence (Priority 2)
   Adaptive branching working
   At least one module complete with all levels
   Progress tracking functional

Week 2 End → Polish (Priority 3)
   Dashboard looks good
   Basic gamification
   One game playable

Post-Launch → Enhance (Priority 4)
   More games
   More modules
   Advanced features
   Analytics
```

---

**Document Version:** 2.0
**Focus:** Priority hierarchy and module structure
**No Code:** Pure strategic planning
**For:** Developers rebuilding LiteRise in 2 weeks
