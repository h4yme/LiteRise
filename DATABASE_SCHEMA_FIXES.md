# Database Schema Fixes Required

## Issues Identified

The API endpoints are failing with "Invalid column name" errors because the database schema doesn't match what the API expects.

---

## Issue 1: `get_node_progress.php` - Missing `QuizScore` Column

**Error:**
```
SQLSTATE[42S22]: Invalid column name 'QuizScore'
```

**Current Query in API:**
```sql
SELECT
    LessonCompleted,
    GameCompleted,
    QuizCompleted,
    QuizScore,           -- ❌ This column doesn't exist
    AdaptiveDecision,
    CompletedAt
FROM StudentNodeProgress
WHERE StudentID = ? AND NodeID = ?
```

### Solution Options:

#### Option A: Add Missing Column to Database (RECOMMENDED)
```sql
ALTER TABLE StudentNodeProgress
ADD QuizScore DECIMAL(5,2) NULL;
```

#### Option B: Update API to Make QuizScore Optional
Modify `get_node_progress.php` to handle missing QuizScore column:

```php
// Change the SELECT to make QuizScore optional
$query = "SELECT
    LessonCompleted,
    GameCompleted,
    QuizCompleted,
    AdaptiveDecision,
    CompletedAt
FROM StudentNodeProgress
WHERE StudentID = ? AND NodeID = ?";

// Then in the response, set QuizScore to 0 if not available
$response = [
    'success' => true,
    'progress' => [
        'lesson_completed' => (bool)$row['LessonCompleted'],
        'game_completed' => (bool)$row['GameCompleted'],
        'quiz_completed' => (bool)$row['QuizCompleted'],
        'quiz_score' => 0.0,  // Default value since column missing
        'adaptive_decision' => $row['AdaptiveDecision'] ?? null,
        'completed_at' => $row['CompletedAt'] ?? null
    ]
];
```

---

## Issue 2: `get_lesson_content.php` - Column Name Mismatch

**Error:**
```
SQLSTATE[42S22]: Invalid column name 'LessonObjective'
```

**Current Database Schema (from `Nodes` table):**
- ✅ Has: `LearningObjectives`
- ❌ API expects: `LessonObjective`

**Also potentially missing:**
- `LessonContent` column (not visible in API responses)

**Current Query in API:**
```sql
SELECT
    n.NodeID,
    n.NodeNumber,
    n.LessonTitle,
    n.LessonObjective,    -- ❌ Should be LearningObjectives
    n.LessonContent,       -- ❌ Might not exist
    n.NodeType,
    n.Quarter,
    n.ModuleID,
    m.ModuleName
FROM Nodes n
JOIN Modules m ON n.ModuleID = m.ModuleID
WHERE n.NodeID = ?
```

### Solution Options:

#### Option A: Rename Column in Database
```sql
-- Rename existing column
EXEC sp_rename 'Nodes.LearningObjectives', 'LessonObjective', 'COLUMN';

-- Add missing content column if needed
ALTER TABLE Nodes
ADD LessonContent NVARCHAR(MAX) NULL;
```

#### Option B: Update API to Use Existing Column Names (RECOMMENDED)
Modify `get_lesson_content.php`:

```php
// Update query to use actual column names
$query = "SELECT
    n.NodeID,
    n.NodeNumber,
    n.LessonTitle,
    n.LearningObjectives as LessonObjective,  -- Use alias to match expected name
    n.ContentJSON as LessonContent,            -- Use ContentJSON if that's your content field
    n.NodeType,
    n.Quarter,
    n.ModuleID,
    m.ModuleName
FROM Nodes n
JOIN Modules m ON n.ModuleID = m.ModuleID
WHERE n.NodeID = ?";
```

---

## Current Database Schema (Based on API Response)

### `Nodes` Table
```
- NodeID (int, primary key)
- ModuleID (int)
- NodeType (varchar) - "CORE_LESSON" or "FINAL_ASSESSMENT"
- NodeNumber (int) - 1-13
- Quarter (int, nullable) - 1-4, NULL for final assessment
- LessonTitle (varchar)
- LearningObjectives (varchar)  ⚠️ API expects "LessonObjective"
- ContentJSON (text, nullable)  ⚠️ API expects "LessonContent"
- SkillCategory (varchar)
- EstimatedDuration (int)
- XPReward (int)
- IsActive (bit)
- CreatedDate (datetime)
```

### `StudentNodeProgress` Table (Expected)
```
- StudentID (int)
- NodeID (int)
- LessonCompleted (bit/varchar) - "0" or "1"
- GameCompleted (bit/varchar) - "0" or "1"
- QuizCompleted (bit/varchar) - "0" or "1"
- QuizScore (decimal, nullable)  ⚠️ MISSING - needs to be added
- AdaptiveDecision (varchar, nullable)
- CompletedAt (datetime, nullable)
```

---

## Recommended Action Plan

1. **Add QuizScore column to StudentNodeProgress:**
   ```sql
   ALTER TABLE StudentNodeProgress
   ADD QuizScore DECIMAL(5,2) NULL;
   ```

2. **Update `get_lesson_content.php` to use correct column names:**
   - Change `LessonObjective` → `LearningObjectives`
   - Use `ContentJSON` instead of `LessonContent` (or add the column if needed)

3. **Update `get_node_progress.php` to handle optional QuizScore:**
   - Either ensure column exists, or make it optional in the query

---

## Testing After Fixes

After applying fixes, test these endpoints:

1. **get_node_progress.php:**
   ```
   GET http://your-api/get_node_progress.php?student_id=28&node_id=1

   Expected: Success response with quiz_score field
   ```

2. **get_lesson_content.php:**
   ```
   GET http://your-api/get_lesson_content.php?node_id=1&placement_level=3

   Expected: Success response with lesson content and objectives
   ```

---

## App Behavior (Current)

The Android app **gracefully handles these errors**:
- ✅ When `get_node_progress` fails → Starts from lesson phase
- ✅ When `get_lesson_content` fails → Shows error in activity
- ✅ Ladder refresh still works using `get_module_ladder`

However, for full functionality, these API endpoints need to work correctly.
