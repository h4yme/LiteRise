# Adaptive Module System - Implementation Guide

## Overview

This implementation uses **Option A**: IRT for Placement Only, Performance-Based Modules

- **IRT** is used ONLY for initial placement test (classify students into Beginner/Intermediate/Advanced)
- **Module progression** uses simple performance metrics (quiz scores, attempts, trends)
- **Curriculum remains locked** - adaptivity is in delivery, pacing, and branching only

---

## System Architecture

### Module Structure

Each module contains **13 nodes**:
- **12 Core Lesson Nodes** (organized in 4 quarters, 3 lessons each)
- **1 Final Assessment Node**

### Lesson Flow (LESSON → GAME → QUIZ)

Every core lesson node follows this mandatory 3-phase flow:

```
┌─────────────────────────────────────┐
│ Phase 1: LESSON                     │
│ - Content delivery (curriculum)     │
│ - Adaptive pacing based on level    │
│ - Scaffolding varies by performance │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│ Phase 2: GAME (Mandatory)           │
│ - Skill practice                    │
│ - Difficulty: EASY/MEDIUM/HARD      │
│ - Earn XP and badges                │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│ Phase 3: QUIZ (Curriculum Assessment│
│ - Fixed questions (curriculum)      │
│ - Pass threshold: 70%               │
│ - Triggers adaptive branching       │
└─────────────────────────────────────┘
```

### Adaptive Branching Logic

After quiz completion, the system makes a decision:

```
Quiz Score < 70%
  → ADD_INTERVENTION (Required support node)

Quiz Score 70-79% + Beginner
  → ADD_SUPPLEMENTAL (Additional practice)

Quiz Score 70-79% + Declining Trend
  → ADD_SUPPLEMENTAL (Preventive support)

Quiz Score 90%+ + Advanced
  → OFFER_ENRICHMENT (Optional challenge)

Otherwise
  → PROCEED (Next core lesson)
```

---

## Database Schema

### Core Tables

**Modules** - 5 MATATAG modules
- Phonics and Word Study (EN3PWS)
- Vocabulary and Word Knowledge (EN3VWK)
- Grammar Awareness and Grammatical Structures (EN3GAGS)
- Comprehending and Analyzing Text (EN3CAT)
- Creating and Composing Text (EN3CCT)

**Nodes** - 13 curriculum-locked nodes per module
- NodeType: CORE_LESSON, FINAL_ASSESSMENT
- Contains: LessonTitle, LearningObjectives, ContentJSON
- Metadata: SkillCategory, EstimatedDuration, XPReward

**SupplementalNodes** - Conditional support nodes
- NodeType: SUPPLEMENTAL, INTERVENTION, ENRICHMENT
- TriggerLogic: Score-based rules (e.g., "quiz_score < 70")
- Appears/disappears based on performance

**StudentNodeProgress** - Tracks student progress
- NodeState: LOCKED, UNLOCKED, IN_PROGRESS, COMPLETED, MASTERED
- Performance: AttemptCount, BestQuizScore, LatestQuizScore
- Phases: LessonCompleted, GameCompleted, QuizCompleted

**QuizAttempts** - Quiz performance tracking
- Score (0-100%), TotalQuestions, CorrectAnswers
- TimeSpentSeconds, Passed (boolean)
- NO theta tracking

**AdaptiveDecisions** - Decision log
- DecisionType: PROCEED, ADD_SUPPLEMENTAL, ADD_INTERVENTION, etc.
- Context: QuizScore, AttemptCount, PlacementLevel, RecentScoresTrend

---

## Java Models

### Node.java
Represents a core lesson node with 3 phases (LESSON → GAME → QUIZ)

```java
Node node = new Node();
node.setNodeType(Node.NodeType.CORE_LESSON);
node.setState(Node.NodeState.UNLOCKED);

// Check phase completion
boolean readyForQuiz = node.isReadyForQuiz(); // Lesson + Game done
int progress = node.getProgressPercentage(); // 0-100%
```

### SupplementalNode.java
Represents conditional support nodes

```java
SupplementalNode supp = new SupplementalNode();
supp.setNodeType(SupplementalNode.SupplementalType.INTERVENTION);
supp.setTriggerLogic("quiz_score < 70");
supp.setVisible(true); // Appears for this student
```

### AdaptiveDecision.java
Represents performance-based adaptive decisions

```java
AdaptiveDecision decision = engine.evaluateQuizPerformance(
    placementLevel, quizScore, attemptCount, recentScores, categoryScore
);

if (decision.needsSupport()) {
    // Add supplemental or intervention node
}
```

### PacingStrategy.java
Determines lesson delivery speed and scaffolding

```java
PacingStrategy pacing = engine.selectLessonPacing(placementLevel, recentScores);
// Returns: speed, scaffolding, examples, durationMinutes, allowReview
```

---

## AdaptiveDecisionEngine Usage

### 1. Quiz Performance Evaluation

```java
AdaptiveDecisionEngine engine = new AdaptiveDecisionEngine(context);

// After quiz completion
AdaptiveDecision decision = engine.evaluateQuizPerformance(
    1,              // placementLevel (1=Beginner, 2=Intermediate, 3=Advanced)
    65,             // quizScore (0-100%)
    1,              // attemptCount
    Arrays.asList(70, 68, 65), // recent 3 quiz scores
    55              // categoryScore from placement test
);

// Check decision
switch (decision.getDecisionType()) {
    case ADD_INTERVENTION:
        // Show intervention node (required)
        break;
    case ADD_SUPPLEMENTAL:
        // Show supplemental node (recommended)
        break;
    case OFFER_ENRICHMENT:
        // Show enrichment node (optional)
        break;
    case PROCEED:
        // Move to next core lesson
        break;
}
```

### 2. Game Difficulty Selection

```java
String difficulty = engine.selectGameDifficulty(
    2,      // placementLevel (Intermediate)
    78,     // currentQuizScore
    0       // gameAttemptCount
);
// Returns: "EASY", "MEDIUM", or "HARD"
```

### 3. Lesson Pacing Strategy

```java
PacingStrategy pacing = engine.selectLessonPacing(
    1,  // placementLevel (Beginner)
    Arrays.asList(65, 68, 70) // recent scores
);

// Use pacing to configure lesson delivery
String speed = pacing.getSpeed(); // "SLOW"
String scaffolding = pacing.getScaffolding(); // "HIGH"
int duration = pacing.getDurationMinutes(); // 15
```

---

## Migration Steps

### 1. Run Database Migration

```sql
-- Execute: database/migrations/adaptive_module_system_migration.sql
-- This creates all tables and inserts 5 MATATAG modules + 8 game types
```

### 2. Populate Nodes

Create 13 nodes for each module (12 lessons + 1 assessment):

```sql
-- Example for Module 1 (Phonics and Word Study)
INSERT INTO Nodes (ModuleID, NodeType, NodeNumber, Quarter, LessonTitle, SkillCategory, EstimatedDuration, XPReward)
VALUES
-- Quarter 1
(1, 'CORE_LESSON', 1, 1, 'Basic Sight Words', 'Phonics', 10, 20),
(1, 'CORE_LESSON', 2, 1, 'CVC Patterns', 'Phonics', 12, 20),
(1, 'CORE_LESSON', 3, 1, 'Short Vowel Sounds', 'Phonics', 10, 20),
-- ... continue for all 12 lessons
-- Quarter 4 Final Assessment
(1, 'FINAL_ASSESSMENT', 13, NULL, 'Module 1 Mastery Assessment', 'Phonics', 20, 50);
```

### 3. Add Quiz Questions

```sql
INSERT INTO QuizQuestions (NodeID, QuestionText, QuestionType, OptionsJSON, CorrectAnswer, EstimatedDifficulty, SkillCategory)
VALUES
(1, 'Which word is a sight word?', 'multiple_choice', '["cat", "the", "jump", "happy"]', 'B', 'Easy', 'Phonics'),
(1, 'Read the word: "and"', 'pronunciation', NULL, NULL, 'Easy', 'Phonics');
```

### 4. Map Games to Nodes

```sql
INSERT INTO NodeGameMapping (NodeID, GameID, OrderIndex)
VALUES
(1, 1, 1), -- Word Hunt for Node 1
(1, 4, 2); -- Minimal Pairs for Node 1
```

### 5. Create Supplemental Nodes

```sql
INSERT INTO SupplementalNodes (NodeType, AfterNodeID, TriggerLogic, Title, SkillCategory, EstimatedDuration, XPReward)
VALUES
('INTERVENTION', 1, 'quiz_score < 70', 'Sight Words Intensive Practice', 'Phonics', 15, 15),
('SUPPLEMENTAL', 1, 'quiz_score >= 70 AND quiz_score < 80 AND placement_level = 1', 'Sight Words Review', 'Phonics', 10, 10),
('ENRICHMENT', 1, 'quiz_score >= 90 AND placement_level = 3', 'Creative Writing with Sight Words', 'Phonics', 12, 25);
```

---

## API Integration (PHP)

### Submit Quiz Endpoint

```php
POST /api/quiz/submit

Request:
{
  "student_id": 123,
  "node_id": 5,
  "answers": [
    {"questionId": 10, "selectedAnswer": "B", "responseTime": 8},
    {"questionId": 11, "selectedAnswer": "A", "responseTime": 12}
  ]
}

Response:
{
  "success": true,
  "score": 72,
  "passed": true,
  "correctAnswers": 4,
  "totalQuestions": 5,
  "attemptNumber": 1,
  "adaptiveDecision": {
    "type": "ADD_SUPPLEMENTAL",
    "reason": "Beginner with borderline pass (72%) - adding support",
    "trend": "STABLE",
    "supplementalNodes": [
      {
        "supplementalNodeId": 3,
        "title": "CVC Review Practice",
        "nodeType": "SUPPLEMENTAL"
      }
    ]
  }
}
```

### Get Lesson Pacing

```php
GET /api/nodes/{nodeId}/pacing?studentId={id}

Response:
{
  "pacing": {
    "speed": "SLOW",
    "scaffolding": "HIGH",
    "examples": "MANY",
    "durationMinutes": 15,
    "allowReview": true
  }
}
```

### Get Game Difficulty

```php
GET /api/games/difficulty?studentId={id}&nodeId={id}

Response:
{
  "difficulty": "MEDIUM"
}
```

---

## Key Benefits of Option A

✅ **Simpler**: No complex IRT calculations during modules
✅ **Transparent**: Score-based decisions (70% = pass) are easy to understand
✅ **Curriculum-Aligned**: Percentage scores match traditional grading
✅ **Maintainable**: Easier to debug and adjust thresholds
✅ **Efficient**: Less computation, faster API responses
✅ **Still Adaptive**: Pacing, games, and branching all adapt to student needs
✅ **Clear Separation**: IRT for placement, performance metrics for progression

---

## Comparison: IRT vs Performance-Based

| Aspect | IRT Throughout | Performance-Based (Option A) |
|--------|---------------|------------------------------|
| **Placement Test** | ✅ IRT adaptive | ✅ IRT adaptive |
| **Module Progression** | IRT theta updates | Quiz scores % |
| **Adaptive Decisions** | Theta + Score | Score + Placement + Trends |
| **Game Difficulty** | Theta-based | Placement + Score |
| **Complexity** | High (IRT math) | Low (simple thresholds) |
| **Transparency** | Low (theta opaque) | High (scores intuitive) |
| **Database** | ThetaHistory table | Simple QuizAttempts |
| **API Overhead** | IRT calculations | Score calculations only |

---

## Next Steps

1. **Populate Database**: Add 13 nodes per module, quiz questions, supplemental nodes
2. **Update UI**: Modify ModuleLadderActivity to display 13 nodes with quarters
3. **Implement 3-Phase Flow**: Update lesson activity to enforce LESSON → GAME → QUIZ
4. **API Integration**: Create quiz submission endpoint that returns adaptive decisions
5. **Testing**: Test all branching scenarios (intervention, supplemental, enrichment)

---

## Support

For questions or issues:
- Review this documentation
- Check database schema in `migrations/adaptive_module_system_migration.sql`
- Examine Java models in `models/` directory
- Reference `AdaptiveDecisionEngine.java` for decision logic
