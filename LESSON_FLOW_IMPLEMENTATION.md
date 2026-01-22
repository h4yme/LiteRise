# 3-Phase Lesson Flow Implementation

## Overview

The LiteRise app implements an adaptive learning flow with **3 mandatory phases** for each lesson node:

```
LESSON → GAME → QUIZ → Adaptive Decision
```

## Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    Student Clicks Node                       │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
           ┌──────────────────────┐
           │ Check Progress Status │
           └──────────┬───────────┘
                      │
        ┌─────────────┼─────────────┬──────────────┐
        │             │             │              │
        ▼             ▼             ▼              ▼
   Not Started   Lesson Done   Game Done    All Complete
        │             │             │              │
        ▼             ▼             ▼              ▼
   ┌────────┐   ┌────────┐   ┌────────┐    ┌──────────┐
   │ PHASE 1│   │ PHASE 2│   │ PHASE 3│    │  Review  │
   │ LESSON │   │  GAME  │   │  QUIZ  │    │  Options │
   └────┬───┘   └────┬───┘   └────┬───┘    └──────────┘
        │             │             │
        │             │             │
        │             │             ▼
        │             │      ┌──────────────┐
        │             │      │ Submit Quiz  │
        │             │      └──────┬───────┘
        │             │             │
        │             │             ▼
        │             │      ┌─────────────────────┐
        │             │      │ Adaptive Decision   │
        │             │      └──────┬──────────────┘
        │             │             │
        │             │      ┌──────┴──────────────────┬───────────────┐
        │             │      │                         │               │
        │             │      ▼                         ▼               ▼
        │             │  Score <70%              70-79% (Beginner)   90%+ (Advanced)
        │             │      │                         │               │
        │             │      ▼                         ▼               ▼
        │             │ ┌────────────┐          ┌─────────────┐  ┌──────────┐
        │             │ │Intervention│          │Supplemental │  │Enrichment│
        │             │ │  (Mandatory)│          │  (Optional) │  │(Optional)│
        │             │ └────────────┘          └─────────────┘  └──────────┘
        │             │
        ▼             ▼
  Mark Complete  Mark Complete
   LessonCompleted  GameCompleted
        = 1            = 1
```

## Phase Details

### PHASE 1: LESSON 📚

**File:** `LessonContentActivity.java`

**Purpose:** Display educational content with adaptive pacing

**Adaptive Pacing:**
- **Beginner (Level 1):** SLOW pacing, HIGH scaffolding
  - Detailed explanations with step-by-step guidance
  - Multiple examples with hints
  - Visual aids and mnemonics

- **Intermediate (Level 2):** MODERATE pacing, BALANCED scaffolding
  - Standard explanations
  - Moderate examples
  - Balanced support

- **Advanced (Level 3):** FAST pacing, MINIMAL scaffolding
  - Concise explanations
  - Brief examples
  - Focus on advanced concepts

**Flow:**
1. Get placement level from `SessionManager`
2. Load lesson content from API: `get_lesson_content.php`
3. Get pacing strategy: `get_pacing_strategy.php`
4. Display content with appropriate scaffolding
5. Student reads and clicks "Complete Lesson"
6. Update database: `LessonCompleted = 1`
7. Return to ModuleLadderActivity

**Key Code:**
```java
private void determinePacingStrategy() {
    switch (placementLevel) {
        case 1: // BEGINNER
            pacingSpeed = "SLOW";
            scaffoldingLevel = "HIGH";
            break;
        case 2: // INTERMEDIATE
            pacingSpeed = "MODERATE";
            scaffoldingLevel = "BALANCED";
            break;
        case 3: // ADVANCED
            pacingSpeed = "FAST";
            scaffoldingLevel = "MINIMAL";
            break;
    }
}
```

---

### PHASE 2: GAME 🎮

**Files:** Various game activities (SentenceScrambleActivity, WordHuntActivity, etc.)

**Purpose:** Reinforce learning through interactive gameplay

**Game Types:**
- **sentence_scramble** - Arrange words to form sentences
- **word_hunt** - Find words in a grid
- **timed_trail** - Speed reading challenges
- **shadow_read** - Read-along dialogues
- **minimal_pairs** - Phonics discrimination
- **traditional** - Standard lesson format

**Difficulty Adaptation:**
- Beginner: More time, hints available, simplified challenges
- Intermediate: Standard difficulty
- Advanced: Less time, no hints, complex challenges

**Flow:**
1. Check `NodeGameMapping` table for game type
2. Launch appropriate game activity
3. Pass difficulty based on `PlacementLevel`
4. Student completes game
5. Update database: `GameCompleted = 1`
6. Return to ModuleLadderActivity

**Key Code:**
```java
private void startGamePhase(int lessonId, int lessonNumber) {
    String gameType = getLessonGameType(lessonId);

    Intent intent = null;
    switch (gameType) {
        case "sentence_scramble":
            intent = new Intent(this, SentenceScrambleActivity.class);
            break;
        case "word_hunt":
            intent = new Intent(this, WordHuntActivity.class);
            break;
        // ... other games
    }

    intent.putExtra("lesson_id", lessonId);
    intent.putExtra("placement_level", placementLevel);
    startActivity(intent);
}
```

---

### PHASE 3: QUIZ ✅

**File:** `QuizActivity.java`

**Purpose:** Assess comprehension and determine adaptive branching

**Quiz Structure:**
- **Questions:** 5-10 multiple choice questions
- **Source:** `QuizQuestions` table
- **Question Types:** Comprehension, application, analysis
- **Time:** No time limit (comprehension-focused)

**Flow:**
1. Load questions from API: `get_quiz_questions.php`
2. Display questions one by one
3. Student selects answers
4. Submit to API: `submit_quiz.php`
5. Calculate score percentage
6. Determine adaptive decision
7. Award XP based on performance
8. Update database: `QuizCompleted = 1`
9. Show results in `QuizResultActivity`

**Key Code:**
```java
private String determineAdaptiveDecision(int scorePercent) {
    if (scorePercent < 70) {
        return "ADD_INTERVENTION"; // Mandatory remedial
    } else if (scorePercent >= 70 && scorePercent < 80 && placementLevel == 1) {
        return "ADD_SUPPLEMENTAL"; // Optional practice for beginners
    } else if (scorePercent >= 90 && placementLevel == 3) {
        return "OFFER_ENRICHMENT"; // Optional challenge for advanced
    } else {
        return "PROCEED"; // Normal progression
    }
}
```

---

## Adaptive Decisions

### 1. ADD_INTERVENTION (Score <70%)

**Type:** Mandatory

**Triggers:** Score below 70% regardless of placement level

**Action:**
- Unlock **Intervention Node** (from `SupplementalNodes` table)
- Student MUST complete intervention before proceeding
- Intervention content includes:
  - Simplified explanations
  - Step-by-step remediation
  - Additional practice exercises
  - Scaffolded support

**Visual Feedback:**
- 🔴 Orange/Red warning icon
- "Additional Practice Needed" message
- "Retake Quiz" button available

---

### 2. ADD_SUPPLEMENTAL (Score 70-79% + Beginner)

**Type:** Optional

**Triggers:**
- Score 70-79% AND
- Placement Level = 1 (Beginner)

**Action:**
- Unlock **Supplemental Node** (optional practice)
- Student can choose to:
  - Take supplemental practice
  - Continue to next lesson
- Supplemental content includes:
  - Additional examples
  - Practice exercises
  - Skill reinforcement

**Visual Feedback:**
- 🔵 Blue info icon
- "Supplemental Practice Available" message
- "Continue" button proceeds to next lesson

---

### 3. PROCEED (Score 80-89%)

**Type:** Normal progression

**Triggers:** Score 80-89% (most common outcome)

**Action:**
- Unlock next sequential node
- Normal progression through module
- Standard XP award (80 XP)

**Visual Feedback:**
- 🟢 Green success icon
- "Great Job!" message
- "Continue" button to next lesson

---

### 4. OFFER_ENRICHMENT (Score 90%+ + Advanced)

**Type:** Optional

**Triggers:**
- Score 90%+ AND
- Placement Level = 3 (Advanced)

**Action:**
- Unlock **Enrichment Node** (optional challenge)
- Student can choose to:
  - Take enrichment challenge
  - Continue to next lesson
- Enrichment content includes:
  - Advanced concepts
  - Critical thinking challenges
  - Extension activities

**Visual Feedback:**
- 🏆 Gold trophy icon
- "Excellent Work!" message
- "Continue" or "Challenge Yourself" options

---

## XP Award System

XP is awarded based on quiz performance:

| Score Range | XP Award | Description |
|-------------|----------|-------------|
| 90-100%     | 100 XP   | Outstanding |
| 80-89%      | 80 XP    | Great |
| 70-79%      | 60 XP    | Good |
| 60-69%      | 40 XP    | Fair |
| 0-59%       | 20 XP    | Needs improvement |

---

## Database Schema

### StudentNodeProgress Table

Tracks completion status for each phase:

```sql
CREATE TABLE StudentNodeProgress (
    ProgressID INT PRIMARY KEY,
    StudentID INT,
    NodeID INT,
    LessonCompleted BIT DEFAULT 0,
    GameCompleted BIT DEFAULT 0,
    QuizCompleted BIT DEFAULT 0,
    QuizScore DECIMAL(5,2),
    AdaptiveDecision VARCHAR(50),
    CompletedAt DATETIME,
    FOREIGN KEY (StudentID) REFERENCES Students(StudentID),
    FOREIGN KEY (NodeID) REFERENCES Nodes(NodeID)
);
```

---

## API Endpoints

### 1. `get_lesson_content.php`
**Input:** lesson_id
**Output:** Lesson content with pacing strategy

### 2. `get_pacing_strategy.php`
**Input:** lesson_id, placement_level
**Output:** Pacing speed, scaffolding level, content format

### 3. `get_quiz_questions.php`
**Input:** lesson_id
**Output:** Array of 5-10 quiz questions

### 4. `submit_quiz.php`
**Input:** lesson_id, student_id, answers[]
**Output:** Score, adaptive decision, XP awarded, unlocked nodes

### 5. `update_node_progress.php`
**Input:** student_id, node_id, phase (lesson/game/quiz)
**Output:** Success status

---

## Implementation Checklist

### ✅ Completed
- [x] ModuleLadderActivity updated with 3-phase routing logic
- [x] LessonContentActivity created (Phase 1)
- [x] QuizActivity created (Phase 3)
- [x] QuizResultActivity created
- [x] Layout files created for all activities
- [x] AndroidManifest.xml updated

### 🔄 In Progress
- [ ] API endpoints implementation
- [ ] Database queries for node progress
- [ ] SessionManager integration for placement level
- [ ] Game activities difficulty adaptation

### 📝 To Do
- [ ] Create quiz questions for all 65 nodes
- [ ] Populate lesson content database
- [ ] Test complete flow end-to-end
- [ ] Implement supplemental/intervention/enrichment content
- [ ] Add analytics tracking
- [ ] Add offline mode support

---

## Testing the Flow

### Test Scenario 1: Beginner with Low Score
1. Click Node 1 → LessonContentActivity (SLOW pacing, HIGH scaffolding)
2. Complete lesson → Return to ladder
3. Click Node 1 → SentenceScrambleActivity (EASY difficulty)
4. Complete game → Return to ladder
5. Click Node 1 → QuizActivity (5 questions)
6. Score 60% → QuizResultActivity shows "ADD_INTERVENTION"
7. Intervention node appears on ladder

### Test Scenario 2: Advanced with High Score
1. Click Node 1 → LessonContentActivity (FAST pacing, MINIMAL scaffolding)
2. Complete lesson → Return to ladder
3. Click Node 1 → TimedTrailActivity (HARD difficulty)
4. Complete game → Return to ladder
5. Click Node 1 → QuizActivity (10 questions)
6. Score 95% → QuizResultActivity shows "OFFER_ENRICHMENT"
7. Enrichment node appears on ladder (optional)

---

## Summary

The 3-phase lesson flow ensures comprehensive learning through:

1. **Content Delivery** (Lesson) - Adaptive to student level
2. **Practice** (Game) - Engaging reinforcement
3. **Assessment** (Quiz) - Adaptive branching decisions

Each phase builds on the previous one, with completion status tracked in the database. The adaptive decisions ensure struggling students get extra help, while advanced students get additional challenges.
