# LiteRise Adaptive Assessment System - Implementation Plan

## Overview
This document outlines the complete implementation plan for transitioning from hardcoded questions to a full IRT-based adaptive testing system with audio-based pronunciation assessment.

## ✅ COMPLETED (Phase 1)

### 1. Database Schema
- ✅ `AssessmentItems` table with IRT parameters (difficulty, discrimination, guessing)
- ✅ `StudentResponses` table for ML-ready tracking
- ✅ `SP_GetNextAdaptiveQuestion` stored procedure
- ✅ `SP_RecordStudentResponse` stored procedure
- ✅ 36 sample questions across 4 categories

**Location:** `api/db/assessment_items_schema.sql`, `api/db/sample_assessment_items.sql`

### 2. API Endpoints
- ✅ `get_next_question.php` - Adaptive question selection
- ✅ `submit_answer.php` - Answer tracking with IRT calculations

**Location:** `api/get_next_question.php`, `api/submit_answer.php`

### 3. Android Models
- ✅ `AdaptiveQuestionRequest/Response`
- ✅ `SubmitAnswerRequest/Response`
- ✅ API methods in ApiService

### 4. Helper Class
- ✅ `AdaptiveQuestionHelper` - Manages API calls and theta tracking

**Location:** `app/src/main/java/com/example/literise/helpers/AdaptiveQuestionHelper.java`

---

## 🚧 IN PROGRESS (Phase 2)

### Android App Integration

#### Step 1: Modify PlacementTestActivity
**File:** `app/src/main/java/com/example/literise/activities/PlacementTestActivity.java`

**Changes needed:**
1. Replace `QuestionBankHelper` with `AdaptiveQuestionHelper`
2. Modify `loadNextQuestion()` to use async API calls
3. Update `checkAnswer()` to submit answers via API
4. Keep category transitions but use API for question selection within categories

**Implementation approach:**
```java
// OLD:
categoryQuestions = questionBankHelper.getQuestionsByCategory(currentCategory);
currentQuestion = irtEngine.selectNextQuestion(categoryQuestions);

// NEW:
String category = getCategoryName(currentCategory);
adaptiveHelper.getNextQuestion(category, new AdaptiveQuestionHelper.QuestionCallback() {
    @Override
    public void onSuccess(AdaptiveQuestionResponse response) {
        currentQuestion = convertToPlacementQuestion(response.getQuestion());
        displayCurrentQuestion();
    }

    @Override
    public void onError(String error) {
        // Handle error
    }
});
```

#### Step 2: Add Question Conversion Method
Create method to convert API response to PlacementQuestion format:

```java
private PlacementQuestion convertToPlacementQuestion(AdaptiveQuestionResponse.QuestionData apiQuestion) {
    PlacementQuestion question = new PlacementQuestion();
    question.setQuestionId(apiQuestion.getItemId());
    question.setCategory(apiQuestion.getCategory());
    question.setQuestionText(apiQuestion.getQuestionText());
    question.setQuestionType(apiQuestion.getQuestionType());
    question.setOptionA(apiQuestion.getOptionA());
    question.setOptionB(apiQuestion.getOptionB());
    question.setOptionC(apiQuestion.getOptionC());
    question.setOptionD(apiQuestion.getOptionD());
    question.setDifficulty(apiQuestion.getDifficulty());
    return question;
}
```

#### Step 3: Update Answer Submission
```java
// After checking answer correctness
adaptiveHelper.submitAnswer(
    currentQuestion.getQuestionId(),
    selectedAnswer,
    isCorrect,
    responseTimeSeconds,
    new AdaptiveQuestionHelper.AnswerCallback() {
        @Override
        public void onSuccess(SubmitAnswerResponse response) {
            // Continue to next question
            loadNextQuestion();
        }

        @Override
        public void onError(String error) {
            // Handle error
        }
    }
);
```

---

## 📋 TODO (Phase 3): Audio-Based Pronunciation System

### Part A: Database Schema for Pronunciation

**File:** `api/db/pronunciation_items_schema.sql` (NEW)

```sql
-- Add pronunciation-specific fields to AssessmentItems
ALTER TABLE AssessmentItems
ADD AudioFileURL VARCHAR(500) NULL,
    TargetPhoneme VARCHAR(50) NULL,
    TargetWord VARCHAR(100) NULL,
    TargetSentence NVARCHAR(500) NULL,
    MinPronunciationScore FLOAT DEFAULT 0.70;

-- Create table for pronunciation scores
CREATE TABLE PronunciationScores (
    ScoreID INT IDENTITY(1,1) PRIMARY KEY,
    ResponseID INT NOT NULL FOREIGN KEY REFERENCES StudentResponses(ResponseID),
    AudioURL VARCHAR(500) NULL,  -- Student's recorded audio
    TranscribedText NVARCHAR(500) NULL,  -- What speech-to-text heard
    PronunciationScore FLOAT NULL,  -- 0.0 to 1.0
    PhonemeAccuracy FLOAT NULL,  -- Specific phoneme accuracy
    FluencyScore FLOAT NULL,  -- Speaking rate/fluency
    RecordedAt DATETIME DEFAULT GETDATE()
);
```

### Part B: Speech-to-Text Integration

#### Option 1: Google Cloud Speech-to-Text (Recommended)
**Pros:**
- Best accuracy
- Free tier: 60 minutes/month
- Supports pronunciation assessment

**Implementation:**
1. Add Google Cloud Speech API to Android project
2. Record audio using MediaRecorder
3. Send to Google Speech API
4. Get transcription + pronunciation confidence score

**Dependencies needed:**
```gradle
// app/build.gradle
implementation 'com.google.cloud:google-cloud-speech:2.20.0'
implementation 'io.grpc:grpc-okhttp:1.50.2'
```

#### Option 2: Android SpeechRecognizer (Simpler, Less Accurate)
**Pros:**
- Already integrated (SpeechRecognitionHelper exists)
- No API costs
- Works offline

**Cons:**
- Less accurate pronunciation scoring
- No detailed phoneme analysis

### Part C: Android Components Needed

#### 1. PronunciationHelper.java (NEW)
```java
public class PronunciationHelper {
    // Record audio
    public void startRecording(String targetWord);

    // Stop and process
    public void stopRecording(PronunciationCallback callback);

    // Upload audio and get score
    public void evaluatePronunciation(File audioFile, String targetText, callback);

    interface PronunciationCallback {
        void onScore(float accuracy, String transcription);
        void onError(String error);
    }
}
```

#### 2. API Endpoint: evaluate_pronunciation.php (NEW)
```php
// Receives audio file
// Calls Google Speech API
// Returns pronunciation score
// Saves to PronunciationScores table
```

### Part D: UI Components for Pronunciation

**New question types:**
1. **Word Pronunciation** - "Say this word: 'butterfly'"
2. **Sentence Reading** - "Read this sentence clearly"
3. **Phoneme Focus** - "Say a word with the /th/ sound"

**UI Elements:**
- Record button (microphone icon)
- Waveform visualization during recording
- Playback button to hear correct pronunciation
- Score feedback (with visual indicator 0-100%)

### Part E: Pronunciation Question Examples

```sql
-- Sample pronunciation items
INSERT INTO AssessmentItems (
    Category, Subcategory, SkillArea,
    QuestionText, QuestionType,
    TargetWord, TargetPhoneme,
    AudioFileURL,  -- Reference audio for correct pronunciation
    DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel
) VALUES
('Pronunciation', 'Single Words', 'Beginning Sounds',
 'Say this word clearly: "cat"', 'Pronunciation',
 'cat', '/k/',
 'https://your-cdn.com/audio/cat.mp3',
 -1.5, 1.3, 0.0, 1),

('Pronunciation', 'Single Words', 'Blends',
 'Say this word: "street"', 'Pronunciation',
 'street', '/str/',
 'https://your-cdn.com/audio/street.mp3',
 0.3, 1.6, 0.0, 2),

('Pronunciation', 'Sentences', 'Fluency',
 'Read this sentence: "The quick brown fox jumps over the lazy dog."', 'Pronunciation',
 'The quick brown fox jumps over the lazy dog.', NULL,
 'https://your-cdn.com/audio/quickfox.mp3',
 1.2, 1.7, 0.0, 3);
```

---

## 🎯 Implementation Priority

### Immediate Next Steps (This Session):
1. ✅ Commit current AdaptiveQuestionHelper
2. ⏭️ Modify PlacementTestActivity to use adaptive API
3. ⏭️ Test adaptive question flow end-to-end
4. ⏭️ Copy API files to XAMPP and run SQL scripts

### Short Term (Next Session):
5. Add pronunciation database schema
6. Create 20-30 pronunciation questions
7. Integrate Google Speech API
8. Build PronunciationHelper class
9. Add pronunciation question UI to PlacementTestActivity

### Long Term (Future):
10. ML model for better theta estimation
11. Detailed pronunciation analytics dashboard
12. Phoneme-level feedback visualization
13. Speech therapy recommendations

---

## 📊 Expected Outcomes

### After Adaptive API Integration:
- ✅ Questions selected based on student ability
- ✅ Better placement accuracy
- ✅ All responses tracked for ML training
- ✅ Real-time difficulty adjustment

### After Pronunciation System:
- ✅ Comprehensive reading assessment (comprehension + pronunciation)
- ✅ Objective pronunciation scoring
- ✅ Speech pattern tracking over time
- ✅ Identify specific phonemes needing practice

---

## 🔧 Technical Requirements

### Server Requirements:
- SQL Server with assessment schema
- PHP 8.2+ with PDO
- (Optional) Google Cloud account for Speech API

### Android Requirements:
- Min SDK: 24 (Android 7.0)
- Permissions: RECORD_AUDIO, INTERNET
- Storage for audio files

### API Keys Needed:
- Google Cloud Speech API key (for pronunciation)
- (Optional) Google Cloud Storage (for audio file hosting)

---

## 📝 Testing Checklist

### Adaptive API Testing:
- [ ] Question selection matches student theta
- [ ] Category filtering works
- [ ] Answer submission updates theta correctly
- [ ] Progress tracking accurate
- [ ] Network error handling works

### Pronunciation Testing:
- [ ] Audio recording works on different devices
- [ ] Speech-to-text accuracy acceptable
- [ ] Pronunciation scoring consistent
- [ ] Score feedback clear to students
- [ ] Audio files saved properly

---

## 💡 Future Enhancements

1. **Adaptive Pronunciation Difficulty**
   - Start with simple words, increase complexity
   - Match pronunciation difficulty to reading level

2. **Accent Awareness**
   - Train model on Philippine English
   - Accept valid regional pronunciations

3. **Real-time Feedback**
   - Show waveform while speaking
   - Highlight mispronounced phonemes

4. **Practice Mode**
   - Allow unlimited retries
   - Provide pronunciation tips
   - Karaoke-style word highlighting

---

**Last Updated:** 2026-01-06
**Status:** Phase 1 Complete, Phase 2 In Progress
