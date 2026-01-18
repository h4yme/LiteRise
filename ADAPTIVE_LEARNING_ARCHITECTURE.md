# 🎓 LiteRise Adaptive Learning System - Architecture Design

**Date:** January 15, 2026
**Version:** 2.0 - Adaptive IRT-Driven System
**Status:** Planning Phase

---

## 🎯 Vision Overview

Transform LiteRise from a static lesson progression to an **adaptive, personalized learning path** that responds to student performance in real-time using IRT (Item Response Theory) assessment results.

---

## 📐 New Module Structure

### Current vs. New Structure

| Aspect | Current (v1.0) | New (v2.0) |
|--------|---------------|-----------|
| **Nodes per module** | 15 lessons | 13 nodes (12 lessons + 1 assessment) |
| **Lesson distribution** | Linear progression | 3 lessons per quarter (Q1-Q4) |
| **Progression** | Static, always same path | Adaptive based on performance |
| **Branching** | None | Dynamic supplemental/intervention |
| **Assessment** | Optional quizzes | Mandatory at lesson end + module assessment |
| **Content type** | Static reading | Interactive + Quiz + Game |

---

## 🌳 Module Node Structure (13 Nodes)

```
Module: Phonics and Word Study
├─ Quarter 1 (Lessons 1-3)
│  ├─ Lesson 1: Sight Words Basics
│  ├─ Lesson 2: CVC Words
│  └─ Lesson 3: Word Families
├─ Quarter 2 (Lessons 4-6)
│  ├─ Lesson 4: Consonant Blends
│  ├─ Lesson 5: Digraphs
│  └─ Lesson 6: Long Vowels
├─ Quarter 3 (Lessons 7-9)
│  ├─ Lesson 7: R-Controlled Vowels
│  ├─ Lesson 8: Diphthongs
│  └─ Lesson 9: Silent Letters
├─ Quarter 4 (Lessons 10-12)
│  ├─ Lesson 10: Multisyllabic Words
│  ├─ Lesson 11: Prefixes & Suffixes
│  └─ Lesson 12: Complex Patterns
└─ Module Assessment (Node 13)
```

---

## 🔀 Adaptive Branching System

### 3-Tier Proficiency Levels

#### 1️⃣ **Beginner (Struggling Learners)**

**IRT Threshold:** θ < -1.0 (Below grade level)

**Branching Strategy:**
- **Intervention lessons** appear before regular lessons
- **Supplemental practice** unlocks after poor quiz performance (<60%)
- **Slower pacing** with more scaffolding

**Example Path for Juan (Beginner):**
```
START (IRT: θ = -1.5, Beginner)
  ↓
Phonics Module Detected as Priority
  ↓
Lesson 1: Sight Words Basics
  ├─ Interactive Content (simplified)
  ├─ Quiz (Score: 45% ❌)
  └─ [BRANCH UNLOCKS] → Intervention: Sight Words Foundations
      ├─ Extra Practice Activities
      ├─ Simpler Examples
      ├─ Guided Practice Quiz
      └─ Return to Lesson 1 Quiz (Retry)
  ↓
After Intervention (Score: 75% ✅)
  ↓
Game: Word Hunt (Beginner Mode)
  ↓
Lesson 2 Unlocked
```

**Visual Representation:**
```
[Lesson 1] ────────────────────> [Lesson 2]
    │
    │ (Quiz < 60%)
    ↓
[Intervention 1A]
[Intervention 1B]
    │
    │ (Retry Quiz)
    └──────────────────────────> [Lesson 2]
```

#### 2️⃣ **Intermediate (On Grade Level)**

**IRT Threshold:** -1.0 ≤ θ ≤ 1.0 (Grade appropriate)

**Branching Strategy:**
- Standard lesson flow
- **Optional enrichment** if quiz score > 85%
- **Light support** if quiz score 60-70%

**Example Path for Maria (Intermediate):**
```
START (IRT: θ = 0.2, Intermediate)
  ↓
Lesson 1: Sight Words Basics
  ├─ Interactive Content (standard)
  ├─ Quiz (Score: 88% ✅)
  └─ [OPTIONAL BRANCH] → Enrichment: Advanced Sight Words
      └─ Challenge Activities
  ↓
Game: Word Hunt (Standard Mode)
  ↓
Lesson 2 Unlocked
```

#### 3️⃣ **Advanced (Above Grade Level)**

**IRT Threshold:** θ > 1.0 (Advanced)

**Branching Strategy:**
- **Fast-track option** to skip mastered content
- **Key Stage 2 preview** lessons unlock
- **Challenge mode** games with higher difficulty

**Example Path for Alex (Advanced):**
```
START (IRT: θ = 1.8, Advanced)
  ↓
Lesson 1: Sight Words Basics
  ├─ Interactive Content (accelerated)
  ├─ Quiz (Score: 95% ✅)
  └─ [BRANCH UNLOCKS] → KS2 Preview: Complex Vocabulary
      └─ Introduction to Grade 4+ words
  ↓
Game: Word Hunt (Expert Mode - Timed Challenge)
  ↓
[OPTION] Skip Lesson 2 (if mastery detected)
  ↓
Lesson 3 Unlocked (or Lesson 2 if needed)
```

---

## 📚 Inside Each Lesson Node Structure

### Old Structure (Boring ❌):
```
Lesson 1
├─ Content Tab (static reading)
├─ Practice Tab (10 questions)
└─ Quiz Tab (5 questions)
```

### New Structure (Engaging ✅):
```
Lesson 1
├─ 1️⃣ Interactive Content (10-15 min)
│   ├─ Animated introduction
│   ├─ Interactive examples
│   ├─ Video demonstrations
│   ├─ Drag-and-drop activities
│   └─ Real-time feedback
│
├─ 2️⃣ Knowledge Check Quiz (5-10 questions)
│   ├─ Adaptive difficulty
│   ├─ Performance tracking
│   ├─ Immediate feedback
│   └─ Branching logic:
│       • Score < 60% → Unlock Intervention
│       • Score 60-84% → Proceed to Game
│       • Score ≥ 85% → Unlock Enrichment + Game
│
└─ 3️⃣ Reward Game (5-10 min)
    ├─ XP & Badge rewards
    ├─ Fun gameplay (Word Hunt, Scramble, etc.)
    ├─ Reinforces lesson concepts
    └─ Difficulty adapts to proficiency level
```

---

## 🎮 Performance-Based Branching Logic

### Decision Tree for Lesson Completion

```
Student completes Interactive Content
    ↓
Takes Knowledge Check Quiz
    ↓
    ├─ Score < 60% (Struggling)
    │   ├─ Lock next lesson
    │   ├─ Show intervention branch
    │   ├─ Required: Complete intervention
    │   └─ Retry quiz (must score ≥ 60%)
    │
    ├─ Score 60-84% (Adequate)
    │   ├─ Unlock game
    │   ├─ Proceed to next lesson after game
    │   └─ No additional branches
    │
    └─ Score ≥ 85% (Excellent)
        ├─ Unlock game + enrichment
        ├─ Show optional challenge content
        └─ Proceed to next lesson
```

### Thresholds & Actions

| Quiz Score | Proficiency | Action | Next Steps |
|-----------|-------------|--------|------------|
| 0-59% | Struggling | 🔒 Lock progression | Mandatory intervention |
| 60-69% | Needs practice | ⚠️ Proceed with caution | Light support offered |
| 70-84% | Adequate | ✅ Proceed | Standard progression |
| 85-94% | Strong | 🌟 Proceed + optional enrichment | Challenge activities unlocked |
| 95-100% | Mastery | 🏆 Fast-track option | Skip similar content |

---

## 🗄️ Database Architecture Options

### Option 1: **Firebase Realtime Database** ⭐ RECOMMENDED

**Pros:**
- Real-time sync across devices
- Built-in authentication
- NoSQL flexibility for adaptive structures
- Offline support (local caching)
- Free tier generous (up to 1GB storage)
- Easy to implement in Android

**Cons:**
- Requires internet connection for initial sync
- Data structure can get complex
- Query limitations compared to SQL

**Structure Example:**
```json
{
  "students": {
    "student123": {
      "name": "Juan Dela Cruz",
      "irtScore": -1.5,
      "proficiencyLevel": "beginner",
      "modules": {
        "module1": {
          "moduleId": 1,
          "lessons": {
            "lesson1": {
              "lessonId": 101,
              "status": "completed",
              "quizScore": 75,
              "gameScore": 850,
              "interventionCompleted": true,
              "branches": {
                "intervention1A": "completed",
                "intervention1B": "completed"
              },
              "attempts": 2,
              "xpEarned": 45,
              "timestamp": 1705315200000
            }
          },
          "currentLesson": 2,
          "progressPercent": 8.3
        }
      }
    }
  },

  "lessons": {
    "101": {
      "lessonId": 101,
      "moduleId": 1,
      "quarter": 1,
      "lessonNumber": 1,
      "title": "Sight Words Basics",
      "gameType": "word_hunt",
      "prerequisites": [],
      "branches": {
        "beginner": ["intervention_101a", "intervention_101b"],
        "advanced": ["enrichment_101"]
      },
      "quizThresholds": {
        "intervention": 60,
        "enrichment": 85
      }
    }
  },

  "interventions": {
    "intervention_101a": {
      "id": "intervention_101a",
      "parentLesson": 101,
      "title": "Sight Words Foundations",
      "type": "intervention",
      "difficulty": "beginner",
      "requiredFor": 101
    }
  }
}
```

### Option 2: **Supabase (PostgreSQL)** 🚀 BEST FOR COMPLEX QUERIES

**Pros:**
- PostgreSQL backend (powerful queries)
- Real-time subscriptions
- Row-level security
- Built-in auth
- REST and GraphQL APIs
- Free tier: 500MB database

**Cons:**
- Requires internet
- More complex setup than Firebase
- Learning curve for PostgreSQL

**Schema Example:**
```sql
-- Students table
CREATE TABLE students (
    student_id UUID PRIMARY KEY,
    name VARCHAR(255),
    irt_score DECIMAL(5,2),
    proficiency_level VARCHAR(20),
    created_at TIMESTAMP DEFAULT NOW()
);

-- Lessons table
CREATE TABLE lessons (
    lesson_id INTEGER PRIMARY KEY,
    module_id INTEGER,
    quarter INTEGER,
    lesson_number INTEGER,
    title VARCHAR(255),
    game_type VARCHAR(50),
    quiz_threshold_intervention INTEGER DEFAULT 60,
    quiz_threshold_enrichment INTEGER DEFAULT 85
);

-- Student progress table
CREATE TABLE student_progress (
    id UUID PRIMARY KEY,
    student_id UUID REFERENCES students(student_id),
    lesson_id INTEGER REFERENCES lessons(lesson_id),
    status VARCHAR(20), -- 'locked', 'in_progress', 'completed'
    quiz_score INTEGER,
    game_score INTEGER,
    intervention_completed BOOLEAN DEFAULT FALSE,
    attempts INTEGER DEFAULT 0,
    xp_earned INTEGER DEFAULT 0,
    completed_at TIMESTAMP
);

-- Branching nodes table
CREATE TABLE lesson_branches (
    branch_id UUID PRIMARY KEY,
    parent_lesson_id INTEGER REFERENCES lessons(lesson_id),
    branch_type VARCHAR(20), -- 'intervention', 'enrichment'
    title VARCHAR(255),
    required_for_progression BOOLEAN DEFAULT FALSE,
    unlock_condition VARCHAR(100) -- e.g., 'quiz_score < 60'
);

-- Student branches completed
CREATE TABLE student_branches (
    id UUID PRIMARY KEY,
    student_id UUID REFERENCES students(student_id),
    branch_id UUID REFERENCES lesson_branches(branch_id),
    completed BOOLEAN DEFAULT FALSE,
    score INTEGER,
    completed_at TIMESTAMP
);
```

### Option 3: **Room + Cloud Sync (Hybrid)** 💾 BEST OFFLINE SUPPORT

**Pros:**
- Full offline capability
- SQLite benefits (local speed)
- Sync to cloud when online
- Data ownership

**Cons:**
- Must implement sync logic manually
- Conflict resolution needed
- More complex architecture

### 🏆 **RECOMMENDATION: Firebase**

For your use case, I recommend **Firebase** because:
1. ✅ Real-time adaptive branching works seamlessly
2. ✅ Easy to implement in Android
3. ✅ Built-in auth for students
4. ✅ Generous free tier
5. ✅ Offline support for mobile app
6. ✅ NoSQL perfect for flexible branching structure

---

## 🎨 Updated Lesson Node Visual

### Ladder View with Branching

```
                [START]
                   ↓
        ╔══════════════════╗
        ║   Lesson 1       ║  (Main path)
        ║   Sight Words    ║
        ╚══════════════════╝
              ↓   ↘
              ↓     [Intervention 1A] ─┐ (Beginner branch)
              ↓     [Intervention 1B] ─┘
              ↓
        ╔══════════════════╗
        ║   Lesson 2       ║
        ║   CVC Words      ║
        ╚══════════════════╝
              ↓   ↘
              ↓     [Enrichment 2A] ──── (Advanced branch)
              ↓
        ╔══════════════════╗
        ║   Lesson 3       ║
        ║   Word Families  ║
        ╚══════════════════╝
              ↓
             ...
              ↓
        ╔══════════════════╗
        ║   Module         ║
        ║   Assessment     ║
        ╚══════════════════╝
```

---

## 📱 Updated Data Models

### Lesson Model Enhancement

```java
public class Lesson {
    // Existing fields
    private int lessonId;
    private int moduleId;
    private int quarter; // NEW: 1-4
    private int lessonNumber; // 1-12 (not 1-15)
    private String title;
    private String gameType;

    // NEW: Branching fields
    private List<String> interventionBranches; // IDs of intervention lessons
    private List<String> enrichmentBranches; // IDs of enrichment lessons
    private List<Integer> prerequisites; // Lesson IDs that must be completed

    // NEW: Performance thresholds
    private int interventionThreshold; // Default: 60
    private int enrichmentThreshold; // Default: 85

    // NEW: Content structure
    private InteractiveContent content; // Replaces static string
    private Quiz knowledgeCheckQuiz; // 5-10 questions
    private Game rewardGame; // Game configuration
}
```

### Student Progress Model

```java
public class StudentProgress {
    private String studentId;
    private int lessonId;

    // Progress tracking
    private LessonStatus status; // LOCKED, IN_PROGRESS, QUIZ_PASSED, COMPLETED
    private int quizScore; // 0-100
    private int gameScore; // Points earned
    private int xpEarned;

    // Branching tracking
    private boolean interventionRequired;
    private List<String> completedInterventions;
    private List<String> unlockedEnrichments;

    // Attempts
    private int quizAttempts;
    private long completedTimestamp;
}
```

---

## 🔄 Implementation Phases

### Phase 1: Database Migration (Week 1-2)
- [ ] Set up Firebase project
- [ ] Design data structure
- [ ] Create data models
- [ ] Implement Firebase SDK
- [ ] Migrate existing progress data

### Phase 2: Adaptive Logic (Week 3-4)
- [ ] Build performance detection system
- [ ] Implement branching logic
- [ ] Create intervention content
- [ ] Create enrichment content
- [ ] Update progression rules

### Phase 3: UI Redesign (Week 5-6)
- [ ] Redesign ModuleLadderActivity for branching
- [ ] Create intervention node visuals
- [ ] Update lesson flow (Content → Quiz → Game)
- [ ] Add progress indicators
- [ ] Implement animations

### Phase 4: Content Creation (Week 7-8)
- [ ] Convert lessons to interactive format
- [ ] Create intervention lessons
- [ ] Create enrichment lessons
- [ ] Design adaptive quizzes
- [ ] Configure game difficulty levels

### Phase 5: Testing & Refinement (Week 9-10)
- [ ] Test all branching scenarios
- [ ] Validate IRT integration
- [ ] User testing with students
- [ ] Performance optimization
- [ ] Bug fixes

---

## ❓ Questions for Clarification

1. **Module Assessment:** Should the final assessment (Node 13) also branch based on performance?

2. **Intervention Retries:** How many times can a student retry an intervention before escalation?

3. **Fast-Tracking:** For advanced students, can they skip entire lessons or just access enrichment?

4. **Grading:** Should we show letter grades (A, B, C) or just percentages?

5. **Teacher Dashboard:** Do teachers need to see student branching paths and interventions?

6. **Offline Mode:** Should students be able to complete lessons offline and sync later?

---

## 🎯 Success Metrics

After implementation, we should track:
- **Intervention effectiveness:** % of students who pass after intervention
- **Adaptive accuracy:** IRT score correlation with actual performance
- **Engagement:** Time spent on interactive content vs. old static reading
- **Learning gains:** Pre/post assessment score improvements
- **Student satisfaction:** App ratings and feedback

---

**Next Steps:** Please review and provide feedback on:
1. Database choice (Firebase recommended)
2. Branching thresholds (60% intervention, 85% enrichment)
3. Module structure (13 nodes: 12 lessons + 1 assessment)
4. Any additional features or changes needed
