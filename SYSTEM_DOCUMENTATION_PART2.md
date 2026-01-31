# LiteRise System Documentation - Part 2
## API Endpoints, Android Architecture & Implementation Guide

---

## 6. API Endpoints

### 6.1 Base URL Structure

```
Production: https://api.literise.com/
Development: http://localhost/literise-api/
```

### 6.2 Authentication Flow

#### **POST /login.php**

**Request:**
```json
{
  "email": "student@example.com",
  "password": "password123"
}
```

**Response (Success):**
```json
{
  "success": true,
  "message": "Login successful",
  "student": {
    "student_id": 28,
    "email": "student@example.com",
    "nickname": "Leo Learner",
    "placement_level": 2,
    "final_theta": 0.35,
    "total_xp": 1250,
    "streak_days": 7
  }
}
```

**Response (Error):**
```json
{
  "success": false,
  "message": "Invalid email or password"
}
```

**PHP Implementation:**
```php
<?php
header('Content-Type: application/json');
require_once 'db_connection.php';

// Get POST data
$data = json_decode(file_get_contents('php://input'), true);
$email = $data['email'] ?? '';
$password = $data['password'] ?? '';

try {
    $stmt = $pdo->prepare("
        SELECT StudentID, Email, PasswordHash, Nickname,
               PlacementLevel, FinalTheta, TotalXP, StreakDays
        FROM Students
        WHERE Email = ? AND IsActive = 1
    ");
    $stmt->execute([$email]);
    $student = $stmt->fetch(PDO::FETCH_ASSOC);

    if ($student && password_verify($password, $student['PasswordHash'])) {
        // Update last login
        $updateStmt = $pdo->prepare("
            UPDATE Students
            SET LastLoginDate = GETDATE()
            WHERE StudentID = ?
        ");
        $updateStmt->execute([$student['StudentID']]);

        echo json_encode([
            'success' => true,
            'message' => 'Login successful',
            'student' => [
                'student_id' => $student['StudentID'],
                'email' => $student['Email'],
                'nickname' => $student['Nickname'],
                'placement_level' => $student['PlacementLevel'],
                'final_theta' => (float)$student['FinalTheta'],
                'total_xp' => $student['TotalXP'],
                'streak_days' => $student['StreakDays']
            ]
        ]);
    } else {
        echo json_encode([
            'success' => false,
            'message' => 'Invalid email or password'
        ]);
    }
} catch (PDOException $e) {
    echo json_encode([
        'success' => false,
        'message' => 'Database error: ' . $e->getMessage()
    ]);
}
?>
```

---

#### **POST /register.php**

**Request:**
```json
{
  "email": "newstudent@example.com",
  "password": "securepass123",
  "first_name": "John",
  "last_name": "Doe"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Registration successful",
  "student_id": 29
}
```

---

### 6.3 IRT Placement Test APIs

#### **POST /get_next_question.php**

**Purpose:** Get next adaptive question based on current theta

**Request:**
```json
{
  "session_id": 1738368000,
  "category": "Phonics and Word Study",
  "assessment_type": "PreAssessment"
}
```

**Response:**
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
    "discrimination": 1.2,
    "reading_passage": null
  }
}
```

**PHP Implementation:**
```php
<?php
header('Content-Type: application/json');
require_once 'db_connection.php';

$data = json_decode(file_get_contents('php://input'), true);
$sessionId = $data['session_id'];
$category = $data['category'];

try {
    // Get current theta from session
    $stmt = $pdo->prepare("
        SELECT FinalTheta FROM AssessmentSessions
        WHERE SessionID = ?
    ");
    $stmt->execute([$sessionId]);
    $session = $stmt->fetch(PDO::FETCH_ASSOC);
    $currentTheta = $session['FinalTheta'] ?? 0.0;

    // Get unanswered questions in category
    $stmt = $pdo->prepare("
        SELECT TOP 10
            ItemID, Category, Subcategory, QuestionType,
            QuestionText, ReadingPassage,
            OptionA, OptionB, OptionC, OptionD,
            Difficulty, Discrimination
        FROM AssessmentItems
        WHERE Category = ?
          AND IsActive = 1
          AND ItemID NOT IN (
              SELECT ItemID FROM StudentResponses
              WHERE SessionID = ?
          )
        ORDER BY NEWID()
    ");
    $stmt->execute([$category, $sessionId]);
    $items = $stmt->fetchAll(PDO::FETCH_ASSOC);

    // Calculate Fisher Information for each item
    $bestItem = null;
    $maxInfo = 0;

    foreach ($items as $item) {
        $a = $item['Discrimination'];
        $b = $item['Difficulty'];
        $c = 0.25;

        // P(θ) = c + (1-c)/(1 + e^(-a(θ-b)))
        $exponent = -$a * ($currentTheta - $b);
        $p = $c + (1 - $c) / (1 + exp($exponent));
        $q = 1 - $p;

        // Information = a² × P × Q
        $info = $a * $a * $p * $q;

        if ($info > $maxInfo) {
            $maxInfo = $info;
            $bestItem = $item;
        }
    }

    if ($bestItem) {
        echo json_encode([
            'success' => true,
            'question' => [
                'item_id' => $bestItem['ItemID'],
                'category' => $bestItem['Category'],
                'subcategory' => $bestItem['Subcategory'],
                'question_type' => $bestItem['QuestionType'],
                'question_text' => $bestItem['QuestionText'],
                'reading_passage' => $bestItem['ReadingPassage'],
                'option_a' => $bestItem['OptionA'],
                'option_b' => $bestItem['OptionB'],
                'option_c' => $bestItem['OptionC'],
                'option_d' => $bestItem['OptionD'],
                'difficulty' => (float)$bestItem['Difficulty'],
                'discrimination' => (float)$bestItem['Discrimination']
            ]
        ]);
    } else {
        echo json_encode([
            'success' => false,
            'message' => 'No more questions available'
        ]);
    }

} catch (PDOException $e) {
    echo json_encode([
        'success' => false,
        'message' => 'Error: ' . $e->getMessage()
    ]);
}
?>
```

---

#### **POST /submit_answer.php**

**Purpose:** Submit answer and update theta

**Request:**
```json
{
  "session_id": 1738368000,
  "item_id": 42,
  "student_response": "B",
  "response_time": 12
}
```

**Response:**
```json
{
  "success": true,
  "is_correct": true,
  "message": "Correct answer!",
  "feedback": {
    "new_theta_estimate": 0.258,
    "theta_se": 0.45,
    "information": 0.32,
    "category_performance": {
      "Phonics and Word Study": 0.75
    }
  }
}
```

**PHP Implementation:**
```php
<?php
header('Content-Type: application/json');
require_once 'db_connection.php';

$data = json_decode(file_get_contents('php://input'), true);
$sessionId = $data['session_id'];
$itemId = $data['item_id'];
$studentResponse = $data['student_response'];
$responseTime = $data['response_time'] ?? 0;

try {
    // Get item details
    $stmt = $pdo->prepare("
        SELECT CorrectAnswer, Difficulty, Discrimination
        FROM AssessmentItems WHERE ItemID = ?
    ");
    $stmt->execute([$itemId]);
    $item = $stmt->fetch(PDO::FETCH_ASSOC);

    // Check if correct
    $isCorrect = strcasecmp($studentResponse, $item['CorrectAnswer']) === 0;

    // Get current theta
    $stmt = $pdo->prepare("
        SELECT FinalTheta FROM AssessmentSessions WHERE SessionID = ?
    ");
    $stmt->execute([$sessionId]);
    $currentTheta = $stmt->fetchColumn() ?: 0.0;

    // Calculate expected probability
    $a = $item['Discrimination'];
    $b = $item['Difficulty'];
    $c = 0.25;
    $exponent = -$a * ($currentTheta - $b);
    $expectedP = $c + (1 - $c) / (1 + exp($exponent));

    // Update theta
    $learningRate = 0.3;
    $y = $isCorrect ? 1.0 : 0.0;
    $error = $y - $expectedP;
    $deltaTheta = $learningRate * $a * $error;
    $newTheta = max(-3.0, min(3.0, $currentTheta + $deltaTheta));

    // Save response
    $stmt = $pdo->prepare("
        INSERT INTO StudentResponses (
            SessionID, ItemID, StudentResponse, IsCorrect,
            ResponseTime, ThetaAtTime, ThetaAfter,
            ItemDifficulty, ItemDiscrimination
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    ");
    $stmt->execute([
        $sessionId, $itemId, $studentResponse, $isCorrect ? 1 : 0,
        $responseTime, $currentTheta, $newTheta,
        $b, $a
    ]);

    // Update session theta
    $stmt = $pdo->prepare("
        UPDATE AssessmentSessions
        SET FinalTheta = ?,
            TotalQuestions = TotalQuestions + 1,
            CorrectAnswers = CorrectAnswers + ?
        WHERE SessionID = ?
    ");
    $stmt->execute([$newTheta, $isCorrect ? 1 : 0, $sessionId]);

    echo json_encode([
        'success' => true,
        'is_correct' => $isCorrect,
        'message' => $isCorrect ? 'Correct answer!' : 'Incorrect answer',
        'feedback' => [
            'new_theta_estimate' => round($newTheta, 3),
            'theta_se' => 0.45, // Simplified
            'information' => round($a * $a * $expectedP * (1 - $expectedP), 3)
        ]
    ]);

} catch (PDOException $e) {
    echo json_encode([
        'success' => false,
        'message' => 'Error: ' . $e->getMessage()
    ]);
}
?>
```

---

### 6.4 Learning Flow APIs

#### **GET /get_module_ladder.php**

**Request:**
```
GET /get_module_ladder.php?student_id=28&module_id=1
```

**Response:**
```json
{
  "success": true,
  "module": {
    "module_id": 1,
    "module_name": "Phonics and Word Study",
    "progress_percentage": 23
  },
  "nodes": [
    {
      "node_id": 1,
      "node_number": 1,
      "lesson_title": "CVC and CVCC Patterns",
      "state": "COMPLETED",
      "lesson_completed": true,
      "game_completed": true,
      "quiz_completed": true,
      "best_quiz_score": 85,
      "xp_earned": 50
    },
    {
      "node_id": 2,
      "node_number": 2,
      "lesson_title": "CCVC Patterns",
      "state": "UNLOCKED",
      "lesson_completed": false,
      "game_completed": false,
      "quiz_completed": false,
      "best_quiz_score": null,
      "xp_earned": 0
    },
    {
      "node_id": 3,
      "node_number": 3,
      "lesson_title": "Blends and Digraphs",
      "state": "LOCKED",
      "lesson_completed": false,
      "game_completed": false,
      "quiz_completed": false,
      "best_quiz_score": null,
      "xp_earned": 0
    }
  ]
}
```

---

#### **GET /get_lesson_content.php**

**Request:**
```
GET /get_lesson_content.php?node_id=1&placement_level=2
```

**Response:**
```json
{
  "success": true,
  "node_id": 1,
  "lesson_title": "CVC and CVCC Patterns",
  "learning_objectives": "Identify and read CVCC words like jump, tent, lamp",
  "content_json": "{\"sections\":[{\"type\":\"introduction\",\"text\":\"Welcome to phonics!\"},{\"type\":\"example\",\"words\":[\"jump\",\"tent\",\"lamp\"]}]}",
  "estimated_duration": 15,
  "xp_reward": 50
}
```

---

#### **GET /get_quiz_questions.php**

**Request:**
```
GET /get_quiz_questions.php?node_id=1&placement_level=2
```

**Response:**
```json
{
  "success": true,
  "questions": [
    {
      "question_id": 101,
      "question_text": "Which word has a CVCC pattern?",
      "option_a": "cat",
      "option_b": "jump",
      "option_c": "dog",
      "option_d": "run",
      "difficulty": 2,
      "points": 10
    },
    {
      "question_id": 102,
      "question_text": "Select the CVCC word:",
      "option_a": "trip",
      "option_b": "lamp",
      "option_c": "go",
      "option_d": "see",
      "difficulty": 1,
      "points": 10
    }
  ]
}
```

---

#### **POST /submit_quiz.php**

**Request:**
```json
{
  "student_id": 28,
  "node_id": 1,
  "answers": [
    {"question_id": 101, "selected_answer": "B"},
    {"question_id": 102, "selected_answer": "B"}
  ],
  "time_spent": 180
}
```

**Response:**
```json
{
  "success": true,
  "score": 85,
  "total_questions": 10,
  "correct_answers": 8,
  "passed": true,
  "xp_earned": 50,
  "adaptive_decision": "PROCEED",
  "message": "Great job! You passed!"
}
```

---

#### **POST /save_game_results.php**

**Request:**
```json
{
  "student_id": 28,
  "node_id": 1,
  "game_type": "PhonicsNinja",
  "score": 420,
  "max_combo": 8,
  "accuracy": 0.75,
  "time_spent": 45
}
```

**Response:**
```json
{
  "success": true,
  "xp_earned": 42,
  "message": "Game results saved successfully"
}
```

---

### 6.5 Complete API Endpoint List

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/login.php` | POST | Student login |
| `/register.php` | POST | New student registration |
| `/forgot_password.php` | POST | Request password reset OTP |
| `/verify_otp.php` | POST | Verify OTP code |
| `/reset_password.php` | POST | Set new password |
| `/get_preassessment_items.php` | POST | Legacy - get all placement items |
| `/get_next_question.php` | POST | Get next adaptive question (IRT) |
| `/submit_answer.php` | POST | Submit answer and update theta |
| `/save_placement_result.php` | POST | Save final placement results |
| `/get_module_ladder.php` | GET | Get 13 nodes for module |
| `/get_lesson_content.php` | GET | Get lesson content |
| `/get_quiz_questions.php` | GET | Get quiz questions |
| `/submit_quiz.php` | POST | Submit quiz answers |
| `/save_game_results.php` | POST | Save game score |
| `/get_node_progress.php` | GET | Check phase completion |
| `/update_node_progress.php` | POST | Update phase flags |
| `/get_word_hunt.php` | POST | Get word hunt game data |
| `/get_scramble_sentences.php` | POST | Get sentence scramble data |
| `/evaluate_pronunciation.php` | POST | Azure pronunciation assessment |

---

## 7. Android Application Structure

### 7.1 Project Structure

```
app/
├── src/main/
│   ├── java/com/example/literise/
│   │   ├── activities/
│   │   │   ├── BaseActivity.java
│   │   │   ├── SplashActivity.java
│   │   │   ├── LoginActivity.java
│   │   │   ├── RegisterActivity.java
│   │   │   ├── WelcomeOnboardingActivity.java
│   │   │   ├── PlacementIntroActivity.java
│   │   │   ├── PlacementTestActivity.java ⭐ (IRT)
│   │   │   ├── PlacementResultActivity.java
│   │   │   ├── DashboardActivity.java ⭐
│   │   │   ├── ModuleLadderActivity.java ⭐
│   │   │   ├── LessonContentActivity.java
│   │   │   ├── QuizActivity.java
│   │   │   ├── QuizResultActivity.java
│   │   │   ├── PhonicsNinjaActivity.java
│   │   │   ├── SynonymSprintActivity.java
│   │   │   ├── WordExplosionActivity.java
│   │   │   └── games/
│   │   │       ├── WordHuntActivity.java
│   │   │       ├── SentenceScrambleActivity.java
│   │   │       ├── MinimalPairsActivity.java
│   │   │       ├── PictureMatchActivity.java
│   │   │       ├── FillInTheBlanksActivity.java
│   │   │       └── StorySequencingActivity.java
│   │   │
│   │   ├── adapters/
│   │   │   ├── ModuleAdapter.java
│   │   │   ├── QuizAdapter.java
│   │   │   └── LeaderboardAdapter.java
│   │   │
│   │   ├── api/
│   │   │   ├── ApiClient.java
│   │   │   ├── ApiService.java ⭐ (Retrofit interface)
│   │   │   └── RetrofitClient.java
│   │   │
│   │   ├── database/
│   │   │   ├── SessionManager.java ⭐
│   │   │   └── QuestionBankHelper.java
│   │   │
│   │   ├── helpers/
│   │   │   ├── AdaptiveQuestionHelper.java ⭐ (IRT API calls)
│   │   │   ├── PronunciationHelper.java
│   │   │   ├── SoundEffectsHelper.java
│   │   │   ├── TextToSpeechHelper.java
│   │   │   └── KaraokeTextHelper.java
│   │   │
│   │   ├── models/
│   │   │   ├── Student.java
│   │   │   ├── LearningModule.java
│   │   │   ├── NodeView.java
│   │   │   ├── PlacementQuestion.java ⭐
│   │   │   ├── AdaptiveQuestionResponse.java ⭐
│   │   │   ├── SubmitAnswerResponse.java ⭐
│   │   │   ├── LessonContentResponse.java
│   │   │   ├── QuizQuestionsResponse.java
│   │   │   └── GameResult.java
│   │   │
│   │   ├── utils/
│   │   │   ├── IRTEngine.java ⭐ (IRT calculations)
│   │   │   ├── ModulePriorityManager.java
│   │   │   ├── SessionLogger.java
│   │   │   └── NetworkUtils.java
│   │   │
│   │   └── views/
│   │       ├── ModulePathView.java (Custom view for ladder)
│   │       └── NodeView.java
│   │
│   ├── res/
│   │   ├── layout/
│   │   │   ├── activity_splash.xml
│   │   │   ├── activity_login.xml
│   │   │   ├── activity_placement_test.xml ⭐
│   │   │   ├── activity_dashboard.xml ⭐
│   │   │   ├── activity_module_ladder.xml ⭐
│   │   │   ├── activity_phonics_ninja.xml
│   │   │   ├── activity_synonym_sprint.xml
│   │   │   ├── activity_word_explosion.xml
│   │   │   └── fragment_question_multiple_choice.xml
│   │   │
│   │   ├── drawable/
│   │   │   ├── gradient_background_ninja.xml
│   │   │   ├── gradient_background_sprint.xml
│   │   │   ├── word_card_correct.xml
│   │   │   ├── word_card_wrong.xml
│   │   │   └── ... (100+ drawables)
│   │   │
│   │   ├── values/
│   │   │   ├── colors.xml
│   │   │   ├── strings.xml
│   │   │   └── styles.xml
│   │   │
│   │   └── font/
│   │       ├── visby_bold.ttf
│   │       ├── visby_regular.ttf
│   │       └── poppins_semibold.ttf
│   │
│   └── AndroidManifest.xml
│
└── build.gradle (app)
```

### 7.2 Key Java Classes

#### **IRTEngine.java** - IRT Calculations

```java
package com.example.literise.utils;

import com.example.literise.models.PlacementQuestion;
import java.util.ArrayList;
import java.util.List;

public class IRTEngine {
    private double theta; // Student ability
    private List<PlacementQuestion> answeredQuestions;
    private List<Boolean> answerResults;

    // Constants
    private static final double INITIAL_THETA = 0.0;
    private static final double THETA_MIN = -3.0;
    private static final double THETA_MAX = 3.0;
    private static final double LEARNING_RATE = 0.3;

    public IRTEngine() {
        this.theta = INITIAL_THETA;
        this.answeredQuestions = new ArrayList<>();
        this.answerResults = new ArrayList<>();
    }

    /**
     * Select next question using Maximum Information criterion
     */
    public PlacementQuestion selectNextQuestion(List<PlacementQuestion> availableQuestions) {
        if (availableQuestions == null || availableQuestions.isEmpty()) {
            return null;
        }

        PlacementQuestion bestQuestion = null;
        double maxInformation = 0.0;

        for (PlacementQuestion question : availableQuestions) {
            if (isQuestionAnswered(question)) continue;

            double information = calculateInformation(question, theta);
            if (information > maxInformation) {
                maxInformation = information;
                bestQuestion = question;
            }
        }

        return bestQuestion != null ? bestQuestion : availableQuestions.get(0);
    }

    /**
     * Calculate Fisher Information: I(θ) = a² × P(θ) × Q(θ)
     */
    private double calculateInformation(PlacementQuestion question, double theta) {
        double a = question.getDiscrimination();
        double b = question.getDifficulty();

        double p = 1.0 / (1.0 + Math.exp(-a * (theta - b)));
        double q = 1.0 - p;

        return a * a * p * q;
    }

    /**
     * Update theta after answer using gradient ascent
     */
    public void updateTheta(PlacementQuestion question, boolean isCorrect) {
        answeredQuestions.add(question);
        answerResults.add(isCorrect);

        double a = question.getDiscrimination();
        double b = question.getDifficulty();

        // Expected probability
        double expectedP = question.calculateProbability(theta);

        // Error
        double error = (isCorrect ? 1.0 : 0.0) - expectedP;

        // Update: Δθ = α × a × error
        double deltaTheta = LEARNING_RATE * a * error;

        // Bounded update
        theta = Math.max(THETA_MIN, Math.min(THETA_MAX, theta + deltaTheta));
    }

    /**
     * Calculate placement level: 1=Beginner, 2=Intermediate, 3=Advanced
     */
    public int calculatePlacementLevel() {
        if (theta < -0.5) return 1;
        else if (theta < 0.5) return 2;
        else return 3;
    }

    /**
     * Get category performance (5 categories)
     */
    public int[] getCategoryScores() {
        int[] scores = new int[5];
        int[] counts = new int[5];

        for (int i = 0; i < answeredQuestions.size(); i++) {
            PlacementQuestion q = answeredQuestions.get(i);
            boolean correct = answerResults.get(i);
            int category = q.getCategory() - 1;

            if (category >= 0 && category < 5) {
                if (correct) scores[category]++;
                counts[category]++;
            }
        }

        for (int i = 0; i < 5; i++) {
            if (counts[i] > 0) {
                scores[i] = (scores[i] * 100) / counts[i];
            }
        }

        return scores;
    }

    // Getters and setters
    public double getTheta() { return theta; }
    public void setTheta(double theta) {
        this.theta = Math.max(THETA_MIN, Math.min(THETA_MAX, theta));
    }
    public int getTotalAnswered() { return answeredQuestions.size(); }
    public int getTotalCorrect() {
        int count = 0;
        for (boolean result : answerResults) {
            if (result) count++;
        }
        return count;
    }
    public double getAccuracyPercentage() {
        if (answerResults.isEmpty()) return 0.0;
        return (getTotalCorrect() * 100.0) / answerResults.size();
    }
}
```

#### **SessionManager.java** - Session Persistence

```java
package com.example.literise.database;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "LiteRiseSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_STUDENT_ID = "studentId";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_NICKNAME = "nickname";
    private static final String KEY_PLACEMENT_LEVEL = "placementLevel";
    private static final String KEY_FINAL_THETA = "finalTheta";
    private static final String KEY_HAS_SEEN_WELCOME = "hasSeenWelcome";
    private static final String KEY_HAS_STARTED_ASSESSMENT = "hasStartedAssessment";
    private static final String KEY_HAS_COMPLETED_ASSESSMENT = "hasCompletedAssessment";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveLoginSession(int studentId, String email, String nickname,
                                   int placementLevel, double finalTheta) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_STUDENT_ID, studentId);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_NICKNAME, nickname);
        editor.putInt(KEY_PLACEMENT_LEVEL, placementLevel);
        editor.putFloat(KEY_FINAL_THETA, (float) finalTheta);
        editor.apply();
    }

    public void setAssessmentCompleted(boolean completed) {
        editor.putBoolean(KEY_HAS_COMPLETED_ASSESSMENT, completed);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public int getStudentId() {
        return prefs.getInt(KEY_STUDENT_ID, 0);
    }

    public int getPlacementLevel() {
        return prefs.getInt(KEY_PLACEMENT_LEVEL, 2);
    }

    public boolean hasCompletedAssessment() {
        return prefs.getBoolean(KEY_HAS_COMPLETED_ASSESSMENT, false);
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}
```

---

*Continue to Part 3 for complete implementation guide...*
