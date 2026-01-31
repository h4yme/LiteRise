# LiteRise Educational Platform - Complete System Documentation
## Technical Blueprint for System Rebuild (2-Week Timeline)

**Version:** 1.0
**Date:** January 31, 2026
**Target:** Android Application + PHP Backend + SQL Server Database
**Timeline:** 2 weeks (14 days)

---

## 📋 Table of Contents

1. [Executive Summary](#executive-summary)
2. [System Architecture](#system-architecture)
3. [Technology Stack](#technology-stack)
4. [Database Schema](#database-schema)
5. [IRT (Item Response Theory) System](#irt-system)
6. [API Endpoints](#api-endpoints)
7. [Android Application Structure](#android-application)
8. [User Flow](#user-flow)
9. [Implementation Timeline](#implementation-timeline)
10. [Testing Requirements](#testing-requirements)

---

## 1. Executive Summary

### 1.1 Project Overview

**LiteRise** is an adaptive English learning platform for Filipino Grade 3 students following the MATATAG curriculum. The system uses **Item Response Theory (IRT)** for initial student placement and implements a **3-phase adaptive learning flow** (LESSON → GAME → QUIZ).

### 1.2 Key Features

✅ **IRT-Based Placement Test** - Adaptive 25-question assessment
✅ **5 Learning Modules** - MATATAG Grade 3 curriculum
✅ **13 Nodes per Module** - 12 lessons + 1 final assessment
✅ **3-Phase Learning** - Lesson → Game → Quiz (auto-progression)
✅ **Adaptive Branching** - Performance-based intervention/enrichment
✅ **Gamification** - XP, badges, streaks, leaderboards
✅ **Pronunciation Assessment** - Azure Speech API integration
✅ **Revolutionary Games** - Phonics Ninja, Synonym Sprint, Word Explosion

### 1.3 Target Metrics

- **Students:** 500+ concurrent users
- **Response Time:** < 2 seconds for all API calls
- **Uptime:** 99.5%
- **Data Retention:** 5 years

---

## 2. System Architecture

### 2.1 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     ANDROID APPLICATION                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │   Activities │  │    Helpers   │  │    Models    │     │
│  │  (40+ views) │  │  (IRT, API)  │  │   (DTOs)     │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
│         │                  │                   │            │
│         └──────────────────┴───────────────────┘            │
│                           │                                 │
│                    Retrofit 2 HTTP Client                   │
└────────────────────────────┬────────────────────────────────┘
                             │
                      HTTPS / JSON
                             │
┌────────────────────────────▼────────────────────────────────┐
│                      PHP BACKEND (API)                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │ Auth APIs    │  │ Lesson APIs  │  │  IRT APIs    │     │
│  │ (login, reg) │  │ (content)    │  │ (adaptive)   │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
│         │                  │                   │            │
│         └──────────────────┴───────────────────┘            │
│                    PDO SQL Driver                           │
└────────────────────────────┬────────────────────────────────┘
                             │
                      SQL Queries
                             │
┌────────────────────────────▼────────────────────────────────┐
│                  MICROSOFT SQL SERVER                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │   Students   │  │    Nodes     │  │   Progress   │     │
│  │   (Users)    │  │  (Lessons)   │  │  (Tracking)  │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │  Responses   │  │   Sessions   │  │   Decisions  │     │
│  │ (IRT Data)   │  │ (Test Data)  │  │  (Adaptive)  │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 Data Flow

```
User Action (Android)
    → HTTP Request (Retrofit)
    → PHP API Endpoint
    → SQL Server Query
    → Process Data
    → Return JSON Response
    → Parse in Android (Gson)
    → Update UI
```

### 2.3 External Services

**Azure Speech Service:**
- Pronunciation assessment
- Text-to-speech
- Endpoint: `https://eastus.api.cognitive.microsoft.com/`
- Key: Stored in environment variables

---

## 3. Technology Stack

### 3.1 Frontend (Android)

| Component | Technology | Version | Purpose |
|-----------|------------|---------|---------|
| Language | Java | 8+ | Core programming |
| Min SDK | Android 5.0 | 21 | Minimum OS version |
| Target SDK | Android 14 | 34 | Latest features |
| HTTP Client | Retrofit 2 | 2.9.0 | API communication |
| JSON Parser | Gson | 2.10.1 | JSON serialization |
| UI Framework | Material Design | 1.11.0 | Modern UI components |
| Database | SQLite | Built-in | Local caching |
| Image Loading | Glide | 4.16.0 | Image optimization |

### 3.2 Backend (API)

| Component | Technology | Version | Purpose |
|-----------|------------|---------|---------|
| Language | PHP | 7.4+ | Server-side logic |
| Database Driver | PDO | Built-in | SQL Server connection |
| Server | Apache/Nginx | 2.4+ | Web server |
| Authentication | Custom JWT | - | Session management |

### 3.3 Database

| Component | Technology | Version | Purpose |
|-----------|------------|---------|---------|
| RDBMS | Microsoft SQL Server | 2019+ | Data storage |
| ORM | None (Raw SQL) | - | Direct queries |

### 3.4 Development Tools

- **IDE:** Android Studio Hedgehog (2023.1.1+)
- **Version Control:** Git
- **API Testing:** Postman
- **Database Tool:** SQL Server Management Studio

---

## 4. Database Schema

### 4.1 Entity Relationship Diagram

```
Students ──┬── AssessmentSessions ──── StudentResponses
           │                                │
           │                           AssessmentItems
           │
           ├── StudentNodeProgress
           │         │
           │         │
           ├── QuizAttempts
           │
           └── GamificationData
                     │
                     ├── XPHistory
                     ├── BadgesEarned
                     └── StreakLog

Modules ──── Nodes ──── StudentNodeProgress
                │
                └── QuizQuestions
                │
                └── SupplementalNodes

AdaptiveDecisions
```

### 4.2 Complete SQL Schema

```sql
-- ============================================
-- CORE TABLES
-- ============================================

-- Students Table
CREATE TABLE Students (
    StudentID INT PRIMARY KEY IDENTITY(1,1),
    Email NVARCHAR(255) NOT NULL UNIQUE,
    PasswordHash NVARCHAR(255) NOT NULL,
    Nickname NVARCHAR(100),
    FirstName NVARCHAR(100),
    LastName NVARCHAR(100),
    PlacementLevel INT DEFAULT 2, -- 1=Beginner, 2=Intermediate, 3=Advanced
    FinalTheta FLOAT DEFAULT 0.0, -- IRT ability estimate
    TotalXP INT DEFAULT 0,
    StreakDays INT DEFAULT 0,
    LastLoginDate DATETIME,
    CreatedDate DATETIME DEFAULT GETDATE(),
    IsActive BIT DEFAULT 1,
    CONSTRAINT CK_PlacementLevel CHECK (PlacementLevel BETWEEN 1 AND 3)
);

-- Create indexes
CREATE INDEX IDX_Students_Email ON Students(Email);
CREATE INDEX IDX_Students_PlacementLevel ON Students(PlacementLevel);

-- ============================================
-- CURRICULUM TABLES
-- ============================================

-- Modules Table (5 MATATAG modules)
CREATE TABLE Modules (
    ModuleID INT PRIMARY KEY IDENTITY(1,1),
    ModuleName NVARCHAR(255) NOT NULL,
    ModuleDomain NVARCHAR(100) NOT NULL, -- 'Phonics', 'Vocabulary', 'Grammar', 'Comprehension', 'Writing'
    ModuleLevel INT DEFAULT 1,
    Priority INT DEFAULT 0, -- Lower = higher priority
    Description NVARCHAR(MAX),
    IconURL NVARCHAR(500),
    IsActive BIT DEFAULT 1,
    CreatedDate DATETIME DEFAULT GETDATE()
);

-- Nodes Table (13 per module: 12 lessons + 1 final assessment)
CREATE TABLE Nodes (
    NodeID INT PRIMARY KEY IDENTITY(1,1),
    ModuleID INT NOT NULL FOREIGN KEY REFERENCES Modules(ModuleID),
    NodeType NVARCHAR(50) NOT NULL, -- 'CORE_LESSON' or 'FINAL_ASSESSMENT'
    NodeNumber INT NOT NULL, -- 1-13
    Quarter INT, -- 1-4 (NULL for final assessment)
    LessonTitle NVARCHAR(255) NOT NULL,
    LearningObjectives NVARCHAR(MAX),
    ContentJSON NVARCHAR(MAX), -- Lesson content as JSON
    SkillCategory NVARCHAR(100),
    EstimatedDuration INT DEFAULT 15, -- minutes
    XPReward INT DEFAULT 50,
    IsActive BIT DEFAULT 1,
    CreatedDate DATETIME DEFAULT GETDATE(),
    CONSTRAINT CK_NodeNumber CHECK (NodeNumber BETWEEN 1 AND 13),
    CONSTRAINT CK_Quarter CHECK (Quarter IS NULL OR Quarter BETWEEN 1 AND 4)
);

CREATE INDEX IDX_Nodes_Module ON Nodes(ModuleID, NodeNumber);

-- ============================================
-- IRT ASSESSMENT TABLES
-- ============================================

-- Assessment Items (Question bank for IRT placement test)
CREATE TABLE AssessmentItems (
    ItemID INT PRIMARY KEY IDENTITY(1,1),
    Category NVARCHAR(100) NOT NULL, -- 'Phonics and Word Study', 'Vocabulary and Word Knowledge', etc.
    Subcategory NVARCHAR(100),
    QuestionType NVARCHAR(50) NOT NULL, -- 'multiple_choice', 'pronunciation', 'reading'
    QuestionText NVARCHAR(MAX) NOT NULL,
    ReadingPassage NVARCHAR(MAX), -- For reading comprehension
    OptionA NVARCHAR(500),
    OptionB NVARCHAR(500),
    OptionC NVARCHAR(500),
    OptionD NVARCHAR(500),
    CorrectAnswer NVARCHAR(10), -- 'A', 'B', 'C', or 'D'
    Difficulty FLOAT NOT NULL, -- IRT parameter 'b' (-3 to 3)
    Discrimination FLOAT NOT NULL, -- IRT parameter 'a' (0.5 to 2.5)
    Guessing FLOAT DEFAULT 0.25, -- IRT parameter 'c' (usually 0.25 for 4-option MC)
    MinimumAccuracy INT DEFAULT 70, -- For pronunciation questions
    TimesUsed INT DEFAULT 0,
    TimesCorrect INT DEFAULT 0,
    IsActive BIT DEFAULT 1,
    CreatedDate DATETIME DEFAULT GETDATE(),
    CONSTRAINT CK_Difficulty CHECK (Difficulty BETWEEN -3 AND 3),
    CONSTRAINT CK_Discrimination CHECK (Discrimination BETWEEN 0 AND 3)
);

CREATE INDEX IDX_Items_Category ON AssessmentItems(Category, IsActive);
CREATE INDEX IDX_Items_Difficulty ON AssessmentItems(Difficulty);

-- Assessment Sessions
CREATE TABLE AssessmentSessions (
    SessionID INT PRIMARY KEY IDENTITY(1,1),
    StudentID INT NOT NULL FOREIGN KEY REFERENCES Students(StudentID),
    AssessmentType NVARCHAR(50) NOT NULL, -- 'PreAssessment' or 'PostAssessment'
    StartTime DATETIME NOT NULL DEFAULT GETDATE(),
    EndTime DATETIME,
    InitialTheta FLOAT DEFAULT 0.0,
    FinalTheta FLOAT,
    TotalQuestions INT DEFAULT 0,
    CorrectAnswers INT DEFAULT 0,
    AccuracyPercentage FLOAT,
    PlacementLevel INT, -- 1, 2, or 3
    IsCompleted BIT DEFAULT 0,
    CONSTRAINT CK_SessionPlacementLevel CHECK (PlacementLevel IS NULL OR PlacementLevel BETWEEN 1 AND 3)
);

CREATE INDEX IDX_Sessions_Student ON AssessmentSessions(StudentID, AssessmentType);

-- Student Responses (IRT answer tracking)
CREATE TABLE StudentResponses (
    ResponseID INT PRIMARY KEY IDENTITY(1,1),
    SessionID INT NOT NULL FOREIGN KEY REFERENCES AssessmentSessions(SessionID),
    ItemID INT NOT NULL FOREIGN KEY REFERENCES AssessmentItems(ItemID),
    StudentResponse NVARCHAR(500), -- 'A', 'B', 'C', 'D', or pronunciation text
    IsCorrect BIT NOT NULL,
    ResponseTime INT, -- seconds
    ThetaAtTime FLOAT, -- Theta estimate when question was answered
    ThetaAfter FLOAT, -- Theta after update
    ItemDifficulty FLOAT, -- Snapshot of item difficulty
    ItemDiscrimination FLOAT, -- Snapshot of item discrimination
    ResponseTimestamp DATETIME DEFAULT GETDATE(),

    -- Pronunciation-specific fields
    PronunciationAccuracy INT, -- 0-100
    PronunciationScore FLOAT,
    FluencyScore FLOAT,
    CompletenessScore FLOAT,
    RecognizedText NVARCHAR(MAX),
    AudioFileURL NVARCHAR(500)
);

CREATE INDEX IDX_Responses_Session ON StudentResponses(SessionID);
CREATE INDEX IDX_Responses_Item ON StudentResponses(ItemID);

-- ============================================
-- LEARNING PROGRESS TABLES
-- ============================================

-- Student Node Progress (Tracks 3-phase completion)
CREATE TABLE StudentNodeProgress (
    ProgressID INT PRIMARY KEY IDENTITY(1,1),
    StudentID INT NOT NULL FOREIGN KEY REFERENCES Students(StudentID),
    NodeID INT NOT NULL FOREIGN KEY REFERENCES Nodes(NodeID),
    NodeState NVARCHAR(50) DEFAULT 'LOCKED', -- 'LOCKED', 'UNLOCKED', 'IN_PROGRESS', 'COMPLETED', 'MASTERED'

    -- Phase completion flags
    LessonCompleted BIT DEFAULT 0,
    GameCompleted BIT DEFAULT 0,
    QuizCompleted BIT DEFAULT 0,

    -- Quiz scores
    LatestQuizScore INT, -- 0-100
    BestQuizScore INT,
    AverageQuizScore FLOAT,
    AttemptCount INT DEFAULT 0,

    -- Timestamps
    StartedDate DATETIME,
    CompletedDate DATETIME,
    LastAccessedDate DATETIME,

    -- Performance tracking
    TimeSpentSeconds INT DEFAULT 0,
    XPEarned INT DEFAULT 0,

    CONSTRAINT UQ_StudentNode UNIQUE(StudentID, NodeID),
    CONSTRAINT CK_NodeState CHECK (NodeState IN ('LOCKED', 'UNLOCKED', 'IN_PROGRESS', 'COMPLETED', 'MASTERED'))
);

CREATE INDEX IDX_Progress_Student ON StudentNodeProgress(StudentID, NodeState);
CREATE INDEX IDX_Progress_Node ON StudentNodeProgress(NodeID);

-- Quiz Attempts
CREATE TABLE QuizAttempts (
    AttemptID INT PRIMARY KEY IDENTITY(1,1),
    StudentID INT NOT NULL FOREIGN KEY REFERENCES Students(StudentID),
    NodeID INT NOT NULL FOREIGN KEY REFERENCES Nodes(NodeID),
    Score INT NOT NULL, -- 0-100
    TotalQuestions INT NOT NULL,
    CorrectAnswers INT NOT NULL,
    TimeSpentSeconds INT,
    Passed BIT NOT NULL, -- TRUE if score >= 70%
    AttemptDate DATETIME DEFAULT GETDATE(),
    AnswersJSON NVARCHAR(MAX), -- Store answers as JSON
    CONSTRAINT CK_QuizScore CHECK (Score BETWEEN 0 AND 100)
);

CREATE INDEX IDX_QuizAttempts_Student ON QuizAttempts(StudentID, NodeID);

-- Game Results
CREATE TABLE GameResults (
    ResultID INT PRIMARY KEY IDENTITY(1,1),
    StudentID INT NOT NULL FOREIGN KEY REFERENCES Students(StudentID),
    NodeID INT FOREIGN KEY REFERENCES Nodes(NodeID),
    GameType NVARCHAR(100) NOT NULL, -- 'PhonicsNinja', 'SynonymSprint', 'WordExplosion', etc.
    Score INT NOT NULL,
    MaxCombo INT DEFAULT 0,
    Accuracy FLOAT,
    TimeSpentSeconds INT,
    XPEarned INT DEFAULT 0,
    PlayedDate DATETIME DEFAULT GETDATE(),
    GameDataJSON NVARCHAR(MAX) -- Additional game-specific data
);

CREATE INDEX IDX_GameResults_Student ON GameResults(StudentID, GameType);

-- ============================================
-- ADAPTIVE SYSTEM TABLES
-- ============================================

-- Adaptive Decisions (Branching logic)
CREATE TABLE AdaptiveDecisions (
    DecisionID INT PRIMARY KEY IDENTITY(1,1),
    StudentID INT NOT NULL FOREIGN KEY REFERENCES Students(StudentID),
    NodeID INT NOT NULL FOREIGN KEY REFERENCES Nodes(NodeID),
    DecisionType NVARCHAR(50) NOT NULL, -- 'PROCEED', 'ADD_SUPPLEMENTAL', 'ADD_INTERVENTION', 'OFFER_ENRICHMENT'
    QuizScore INT,
    PlacementLevel INT,
    AttemptCount INT,
    Context NVARCHAR(MAX), -- JSON with additional context (trends, category scores, etc.)
    DecisionDate DATETIME DEFAULT GETDATE(),
    CONSTRAINT CK_DecisionType CHECK (DecisionType IN ('PROCEED', 'ADD_SUPPLEMENTAL', 'ADD_INTERVENTION', 'OFFER_ENRICHMENT', 'SKIP_AHEAD'))
);

CREATE INDEX IDX_Decisions_Student ON AdaptiveDecisions(StudentID, NodeID);

-- Supplemental Nodes (Dynamic support nodes)
CREATE TABLE SupplementalNodes (
    SupplementalNodeID INT PRIMARY KEY IDENTITY(1,1),
    ParentNodeID INT NOT NULL FOREIGN KEY REFERENCES Nodes(NodeID),
    NodeType NVARCHAR(50) NOT NULL, -- 'SUPPLEMENTAL', 'INTERVENTION', 'ENRICHMENT'
    Title NVARCHAR(255) NOT NULL,
    ContentJSON NVARCHAR(MAX),
    TriggerLogic NVARCHAR(500), -- e.g., 'quiz_score < 70'
    EstimatedDuration INT DEFAULT 10,
    XPReward INT DEFAULT 25,
    IsActive BIT DEFAULT 1,
    CreatedDate DATETIME DEFAULT GETDATE()
);

-- ============================================
-- GAMIFICATION TABLES
-- ============================================

-- XP History
CREATE TABLE XPHistory (
    XPHistoryID INT PRIMARY KEY IDENTITY(1,1),
    StudentID INT NOT NULL FOREIGN KEY REFERENCES Students(StudentID),
    XPEarned INT NOT NULL,
    Source NVARCHAR(100) NOT NULL, -- 'LESSON_COMPLETE', 'GAME_WIN', 'QUIZ_PASS', 'BADGE_UNLOCK'
    SourceID INT, -- NodeID, GameResultID, etc.
    Description NVARCHAR(255),
    EarnedDate DATETIME DEFAULT GETDATE()
);

CREATE INDEX IDX_XPHistory_Student ON XPHistory(StudentID);

-- Badges
CREATE TABLE Badges (
    BadgeID INT PRIMARY KEY IDENTITY(1,1),
    BadgeName NVARCHAR(100) NOT NULL UNIQUE,
    BadgeDescription NVARCHAR(500),
    BadgeIcon NVARCHAR(500),
    XPReward INT DEFAULT 100,
    Criteria NVARCHAR(MAX), -- JSON describing unlock criteria
    Rarity NVARCHAR(50), -- 'COMMON', 'RARE', 'EPIC', 'LEGENDARY'
    IsActive BIT DEFAULT 1
);

-- Student Badges (Many-to-many)
CREATE TABLE StudentBadges (
    StudentBadgeID INT PRIMARY KEY IDENTITY(1,1),
    StudentID INT NOT NULL FOREIGN KEY REFERENCES Students(StudentID),
    BadgeID INT NOT NULL FOREIGN KEY REFERENCES Badges(BadgeID),
    EarnedDate DATETIME DEFAULT GETDATE(),
    CONSTRAINT UQ_StudentBadge UNIQUE(StudentID, BadgeID)
);

-- Streak Tracking
CREATE TABLE StreakLog (
    StreakLogID INT PRIMARY KEY IDENTITY(1,1),
    StudentID INT NOT NULL FOREIGN KEY REFERENCES Students(StudentID),
    LoginDate DATE NOT NULL,
    StreakCount INT NOT NULL,
    XPEarned INT DEFAULT 0,
    CONSTRAINT UQ_StudentLoginDate UNIQUE(StudentID, LoginDate)
);

CREATE INDEX IDX_Streak_Student ON StreakLog(StudentID, LoginDate DESC);

-- ============================================
-- QUIZ QUESTIONS TABLE
-- ============================================

CREATE TABLE QuizQuestions (
    QuestionID INT PRIMARY KEY IDENTITY(1,1),
    NodeID INT NOT NULL FOREIGN KEY REFERENCES Nodes(NodeID),
    QuestionText NVARCHAR(MAX) NOT NULL,
    OptionA NVARCHAR(500),
    OptionB NVARCHAR(500),
    OptionC NVARCHAR(500),
    OptionD NVARCHAR(500),
    CorrectAnswer NVARCHAR(10) NOT NULL, -- 'A', 'B', 'C', or 'D'
    Difficulty INT DEFAULT 2, -- 1=Easy, 2=Medium, 3=Hard
    Points INT DEFAULT 10,
    IsActive BIT DEFAULT 1,
    CONSTRAINT CK_QuizDifficulty CHECK (Difficulty BETWEEN 1 AND 3)
);

CREATE INDEX IDX_QuizQuestions_Node ON QuizQuestions(NodeID);
```

### 4.3 Sample Data Insertion

```sql
-- Insert Modules
INSERT INTO Modules (ModuleName, ModuleDomain, Priority, Description) VALUES
('Phonics and Word Study', 'Phonics', 1, 'Learn letter sounds and word patterns'),
('Vocabulary and Word Knowledge', 'Vocabulary', 2, 'Expand your word knowledge'),
('Grammar Awareness and Grammatical Structures', 'Grammar', 3, 'Master grammar rules'),
('Comprehending and Analyzing Text', 'Comprehension', 4, 'Understand what you read'),
('Creating and Composing Text', 'Writing', 5, 'Express yourself through writing');

-- Insert Nodes for Module 1 (Phonics)
INSERT INTO Nodes (ModuleID, NodeType, NodeNumber, Quarter, LessonTitle, LearningObjectives, XPReward) VALUES
(1, 'CORE_LESSON', 1, 1, 'CVC and CVCC Patterns', 'Identify and read CVCC words', 50),
(1, 'CORE_LESSON', 2, 1, 'CCVC Patterns', 'Recognize CCVC word structures', 50),
(1, 'CORE_LESSON', 3, 1, 'Blends and Digraphs', 'Distinguish consonant blends', 50),
(1, 'CORE_LESSON', 4, 2, 'Long Vowel Sounds', 'Identify long vowel patterns', 50),
-- ... (continue for all 13 nodes)
(1, 'FINAL_ASSESSMENT', 13, NULL, 'Phonics Mastery Test', 'Demonstrate phonics mastery', 100);

-- Insert Sample IRT Items
INSERT INTO AssessmentItems (Category, Subcategory, QuestionType, QuestionText,
    OptionA, OptionB, OptionC, OptionD, CorrectAnswer, Difficulty, Discrimination) VALUES
('Phonics and Word Study', 'Phonics', 'multiple_choice', 'Which word has the CVCC pattern?',
    'cat', 'jump', 'trip', 'run', 'B', 0.3, 1.2),
('Vocabulary and Word Knowledge', 'Vocabulary', 'multiple_choice', 'What is a synonym for "happy"?',
    'sad', 'joyful', 'angry', 'tired', 'B', -0.2, 1.5),
-- ... (add 100+ items)
```

---

## 5. IRT (Item Response Theory) System

### 5.1 Mathematical Foundation

**IRT** estimates student ability (θ - theta) using the **3-Parameter Logistic Model (3PL)**:

#### **Formula:**

```
P(θ) = c + (1 - c) / (1 + e^(-a(θ - b)))

Where:
- P(θ) = Probability of correct answer
- θ (theta) = Student ability (-3 to 3)
- a = Item discrimination (0.5 to 2.5) - how well item separates abilities
- b = Item difficulty (-3 to 3) - easier items have lower b
- c = Guessing parameter (0.25 for 4-option MC)
- e = Euler's number (2.71828...)
```

#### **Example Calculation:**

Given:
- Student ability: θ = 0.5
- Item difficulty: b = 0.3
- Item discrimination: a = 1.2
- Guessing: c = 0.25

```
Step 1: Calculate exponent
exponent = -a(θ - b) = -1.2(0.5 - 0.3) = -0.24

Step 2: Calculate probability
P(0.5) = 0.25 + (1 - 0.25) / (1 + e^(-0.24))
P(0.5) = 0.25 + 0.75 / (1 + 0.787)
P(0.5) = 0.25 + 0.75 / 1.787
P(0.5) = 0.25 + 0.420
P(0.5) = 0.670 or 67% chance of correct answer
```

### 5.2 Theta Update Algorithm

After each answer, theta is updated using **gradient ascent**:

```
Δθ = α × a × (y - P(θ))

Where:
- α = Learning rate (0.3)
- a = Item discrimination
- y = Observed response (1 if correct, 0 if incorrect)
- P(θ) = Expected probability of correct answer

Updated theta:
θ_new = θ_old + Δθ

Bounded to [-3, 3]:
θ_new = max(-3, min(3, θ_new))
```

#### **Example Theta Update:**

```
Initial state:
θ = 0.0 (neutral starting point)

Question 1:
- Difficulty b = -0.5 (easy)
- Discrimination a = 1.0
- Student answers CORRECT (y = 1)

Calculate P(θ):
P(0.0) = 0.25 + 0.75 / (1 + e^(-1.0(0.0 - (-0.5))))
P(0.0) = 0.25 + 0.75 / (1 + e^(-0.5))
P(0.0) = 0.25 + 0.75 / 1.649
P(0.0) = 0.705

Calculate error:
error = y - P(θ) = 1.0 - 0.705 = 0.295

Calculate Δθ:
Δθ = 0.3 × 1.0 × 0.295 = 0.089

Update theta:
θ_new = 0.0 + 0.089 = 0.089

Student's ability increased from 0.0 to 0.089!
```

### 5.3 Question Selection Strategy

Use **Maximum Fisher Information**:

```
I(θ) = a² × P(θ) × (1 - P(θ))

Select question with highest I(θ) at current theta.
```

#### **Example:**

Student θ = 0.2

Question A: a = 1.0, b = 0.3
- P(0.2) = 0.475
- I(0.2) = 1.0² × 0.475 × 0.525 = 0.249

Question B: a = 1.5, b = -1.5
- P(0.2) = 0.930
- I(0.2) = 1.5² × 0.930 × 0.070 = 0.146

**Question A selected** (higher information = 0.249)

### 5.4 Placement Level Calculation

```java
if (theta < -0.5) {
    return 1; // Beginner
} else if (theta < 0.5) {
    return 2; // Intermediate
} else {
    return 3; // Advanced
}
```

**Theta Interpretation:**
```
θ < -0.5     → Beginner     (struggles with grade-level questions)
-0.5 ≤ θ < 0.5 → Intermediate (average performance)
θ ≥ 0.5      → Advanced    (excels at grade-level questions)
```

### 5.5 IRT Implementation Pseudocode

```python
class IRTEngine:
    def __init__(self):
        self.theta = 0.0
        self.learning_rate = 0.3

    def calculate_probability(self, item):
        """Calculate P(θ) - probability of correct answer"""
        exponent = -item.a * (self.theta - item.b)
        probability = item.c + (1 - item.c) / (1 + exp(exponent))
        return probability

    def calculate_information(self, item):
        """Calculate Fisher Information I(θ)"""
        p = self.calculate_probability(item)
        q = 1 - p
        information = item.a ** 2 * p * q
        return information

    def select_next_item(self, available_items):
        """Select item with maximum information"""
        max_info = 0
        best_item = None

        for item in available_items:
            info = self.calculate_information(item)
            if info > max_info:
                max_info = info
                best_item = item

        return best_item

    def update_theta(self, item, is_correct):
        """Update theta after answer"""
        # Calculate expected probability
        p = self.calculate_probability(item)

        # Calculate error
        y = 1.0 if is_correct else 0.0
        error = y - p

        # Calculate delta theta
        delta_theta = self.learning_rate * item.a * error

        # Update theta with bounds
        self.theta = max(-3.0, min(3.0, self.theta + delta_theta))

    def calculate_placement_level(self):
        """Convert theta to placement level"""
        if self.theta < -0.5:
            return 1  # Beginner
        elif self.theta < 0.5:
            return 2  # Intermediate
        else:
            return 3  # Advanced
```

### 5.6 IRT Test Flow

```
1. Initialize: θ = 0.0

2. For each of 25 questions (5 per category):
   a. Select question with max information I(θ)
   b. Present question to student
   c. Student answers
   d. Submit answer to API
   e. API checks correctness
   f. Update theta: θ_new = θ_old + Δθ
   g. Log response to StudentResponses table

3. Calculate final placement:
   - Final θ = 0.65
   - Level = 3 (Advanced)

4. Calculate category scores:
   - Phonics: 80% (4/5 correct)
   - Vocabulary: 60% (3/5 correct)
   - Grammar: 70% (3.5/5 weighted)
   - Comprehension: 65%
   - Writing: 55%

5. Save to database:
   - UPDATE Students SET PlacementLevel = 3, FinalTheta = 0.65
   - INSERT INTO AssessmentSessions (...)
   - Return results to app
```

---

*This is Part 1 of the documentation. Continue to next sections...*
