# Adaptive Testing Implementation Guide

## 🎯 What is Adaptive Testing?

**Adaptive Testing** (also called Computer Adaptive Testing or CAT) uses IRT to personalize each student's assessment in real-time:

- ✅ **Right answer** → Give a harder question next
- ❌ **Wrong answer** → Give an easier question next
- 🎓 **Result**: More accurate ability estimates with fewer questions

---

## 🔄 How It Works

### Traditional Fixed Assessment
```
All students get the same 20 questions in the same order
Student A (Advanced): Bored by 10 easy questions, challenged by 5 hard ones
Student B (Below Basic): Frustrated by 15 hard questions, comfortable with 5 easy ones
```

### Adaptive Assessment
```
Each student gets personalized questions matched to their ability

Student A (Advanced):
  Q1 (Medium) → ✅ Correct → Q2 (Hard) → ✅ Correct → Q3 (Very Hard) → ❌ Wrong → Q4 (Hard)...
  Final: theta = 1.8 (Advanced) with high precision

Student B (Below Basic):
  Q1 (Medium) → ❌ Wrong → Q2 (Easy) → ❌ Wrong → Q3 (Very Easy) → ✅ Correct → Q4 (Easy)...
  Final: theta = -1.2 (Below Basic) with high precision
```

---

## 📊 Benefits

| Aspect | Fixed Test | Adaptive Test |
|--------|-----------|---------------|
| **Questions needed** | 20-30 | 10-15 |
| **Precision** | Medium | High |
| **Student experience** | Some too easy/hard | Just right |
| **Testing time** | 20-30 minutes | 10-15 minutes |
| **Cheating risk** | High (same questions) | Low (unique per student) |

---

## 🛠️ Implementation Flow

### 1. **Start Session**
```http
POST /api/login.php
→ Get student's CurrentAbility

POST /api/create_session.php (or auto-create)
→ Session starts with InitialTheta = CurrentAbility
```

### 2. **Get First Item**
```http
POST /api/get_next_item.php
{
  "session_id": 123,
  "current_theta": 0.0,
  "items_answered": []
}

Response:
{
  "success": true,
  "item": { /* question data */ },
  "current_theta": 0.0,
  "items_completed": 0,
  "items_remaining": 20,
  "assessment_complete": false
}
```

### 3. **Submit Answer & Update Theta**
```http
POST /api/submit_single_response.php
{
  "session_id": 123,
  "item_id": 5,
  "selected_option": "B",
  "is_correct": 1,
  "time_spent": 15
}

Response:
{
  "success": true,
  "is_correct": true,
  "new_theta": 0.34,
  "previous_theta": 0.0,
  "theta_change": 0.34,
  "classification": "Basic",
  "standard_error": 0.52,
  "feedback": "Correct! Next question coming up.",
  "total_responses": 1
}
```

### 4. **Get Next Adaptive Item**
```http
POST /api/get_next_item.php
{
  "session_id": 123,
  "current_theta": 0.34,  ← Updated theta!
  "items_answered": [5]
}

Response:
{
  "item": { /* harder question (b ≈ 0.5) */ },
  "current_theta": 0.34,
  ...
}
```

### 5. **Repeat Until Done**

Continue loop:
1. Submit answer → Get new theta
2. Get next item based on new theta
3. Repeat

Stop when:
- ✅ `assessment_complete: true` in response
- ✅ Standard Error < 0.3 (sufficient precision)
- ✅ Maximum items reached (20)

### 6. **Finalize Session**
```http
POST /api/submit_responses.php
{
  "session_id": 123,
  "student_id": 4,
  "responses": [] // Empty, already saved individually
}

→ Marks session complete
→ Updates Students.CurrentAbility
→ Returns final results
```

---

## 📱 Android App Changes Needed

### Current Approach (Fixed Test)
```java
// Get all 20 items at once
apiService.getPreAssessmentItems().enqueue(...)
  → questionList = response.items (20 items)
  → Show questions one by one
  → Submit all responses at end
```

### New Approach (Adaptive Test)

#### Option 1: Complete Refactor (Recommended)
```java
// Get one item at a time
private int currentQuestionIndex = 0;
private List<Integer> itemsAnswered = new ArrayList<>();
private double currentTheta = 0.0;

private void loadNextQuestion() {
    GetNextItemRequest request = new GetNextItemRequest(
        sessionId,
        currentTheta,
        itemsAnswered
    );

    apiService.getNextItem(request).enqueue(new Callback<NextItemResponse>() {
        @Override
        public void onResponse(..., Response<NextItemResponse> response) {
            if (response.body().isAssessmentComplete()) {
                finishAssessment();
            } else {
                displayQuestion(response.body().getItem());
            }
        }
    });
}

private void submitAnswer(int itemId, String answer, boolean isCorrect) {
    SubmitSingleRequest request = new SubmitSingleRequest(
        sessionId,
        itemId,
        answer,
        isCorrect,
        timeSpent
    );

    apiService.submitSingleResponse(request).enqueue(new Callback<SingleResponseResult>() {
        @Override
        public void onResponse(..., Response<SingleResponseResult> response) {
            // Update theta
            currentTheta = response.body().getNewTheta();

            // Add to answered list
            itemsAnswered.add(itemId);

            // Show feedback
            Toast.makeText(this, response.body().getFeedback(), Toast.LENGTH_SHORT).show();

            // Load next question
            loadNextQuestion();
        }
    });
}
```

#### Option 2: Hybrid (Keep Fixed, Add Adaptive Mode)
```java
// Add assessment mode toggle
public enum AssessmentMode {
    FIXED,      // Get all 20 items at once (current behavior)
    ADAPTIVE    // Get one item at a time based on ability
}

private AssessmentMode mode = AssessmentMode.ADAPTIVE;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (mode == AssessmentMode.FIXED) {
        loadAllQuestions();  // Existing code
    } else {
        loadNextAdaptiveQuestion();  // New code
    }
}
```

---

## 🔧 API Endpoints

### New Endpoints

#### 1. `/api/get_next_item.php`
**Purpose**: Get the next best item using IRT Maximum Information criterion

**Input**:
```json
{
  "session_id": 123,
  "current_theta": 0.5,
  "items_answered": [1, 3, 5, 7, 9]
}
```

**Output**:
```json
{
  "success": true,
  "item": {
    "ItemID": 12,
    "ItemText": "Which word is spelled correctly?",
    "DifficultyParam": 0.6,  ← Matched to theta!
    ...
  },
  "current_theta": 0.5,
  "items_completed": 5,
  "items_remaining": 15,
  "assessment_complete": false,
  "progress_percentage": 25.0
}
```

#### 2. `/api/submit_single_response.php`
**Purpose**: Submit one response and get updated theta immediately

**Input**:
```json
{
  "session_id": 123,
  "item_id": 12,
  "selected_option": "A",
  "is_correct": 1,
  "time_spent": 18
}
```

**Output**:
```json
{
  "success": true,
  "is_correct": true,
  "new_theta": 0.73,
  "previous_theta": 0.5,
  "theta_change": 0.23,
  "classification": "Proficient",
  "standard_error": 0.38,
  "feedback": "Correct! Moving to a harder question.",
  "total_responses": 6
}
```

### Modified Endpoints

#### `/api/submit_responses.php`
**Still used** to finalize the session and update the student's overall ability.

For adaptive testing, call this at the end with an empty responses array (since responses were already saved individually).

---

## 📊 Item Selection Algorithm

### Maximum Information Criterion

The `selectNextItem()` method chooses the item that provides the **most information** at the current theta:

```
Information(θ) = a² × (P - c)² × Q / ((1 - c) × P)

Where:
- a = discrimination parameter
- b = difficulty parameter
- c = guessing parameter
- P = probability of correct response
- Q = 1 - P
```

**Result**: The item with difficulty close to the student's ability provides maximum information.

**Example**:
- Student theta = 0.5
- Item A: b = 0.5 (matched difficulty) → High information ✅
- Item B: b = 2.0 (too hard) → Low information ❌
- Item C: b = -1.0 (too easy) → Low information ❌

The system picks **Item A** automatically!

---

## 🎯 Stopping Rules

The assessment can stop when:

### 1. **Precision Target** (Recommended)
```php
$targetSEM = 0.3;  // Stop when Standard Error ≤ 0.3
$minItems = 10;    // But answer at least 10 items
```

- SEM < 0.3 = Very reliable estimate
- Typically achieved in 10-15 items

### 2. **Maximum Items**
```php
$maxItems = 20;  // Never exceed 20 items
```

- Safety limit to prevent endless tests
- Good for time constraints

### 3. **Fixed Length**
```php
$fixedLength = 15;  // Always give exactly 15 items
```

- Simpler for students to understand
- "You have 10 questions remaining"

---

## 🚀 Migration Path

### Phase 1: Backend Ready (✅ Done)
- ✅ IRT algorithm implemented
- ✅ `get_next_item.php` created
- ✅ `submit_single_response.php` created
- ✅ Database schema supports adaptive testing

### Phase 2: Android App (To Do)
1. Create new request/response models:
   - `GetNextItemRequest.java`
   - `NextItemResponse.java`
   - `SubmitSingleRequest.java`
   - `SingleResponseResult.java`

2. Update `ApiService.java`:
   ```java
   @POST("get_next_item.php")
   Call<NextItemResponse> getNextItem(@Body GetNextItemRequest request);

   @POST("submit_single_response.php")
   Call<SingleResponseResult> submitSingleResponse(@Body SubmitSingleRequest request);
   ```

3. Refactor `PreAssessmentActivity.java`:
   - Remove `loadQuestions()` bulk load
   - Add `loadNextQuestion()` adaptive load
   - Submit answers one at a time
   - Update theta after each response
   - Show real-time feedback

4. Add progress tracking:
   - "Question 8 of ~15" (approximate)
   - Theta visualization (optional)
   - Classification badge (updates live)

### Phase 3: Testing
1. Test with students at different ability levels
2. Verify theta changes appropriately
3. Check stopping rules work correctly
4. Ensure all item types work (Syntax, Spelling, etc.)

### Phase 4: Launch
1. Make adaptive mode default
2. Keep fixed mode as fallback
3. Monitor performance and precision
4. Gather feedback

---

## 📈 Expected Results

**Before (Fixed Test)**:
- All students: 20 questions
- Avg precision: SEM ≈ 0.45
- Avg time: 25 minutes
- Student satisfaction: Mixed

**After (Adaptive Test)**:
- Average students: 12-15 questions
- Avg precision: SEM ≈ 0.28 (better!)
- Avg time: 15 minutes (40% faster!)
- Student satisfaction: High (questions are "just right")

---

## 🔍 Debugging

### Check if Item Selection Works
```sql
-- See what items are being selected
SELECT
    r.ResponseID,
    r.ItemID,
    i.DifficultyParam,
    r.ThetaBeforeResponse,
    r.IsCorrect,
    r.ThetaAfterResponse
FROM Responses r
JOIN Items i ON r.ItemID = i.ItemID
WHERE SessionID = 123
ORDER BY r.ResponseID;
```

Look for:
- ✅ Difficulty increasing after correct answers
- ✅ Difficulty decreasing after wrong answers
- ✅ Difficulty staying near theta

### Check Theta Progression
```sql
-- See how theta changes over time
SELECT
    ResponseID,
    ItemID,
    IsCorrect,
    ROUND(ThetaBeforeResponse, 2) as Before,
    ROUND(ThetaAfterResponse, 2) as After,
    ROUND(ThetaAfterResponse - ThetaBeforeResponse, 2) as Change
FROM Responses
WHERE SessionID = 123
ORDER BY ResponseID;
```

Expected pattern:
- Correct answer → Theta increases (or stays if already high)
- Wrong answer → Theta decreases (or stays if already low)
- Changes get smaller as precision improves

---

## 🎓 Best Practices

1. **Start from Student's Known Ability**
   - Use `Students.CurrentAbility` as `InitialTheta`
   - Much faster convergence than starting from 0.0

2. **Show Progress Clearly**
   - "Approximately X questions remaining"
   - Progress bar based on SEM, not fixed count

3. **Give Immediate Feedback**
   - Show "Correct!" or "Try again"
   - Optionally show difficulty level changing

4. **Handle Edge Cases**
   - Student gets all questions right → Stop at theta ≈ 2.0
   - Student gets all questions wrong → Stop at theta ≈ -2.0
   - No more items available → Stop gracefully

5. **Balance Speed vs Precision**
   - Minimum 10 items (prevent lucky guessing)
   - Maximum 20 items (respect student time)
   - Target SEM 0.3 (good balance)

---

## 📚 Further Reading

- [Wikipedia: Computerized Adaptive Testing](https://en.wikipedia.org/wiki/Computerized_adaptive_testing)
- [Wikipedia: Item Response Theory](https://en.wikipedia.org/wiki/Item_response_theory)
- Research paper: "Maximum Information Item Selection in CAT" (Dodd, 1990)

---

*Ready to implement? Start with Phase 2 (Android App) and test thoroughly before launch!*
