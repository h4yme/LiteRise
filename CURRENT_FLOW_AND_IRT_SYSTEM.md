# 📚 LiteRise App Flow & IRT System - Complete Technical Guide

## 🎯 Table of Contents
1. [App Startup Flow](#app-startup-flow)
2. [IRT Placement Test System](#irt-placement-test-system)
3. [Learning Flow (LESSON → GAME → QUIZ)](#learning-flow)
4. [Code Walkthrough](#code-walkthrough)

---

## 1. App Startup Flow

### **Entry Point: SplashActivity**

```
App Launch
   ↓
SplashActivity (2 second delay)
   ↓
Check SessionManager.isLoggedIn()
   ↓
┌─────────────────────────────────┐
│ NOT LOGGED IN                   │
│ → LoginRegisterSelectionActivity│
│   → LoginActivity / Register    │
└─────────────────────────────────┘
         OR
┌─────────────────────────────────┐
│ LOGGED IN                       │
│ ↓ Check session state:          │
│ • hasSeenWelcome()?             │
│ • hasStartedAssessment()?       │
│ • hasCompletedAssessment()?     │
└─────────────────────────────────┘
   ↓
Navigate to appropriate screen
```

### **Code: SplashActivity.java**

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_splash);

    new Handler().postDelayed(() -> {
        SessionManager session = new SessionManager(SplashActivity.this);
        Intent intent;

        if (session.isLoggedIn()) {
            // Check progress and route accordingly
            if (!session.hasSeenWelcome()) {
                // First time - show welcome onboarding
                intent = new Intent(this, WelcomeOnboardingActivity.class);
            } else if (session.hasStartedAssessment() && !session.hasCompletedAssessment()) {
                // Resume placement test
                intent = new Intent(this, PlacementTestActivity.class);
            } else if (!session.hasCompletedAssessment()) {
                // Ready for placement test
                intent = new Intent(this, WelcomeOnboardingActivity.class);
            } else {
                // Go to Dashboard
                intent = new Intent(this, DashboardActivity.class);
            }
        } else {
            // Not logged in
            intent = new Intent(this, LoginRegisterSelectionActivity.class);
        }

        startActivity(intent);
        finish();
    }, 2000);
}
```

### **Session State Tracking**

The `SessionManager` tracks student progress:

```java
// Session Manager stores:
- isLoggedIn: boolean
- studentId: int
- email: String
- placementLevel: int (1=Beginner, 2=Intermediate, 3=Advanced)
- hasSeenWelcome: boolean
- hasStartedAssessment: boolean
- hasCompletedAssessment: boolean
```

---

## 2. IRT Placement Test System

### **What is IRT?**

**IRT (Item Response Theory)** is a psychometric model that estimates a student's ability (θ - theta) based on their responses to questions with known difficulty and discrimination parameters.

### **IRT Formula**

```
Probability of Correct Answer:
P(θ) = 1 / (1 + e^(-a(θ - b)))

Where:
- θ (theta) = Student ability (-3 to +3)
- b = Question difficulty (-3 to +3)
- a = Question discrimination (how well it separates abilities)
- e = Euler's number (2.71828...)
```

### **How IRT Works in LiteRise**

```
┌──────────────────────────────────────────────────────┐
│ 1. INITIALIZATION                                    │
│    theta (θ) = 0.0 (neutral starting point)          │
│    Student takes 25 questions (5 per category)       │
└──────────────────────────────────────────────────────┘
                       ↓
┌──────────────────────────────────────────────────────┐
│ 2. QUESTION SELECTION (Maximum Information)          │
│    For each unanswered question, calculate:          │
│    Information = a² × P(θ) × (1 - P(θ))             │
│    Select question with HIGHEST information          │
└──────────────────────────────────────────────────────┘
                       ↓
┌──────────────────────────────────────────────────────┐
│ 3. STUDENT ANSWERS QUESTION                          │
│    Submit answer to API (A/B/C/D)                    │
└──────────────────────────────────────────────────────┘
                       ↓
┌──────────────────────────────────────────────────────┐
│ 4. THETA UPDATE (Gradient Ascent)                   │
│    expected_p = P(θ) using current theta             │
│    error = (isCorrect ? 1.0 : 0.0) - expected_p     │
│    Δθ = 0.3 × a × error                              │
│    θ_new = θ_old + Δθ (clamped to -3.0 to 3.0)      │
└──────────────────────────────────────────────────────┘
                       ↓
┌──────────────────────────────────────────────────────┐
│ 5. REPEAT UNTIL 25 QUESTIONS COMPLETE                │
└──────────────────────────────────────────────────────┘
                       ↓
┌──────────────────────────────────────────────────────┐
│ 6. CALCULATE PLACEMENT LEVEL                         │
│    θ < -0.5   → Beginner (Level 1)                   │
│    -0.5 ≤ θ < 0.5 → Intermediate (Level 2)           │
│    θ ≥ 0.5    → Advanced (Level 3)                   │
└──────────────────────────────────────────────────────┘
```

### **Code: IRTEngine.java**

#### **Question Selection (Maximum Information)**

```java
public PlacementQuestion selectNextQuestion(List<PlacementQuestion> availableQuestions) {
    PlacementQuestion bestQuestion = null;
    double maxInformation = 0.0;

    for (PlacementQuestion question : availableQuestions) {
        // Skip already answered
        if (isQuestionAnswered(question)) continue;

        // Calculate information for this question at current theta
        double information = calculateInformation(question, theta);

        if (information > maxInformation) {
            maxInformation = information;
            bestQuestion = question;
        }
    }

    return bestQuestion;
}

// Fisher Information Formula
private double calculateInformation(PlacementQuestion question, double theta) {
    double a = question.getDiscrimination(); // e.g., 1.2
    double b = question.getDifficulty();      // e.g., 0.5

    // Probability of correct answer: P(θ) = 1 / (1 + e^(-a(θ-b)))
    double p = 1.0 / (1.0 + Math.exp(-a * (theta - b)));
    double q = 1.0 - p; // Probability of incorrect

    // Fisher Information = a² × P × Q
    return a * a * p * q;
}
```

**Example:**
- Student has θ = 0.2
- Question A: difficulty b = 0.3, discrimination a = 1.0
- Question B: difficulty b = -1.5, discrimination a = 1.5

```
Question A:
P(0.2) = 1 / (1 + e^(-1.0(0.2-0.3))) = 0.475
Information = 1.0² × 0.475 × 0.525 = 0.249

Question B:
P(0.2) = 1 / (1 + e^(-1.5(0.2-(-1.5)))) = 0.930
Information = 1.5² × 0.930 × 0.070 = 0.146

→ Question A selected (higher information)
```

#### **Theta Update (After Answer)**

```java
public void updateTheta(PlacementQuestion question, boolean isCorrect) {
    // Store question and result
    answeredQuestions.add(question);
    answerResults.add(isCorrect);

    double a = question.getDiscrimination();
    double b = question.getDifficulty();

    // Expected probability of correct answer
    double expectedP = 1.0 / (1.0 + Math.exp(-a * (theta - b)));

    // Calculate error
    double error = (isCorrect ? 1.0 : 0.0) - expectedP;

    // Update theta using gradient ascent
    // Δθ = learning_rate × a × error
    double deltaTheta = 0.3 * a * error; // learning_rate = 0.3

    // Update with bounds [-3.0, 3.0]
    theta = Math.max(-3.0, Math.min(3.0, theta + deltaTheta));
}
```

**Example Theta Updates:**

```
Initial θ = 0.0

Question 1: difficulty = -0.5, discrimination = 1.0, Answer = CORRECT
  expected_p = 1/(1+e^(-1.0(0-(-0.5)))) = 0.622
  error = 1.0 - 0.622 = 0.378
  Δθ = 0.3 × 1.0 × 0.378 = 0.113
  θ_new = 0.0 + 0.113 = 0.113 ✅ (ability increased!)

Question 2: difficulty = 0.8, discrimination = 1.2, Answer = INCORRECT
  expected_p = 1/(1+e^(-1.2(0.113-0.8))) = 0.307
  error = 0.0 - 0.307 = -0.307
  Δθ = 0.3 × 1.2 × (-0.307) = -0.110
  θ_new = 0.113 + (-0.110) = 0.003 ❌ (ability decreased)

Question 3: difficulty = 0.2, discrimination = 1.5, Answer = CORRECT
  expected_p = 1/(1+e^(-1.5(0.003-0.2))) = 0.433
  error = 1.0 - 0.433 = 0.567
  Δθ = 0.3 × 1.5 × 0.567 = 0.255
  θ_new = 0.003 + 0.255 = 0.258 ✅

... (continues for 25 questions)

Final θ = 0.65 → Advanced Level!
```

#### **Placement Level Calculation**

```java
public int calculatePlacementLevel() {
    if (theta < -0.5) {
        return 1; // Beginner
    } else if (theta < 0.5) {
        return 2; // Intermediate
    } else {
        return 3; // Advanced
    }
}
```

**Theta Ranges:**
```
θ < -0.5     → Beginner     (struggling with basic questions)
-0.5 ≤ θ < 0.5 → Intermediate (average performance)
θ ≥ 0.5      → Advanced    (excelling at hard questions)
```

### **API Integration**

#### **Get Next Question from API**

```java
// PlacementTestActivity.java
private void loadNextQuestion() {
    String categoryName = getCategoryName(currentCategory); // e.g., "Phonics and Word Study"

    adaptiveHelper.getNextQuestion(categoryName, new QuestionCallback() {
        @Override
        public void onSuccess(AdaptiveQuestionResponse response) {
            currentQuestion = convertToPlacementQuestion(response.getQuestion());
            displayCurrentQuestion();
        }

        @Override
        public void onError(String error) {
            // Fallback to local question bank
            categoryQuestions = questionBankHelper.getQuestionsByCategory(currentCategory);
            currentQuestion = irtEngine.selectNextQuestion(categoryQuestions);
            displayCurrentQuestion();
        }
    });
}
```

**API Request:**
```
POST /get_next_question.php
{
  "session_id": 1738368000,
  "category": "Phonics and Word Study",
  "assessment_type": "PreAssessment"
}
```

**API Response:**
```json
{
  "success": true,
  "question": {
    "item_id": 42,
    "category": "Phonics and Word Study",
    "subcategory": "Phonics",
    "question_type": "multiple_choice",
    "question_text": "Which word has the CVCC pattern?",
    "option_a": "cat",
    "option_b": "jump",
    "option_c": "trip",
    "option_d": "run",
    "difficulty": 0.3,
    "discrimination": 1.2
    // Note: correct_answer NOT sent (security)
  }
}
```

#### **Submit Answer to API**

```java
private void checkAnswer() {
    int responseTime = (int) ((System.currentTimeMillis() - questionStartTime) / 1000);

    adaptiveHelper.submitAnswer(
        currentQuestion.getQuestionId(),
        selectedAnswerLetter, // "A", "B", "C", or "D"
        false, // We don't know if correct yet
        responseTime,
        new AnswerCallback() {
            @Override
            public void onSuccess(SubmitAnswerResponse response) {
                boolean isCorrect = response.isCorrect(); // API tells us

                // Update local IRT engine
                irtEngine.updateTheta(currentQuestion, isCorrect);

                // Sync theta from API (more accurate)
                double apiTheta = response.getFeedback().getNewThetaEstimate();
                irtEngine.setTheta(apiTheta);

                // Move to next question
                currentQuestionNumber++;
                if (currentQuestionNumber > 25) {
                    showResults();
                } else {
                    loadNextQuestion();
                }
            }
        }
    );
}
```

**API Request:**
```
POST /submit_answer.php
{
  "session_id": 1738368000,
  "item_id": 42,
  "student_response": "B",
  "response_time": 12
}
```

**API Response:**
```json
{
  "success": true,
  "is_correct": true,
  "message": "Correct answer!",
  "feedback": {
    "new_theta_estimate": 0.258,
    "theta_se": 0.45,
    "category_performance": {
      "Phonics and Word Study": 0.75
    }
  }
}
```

### **Category Performance Tracking**

```java
public int[] getCategoryScores() {
    int[] scores = new int[5]; // [Phonics, Vocabulary, Grammar, Comprehending, Creating]
    int[] counts = new int[5];

    for (int i = 0; i < answeredQuestions.size(); i++) {
        PlacementQuestion q = answeredQuestions.get(i);
        boolean correct = answerResults.get(i);
        int category = q.getCategory() - 1; // 0-indexed

        if (category >= 0 && category < 5) {
            if (correct) scores[category]++;
            counts[category]++;
        }
    }

    // Convert to percentages
    for (int i = 0; i < 5; i++) {
        if (counts[i] > 0) {
            scores[i] = (scores[i] * 100) / counts[i];
        }
    }

    return scores; // e.g., [80, 60, 70, 65, 55]
}
```

**Example Output:**
```
Category 1 (Phonics): 80% (4/5 correct)
Category 2 (Vocabulary): 60% (3/5 correct)
Category 3 (Grammar): 70% (3.5/5 correct)
Category 4 (Comprehending): 65% (3.25/5 correct)
Category 5 (Creating): 55% (2.75/5 correct)

Overall Accuracy: 66%
Final θ: 0.45 → Intermediate Level
```

---

## 3. Learning Flow (LESSON → GAME → QUIZ)

### **Module Ladder System**

After placement test, student goes to Dashboard → Module → 13 Nodes

```
ModuleLadderActivity
   ↓
Fetches nodes via API: get_module_ladder.php
   ↓
Returns 13 NodeViews with states:
- LOCKED (not accessible yet)
- UNLOCKED (ready to start)
- IN_PROGRESS (partially completed)
- COMPLETED (all 3 phases done)
```

### **3-Phase Adaptive Flow**

```
Student taps Node 1 (UNLOCKED)
   ↓
┌──────────────────────────────────────┐
│ PHASE 1: LESSON                      │
│ Activity: LessonContentActivity      │
│ API: get_lesson_content.php          │
│   ↓                                  │
│ Shows:                               │
│ - Lesson title                       │
│ - Learning objectives                │
│ - Content (adaptive by level)        │
│ - Leo mascot guidance                │
│   ↓                                  │
│ Student reads and completes          │
└──────────────────────────────────────┘
   ↓ (Auto-proceed after 800ms)
┌──────────────────────────────────────┐
│ Toast: "✅ Lesson Complete!          │
│         Now let's play! 🎮"          │
└──────────────────────────────────────┘
   ↓
┌──────────────────────────────────────┐
│ PHASE 2: GAME                        │
│ Activity: PhonicsNinjaActivity       │
│           WordHuntActivity, etc.     │
│   ↓                                  │
│ Student plays game:                  │
│ - 45 seconds                         │
│ - Scores points                      │
│ - Earns XP                           │
│   ↓                                  │
│ Results saved via API:               │
│ POST save_game_results.php           │
└──────────────────────────────────────┘
   ↓ (Auto-proceed after 800ms)
┌──────────────────────────────────────┐
│ Toast: "🎮 Great job!                │
│         Time for the quiz! 📝"       │
└──────────────────────────────────────┘
   ↓
┌──────────────────────────────────────┐
│ PHASE 3: QUIZ                        │
│ Activity: QuizActivity               │
│ API: get_quiz_questions.php          │
│   ↓                                  │
│ Student answers 10 questions         │
│   ↓                                  │
│ Submit via: submit_quiz.php          │
│   ↓                                  │
│ Calculate score: 85%                 │
└──────────────────────────────────────┘
   ↓
┌──────────────────────────────────────┐
│ ADAPTIVE DECISION                    │
│   ↓                                  │
│ if (score < 70%)                     │
│   → ADD_INTERVENTION                 │
│ else if (score 70-79% && beginner)   │
│   → ADD_SUPPLEMENTAL                 │
│ else if (score >= 90% && advanced)   │
│   → OFFER_ENRICHMENT                 │
│ else                                 │
│   → PROCEED to next node             │
└──────────────────────────────────────┘
   ↓
Returns to ModuleLadderActivity
- Node 1: COMPLETED ✅
- Node 2: UNLOCKED 🔓
```

### **Code: ModuleLadderActivity.java (Auto-Progression)**

```java
// Initialize activity result launchers for seamless flow
private void initializeActivityLaunchers() {
    // LESSON launcher
    lessonLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (isAutoProceedMode && currentNode != null) {
                // Show success toast
                Toast.makeText(this, "✅ Lesson Complete! Now let's play! 🎮",
                    Toast.LENGTH_SHORT).show();

                // Auto-proceed to game after 800ms
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    startGamePhase(currentNode);
                }, 800);
            } else {
                // Manual mode - return to ladder
                loadModuleLadder();
            }
        }
    );

    // GAME launcher
    gameLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (isAutoProceedMode && currentNode != null) {
                Toast.makeText(this, "🎮 Great job! Time for the quiz! 📝",
                    Toast.LENGTH_SHORT).show();

                // Auto-proceed to quiz after 800ms
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    startQuizPhase(currentNode);
                }, 800);
            } else {
                loadModuleLadder();
            }
        }
    );

    // QUIZ launcher
    quizLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            // Quiz complete - return to ladder
            // Ladder will refresh and show updated progress
            loadModuleLadder();
        }
    );
}

// Start lesson phase
private void startLessonPhase(NodeView node) {
    currentNode = node;
    Intent intent = new Intent(this, LessonContentActivity.class);
    intent.putExtra("node_id", node.getNodeId());
    intent.putExtra("student_id", studentId);
    intent.putExtra("placement_level", placementLevel);
    lessonLauncher.launch(intent);
}

// Start game phase
private void startGamePhase(NodeView node) {
    Intent intent = selectGameForNode(node); // Returns appropriate game
    intent.putExtra("node_id", node.getNodeId());
    intent.putExtra("student_id", studentId);
    gameLauncher.launch(intent);
}

// Start quiz phase
private void startQuizPhase(NodeView node) {
    Intent intent = new Intent(this, QuizActivity.class);
    intent.putExtra("node_id", node.getNodeId());
    intent.putExtra("student_id", studentId);
    intent.putExtra("placement_level", placementLevel);
    quizLauncher.launch(intent);
}
```

### **Game Selection Logic**

```java
private Intent selectGameForNode(NodeView node) {
    String domain = moduleDomain; // e.g., "Phonics", "Vocabulary"
    int nodeNumber = node.getNodeNumber();

    Intent intent;

    switch (domain.toLowerCase()) {
        case "phonics":
            // Rotate games for Phonics module
            if (nodeNumber % 3 == 1) {
                intent = new Intent(this, MinimalPairsActivity.class);
            } else if (nodeNumber % 3 == 2) {
                intent = new Intent(this, PictureMatchActivity.class);
            } else {
                intent = new Intent(this, WordHuntActivity.class);
            }
            break;

        case "vocabulary":
            if (nodeNumber % 3 == 1) {
                intent = new Intent(this, PictureMatchActivity.class);
            } else if (nodeNumber % 3 == 2) {
                intent = new Intent(this, StorySequencingActivity.class);
            } else {
                intent = new Intent(this, WordHuntActivity.class);
            }
            break;

        case "grammar":
            if (nodeNumber % 2 == 0) {
                intent = new Intent(this, SentenceScrambleActivity.class);
            } else {
                intent = new Intent(this, FillInTheBlanksActivity.class);
            }
            break;

        default:
            // Fallback
            intent = new Intent(this, WordHuntActivity.class);
    }

    return intent;
}
```

---

## 4. Code Walkthrough

### **Complete User Journey with Code**

```
┌─────────────────────────────────────────────────────────┐
│ 1. APP LAUNCH                                           │
└─────────────────────────────────────────────────────────┘
```

```java
// SplashActivity.java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_splash);

    new Handler().postDelayed(() -> {
        SessionManager session = new SessionManager(this);

        if (session.isLoggedIn()) {
            if (session.hasCompletedAssessment()) {
                // Go to Dashboard
                startActivity(new Intent(this, DashboardActivity.class));
            } else {
                // Go to Placement Test
                startActivity(new Intent(this, PlacementTestActivity.class));
            }
        } else {
            // Go to Login
            startActivity(new Intent(this, LoginActivity.class));
        }

        finish();
    }, 2000);
}
```

```
┌─────────────────────────────────────────────────────────┐
│ 2. PLACEMENT TEST                                       │
└─────────────────────────────────────────────────────────┘
```

```java
// PlacementTestActivity.java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_placement_test);

    // Initialize IRT Engine
    irtEngine = new IRTEngine(); // θ = 0.0 initially

    // Start with first question
    loadNextQuestion();
}

private void loadNextQuestion() {
    String categoryName = getCategoryName(currentCategory);

    // Fetch adaptive question from API
    adaptiveHelper.getNextQuestion(categoryName, response -> {
        currentQuestion = convertToPlacementQuestion(response.getQuestion());
        displayCurrentQuestion();
    });
}

private void checkAnswer() {
    // Submit answer to API
    adaptiveHelper.submitAnswer(
        currentQuestion.getQuestionId(),
        selectedAnswerLetter, // "A", "B", "C", or "D"
        responseTime,
        response -> {
            boolean isCorrect = response.isCorrect();

            // Update theta
            irtEngine.updateTheta(currentQuestion, isCorrect);

            // Sync with API theta
            irtEngine.setTheta(response.getFeedback().getNewThetaEstimate());

            currentQuestionNumber++;

            if (currentQuestionNumber > 25) {
                showResults();
            } else {
                loadNextQuestion();
            }
        }
    );
}

private void showResults() {
    int placementLevel = irtEngine.calculatePlacementLevel(); // 1, 2, or 3
    double accuracy = irtEngine.getAccuracyPercentage();
    int[] categoryScores = irtEngine.getCategoryScores();

    // Save to session
    sessionManager.savePlacementLevel(placementLevel);

    // Navigate to results
    Intent intent = new Intent(this, PlacementResultActivity.class);
    intent.putExtra("placement_level", placementLevel);
    intent.putExtra("accuracy", accuracy);
    intent.putExtra("category_scores", categoryScores);
    startActivity(intent);
    finish();
}
```

```
┌─────────────────────────────────────────────────────────┐
│ 3. DASHBOARD                                            │
└─────────────────────────────────────────────────────────┘
```

```java
// DashboardActivity.java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_dashboard);

    // Load student stats
    loadGamificationStats(); // XP, Streak, Badges

    // Load modules prioritized by placement test results
    loadModules();
}

private void loadModules() {
    // Get category scores from placement test
    int[] categoryScores = {
        sessionManager.getCategoryScore("Cat1_PhonicsWordStudy"),
        sessionManager.getCategoryScore("Cat2_VocabularyWordKnowledge"),
        sessionManager.getCategoryScore("Cat3_GrammarAwareness"),
        sessionManager.getCategoryScore("Cat4_ComprehendingText"),
        sessionManager.getCategoryScore("Cat5_CreatingComposing")
    };

    // Create modules
    modules = new ArrayList<>();
    modules.add(new LearningModule(1, "Phonics and Word Study", categoryScores[0]));
    modules.add(new LearningModule(2, "Vocabulary and Word Knowledge", categoryScores[1]));
    modules.add(new LearningModule(3, "Grammar Awareness", categoryScores[2]));
    modules.add(new LearningModule(4, "Comprehending and Analyzing Text", categoryScores[3]));
    modules.add(new LearningModule(5, "Creating and Composing Text", categoryScores[4]));

    // Prioritize weakest modules first
    ModulePriorityManager.prioritizeModules(modules, placementLevel);

    // Display
    moduleAdapter.setModules(modules);
}
```

```
┌─────────────────────────────────────────────────────────┐
│ 4. MODULE LADDER                                        │
└─────────────────────────────────────────────────────────┘
```

```java
// ModuleLadderActivity.java
private void loadModuleLadder() {
    apiService.getModuleLadder(studentId, moduleId)
        .enqueue(new Callback<ModuleLadderResponse>() {
            @Override
            public void onResponse(Response<ModuleLadderResponse> response) {
                List<NodeView> nodes = response.body().getNodes();

                // nodes contains:
                // - Node 1: UNLOCKED (can start)
                // - Nodes 2-13: LOCKED (not accessible)

                pathView.setNodes(nodes);
                pathView.setOnNodeClickListener(node -> {
                    if (node.getState() == NodeState.UNLOCKED) {
                        startLessonPhase(node);
                    }
                });
            }
        });
}
```

```
┌─────────────────────────────────────────────────────────┐
│ 5. LESSON → GAME → QUIZ (AUTO-PROGRESSION)             │
└─────────────────────────────────────────────────────────┘
```

```java
// Lesson completes
lessonLauncher result received
   ↓
Toast: "✅ Lesson Complete! Now let's play! 🎮"
   ↓
Wait 800ms
   ↓
startGamePhase(currentNode)
   ↓
Game completes
   ↓
Toast: "🎮 Great job! Time for the quiz! 📝"
   ↓
Wait 800ms
   ↓
startQuizPhase(currentNode)
   ↓
Quiz completes (score: 85%)
   ↓
Return to ModuleLadderActivity
   ↓
Refresh ladder via API
   ↓
- Node 1: COMPLETED ✅
- Node 2: UNLOCKED 🔓 (ready to start!)
```

---

## Summary

The LiteRise app uses a sophisticated **IRT-based placement system** to classify students into ability levels, then provides an **adaptive 3-phase learning flow** (LESSON → GAME → QUIZ) with **seamless auto-progression** between phases.

**Key Components:**
1. **IRTEngine** - Calculates student ability (θ) using gradient ascent
2. **SessionManager** - Tracks progress and state
3. **ActivityResultLaunchers** - Enables auto-progression
4. **API Integration** - Syncs all data with backend
5. **Adaptive Branching** - Adds support nodes based on performance

**Flow Summary:**
```
Login → Placement Test (25 IRT questions) → Calculate θ →
Classify Level (Beginner/Intermediate/Advanced) → Dashboard →
Module Ladder (13 nodes) → Node 1 (Lesson → Game → Quiz) →
Adaptive Decision → Node 2 (Lesson → Game → Quiz) → ... →
Complete Module → Next Module
```

All theta calculations, question selections, and progress tracking happen in real-time with API synchronization! 🚀
