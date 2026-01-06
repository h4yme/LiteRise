# Pronunciation Assessment & ML Enhancement Guide

## Overview
This guide covers:
1. Pronunciation Assessment System Deployment
2. Google Cloud Speech API Integration (Optional)
3. ML-Based Theta Estimation Implementation

---

## Part 1: Pronunciation Assessment System

### Features
✅ **30 IRT-Calibrated Pronunciation Questions** with phonetic transcriptions
✅ **Audio Recording** via MediaRecorder
✅ **Server-Side Scoring** with phoneme-level analysis
✅ **Progress Tracking** for long-term improvement monitoring
✅ **Adaptive Difficulty** based on student ability
✅ **Google Cloud Speech API Ready** (optional enhancement)

---

## Deployment Steps

### Step 1: Deploy Database Schema

Run the pronunciation schema extension in SQL Server:

```sql
-- 1. Run pronunciation schema
USE LiteRise;
GO
```

Execute `api/db/pronunciation_schema.sql` in SSMS

**This creates:**
- ✅ New columns in `AssessmentItems`:
  - `AudioPromptURL` - URL to audio pronunciation sample
  - `TargetPronunciation` - Expected word/phrase
  - `PhoneticTranscription` - IPA phonetic notation
  - `MinimumAccuracy` - Pass threshold (default 65%)
  - `PronunciationTips` - Helpful hints for students

- ✅ `PronunciationScores` table:
  - Stores detailed pronunciation results
  - Phoneme-level accuracy analysis
  - Speech quality metrics (confidence, fluency, completeness)
  - Full API response for debugging

- ✅ `StudentPronunciationProgress` table:
  - Daily/weekly progress tracking
  - Mastered vs problematic phonemes
  - Accuracy trends and improvement metrics

- ✅ Stored Procedures:
  - `SP_RecordPronunciationScore` - Save detailed pronunciation results
  - `SP_GetPronunciationProgress` - Retrieve student progress

**Verify:**
```sql
-- Check new columns
SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'AssessmentItems'
AND COLUMN_NAME IN ('AudioPromptURL', 'TargetPronunciation', 'PhoneticTranscription');

-- Check new tables
SELECT * FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_NAME IN ('PronunciationScores', 'StudentPronunciationProgress');
```

### Step 2: Load Sample Pronunciation Questions

Execute `api/db/sample_pronunciation_items.sql` in SSMS

**This creates 30 pronunciation questions:**

| Difficulty | Count | Examples | IRT Range |
|-----------|-------|----------|-----------|
| Easy | 5 | cat, dog, sun, big, hat | -2.0 to -1.6 |
| Medium | 10 | stop, tree, make, ship, knife | -1.0 to -0.1 |
| Medium-Hard | 9 | spring, splash, happy, elephant, important | 0.6 to 1.4 |
| Hard | 6 | enough, through, beautiful, congratulations, pharmaceutical | 1.6 to 2.5 |

**Each question includes:**
- Target word
- Phonetic transcription (IPA)
- Minimum passing accuracy
- Pronunciation tips

**Verify:**
```sql
SELECT
    CASE
        WHEN DifficultyParam <= -1.0 THEN 'Easy'
        WHEN DifficultyParam <= 0.5 THEN 'Medium'
        WHEN DifficultyParam <= 1.5 THEN 'Medium-Hard'
        ELSE 'Hard'
    END AS Level,
    COUNT(*) AS Count,
    MIN(DifficultyParam) AS MinDiff,
    MAX(DifficultyParam) AS MaxDiff
FROM AssessmentItems
WHERE QuestionType = 'Pronunciation'
GROUP BY
    CASE
        WHEN DifficultyParam <= -1.0 THEN 'Easy'
        WHEN DifficultyParam <= 0.5 THEN 'Medium'
        WHEN DifficultyParam <= 1.5 THEN 'Medium-Hard'
        ELSE 'Hard'
    END;
```

### Step 3: Deploy API Endpoint

Copy the pronunciation API to XAMPP:

```bash
cp api/evaluate_pronunciation.php C:/xampp/htdocs/api/
```

**Or manually:**
1. Open `api/evaluate_pronunciation.php`
2. Copy entire contents
3. Paste to `C:\xampp\htdocs\api\evaluate_pronunciation.php`
4. Save

**API Endpoint:** `POST http://localhost/api/evaluate_pronunciation.php`

**Request Format:** `multipart/form-data`
```
student_id: 31
item_id: 37
response_id: 1234
target_word: "elephant"
audio_file: <binary file>
```

**Response Format:**
```json
{
  "success": true,
  "score_id": 567,
  "pronunciation_result": {
    "recognized_text": "elephant",
    "confidence": 0.95,
    "overall_accuracy": 87,
    "pronunciation_score": 0.87,
    "fluency_score": 0.92,
    "completeness_score": 1.0,
    "feedback": "Great job! Your pronunciation is excellent!",
    "passed": true,
    "minimum_accuracy": 65
  },
  "phoneme_details": [
    {"phoneme": "ɛ", "accuracy": 0.95},
    {"phoneme": "l", "accuracy": 0.88},
    {"phoneme": "ə", "accuracy": 0.90}
  ]
}
```

### Step 4: Rebuild Android App

The app now includes `PronunciationHelper.java` for:
- ✅ Audio recording with MediaRecorder
- ✅ Multipart file upload to API
- ✅ Pronunciation scoring with detailed feedback

**No additional Android changes needed!** The `PlacementTestActivity` already has pronunciation UI - it just needs to use the new `PronunciationHelper` instead of the basic `SpeechRecognitionHelper`.

**To use in your code:**
```java
// Initialize helper
PronunciationHelper pronunciationHelper = new PronunciationHelper(this);

// Start recording
pronunciationHelper.startRecording(new PronunciationHelper.PronunciationCallback() {
    @Override
    public void onRecordingStarted() {
        // Show recording UI
    }

    @Override
    public void onRecordingStopped(File audioFile, int durationMs) {
        // Evaluate the recording
        pronunciationHelper.evaluatePronunciation(
            itemId, responseId, targetWord, audioFile,
            new PronunciationHelper.EvaluationCallback() {
                @Override
                public void onEvaluationSuccess(PronunciationHelper.PronunciationResult result) {
                    // Show results: result.getOverallAccuracy(), result.getFeedback()
                    boolean passed = result.isPassed();
                }

                @Override
                public void onEvaluationError(String error) {
                    // Handle error
                }
            }
        );
    }

    @Override
    public void onRecordingError(String error) {
        // Handle recording error
    }
});

// Stop recording (e.g., after button click or timeout)
pronunciationHelper.stopRecording(callback);
```

### Step 5: Test Pronunciation Flow

1. **Rebuild app** in Android Studio
2. **Start assessment**
3. **Pronunciation questions** will appear when:
   - Question type is "Pronunciation"
   - Currently uses basic speech recognition
4. **Test recording:**
   - Tap microphone button
   - Say the word
   - View pronunciation score

---

## Part 2: Google Cloud Speech API Integration (Optional)

The current implementation uses basic text matching for pronunciation scoring. For **production-quality pronunciation assessment**, integrate Google Cloud Speech-to-Text API.

### Benefits of Google Speech API
- ✅ Accurate speech-to-text transcription
- ✅ Confidence scores for each word
- ✅ Pronunciation assessment features
- ✅ Support for multiple languages
- ✅ Phoneme-level accuracy (with premium tier)

### Setup Steps

#### 1. Create Google Cloud Project

1. Go to https://console.cloud.google.com/
2. Create new project: "LiteRise-Speech"
3. Enable **Cloud Speech-to-Text API**
4. Create service account:
   - IAM & Admin > Service Accounts > Create
   - Role: "Cloud Speech Client"
   - Create key (JSON format)
5. Download credentials JSON file

#### 2. Install Google Cloud PHP SDK

```bash
cd C:/xampp/htdocs/api
composer require google/cloud-speech
```

#### 3. Configure API Credentials

Save credentials file:
```bash
C:/xampp/htdocs/api/google-cloud-credentials.json
```

Update `evaluate_pronunciation.php`:
```php
// Uncomment the Google Cloud section (lines 70-120)
use Google\Cloud\Speech\V1\SpeechClient;
use Google\Cloud\Speech\V1\RecognitionAudio;
use Google\Cloud\Speech\V1\RecognitionConfig;

$speech = new SpeechClient([
    'credentials' => __DIR__ . '/google-cloud-credentials.json'
]);
```

#### 4. Test API

```bash
# Test from command line
curl -X POST http://localhost/api/evaluate_pronunciation.php \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "student_id=31" \
  -F "item_id=37" \
  -F "response_id=1234" \
  -F "target_word=elephant" \
  -F "audio_file=@test_audio.wav"
```

### Advanced Features

**Pronunciation Assessment API (Premium):**
```php
$config = (new RecognitionConfig())
    ->setEnableAutomaticPunctuation(false)
    ->setPhraseHints([$targetPronunciation])
    ->setModel('default')
    ->setUseEnhanced(true)
    ->setEnableSpeakerDiarization(false)
    ->setEnableWordTimeOffsets(true)
    ->setEnableWordConfidence(true);
```

**Phoneme-Level Scoring:**
Google provides phoneme accuracy with their Pronunciation Assessment feature (requires premium tier).

---

## Part 3: ML-Based Theta Estimation

The current system uses simple IRT formulas for theta estimation. For **advanced ability prediction**, implement an ML model.

### Why ML for Theta Estimation?

**Current (Formula-Based):**
- Uses 3-Parameter Logistic (3PL) IRT model
- Simple weighted average
- Doesn't consider response patterns

**ML-Based (Advanced):**
- ✅ Considers response patterns and sequences
- ✅ Learns from historical data
- ✅ Adapts to individual student characteristics
- ✅ Predicts performance more accurately
- ✅ Identifies struggling students earlier

### Implementation Approach

#### Option 1: Python-Based ML Model (Recommended)

**1. Export Training Data:**

Create SQL script to export student responses:
```sql
SELECT
    StudentID,
    ItemID,
    DifficultyParam,
    DiscriminationParam,
    GuessingParam,
    IsCorrect,
    ResponseTime,
    QuestionNumber,
    StudentThetaAtTime,
    Category
FROM dbo.StudentResponses sr
JOIN dbo.AssessmentItems ai ON sr.ItemID = ai.ItemID
WHERE sr.CreatedAt >= DATEADD(MONTH, -3, GETDATE())
ORDER BY sr.StudentID, sr.CreatedAt;
```

Export to CSV: `student_responses_training.csv`

**2. Train ML Model (Python):**

```python
import pandas as pd
import numpy as np
from sklearn.ensemble import RandomForestRegressor
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_squared_error
import joblib

# Load data
df = pd.read_csv('student_responses_training.csv')

# Feature engineering
features = [
    'DifficultyParam',
    'DiscriminationParam',
    'GuessingParam',
    'ResponseTime',
    'QuestionNumber',
    'IsCorrect',
    'PreviousCorrectStreak',  # Calculate from sequence
    'AverageAccuracyLast5',   # Rolling average
    'DifficultyTrend'          # Increasing/decreasing
]

# Calculate sequence-based features
def add_sequence_features(group):
    group = group.sort_values('QuestionNumber')
    group['PreviousCorrectStreak'] = group['IsCorrect'].rolling(5, min_periods=1).sum()
    group['AverageAccuracyLast5'] = group['IsCorrect'].rolling(5, min_periods=1).mean()
    group['DifficultyTrend'] = group['DifficultyParam'].diff().fillna(0)
    return group

df = df.groupby('StudentID').apply(add_sequence_features).reset_index(drop=True)

# Prepare training data
X = df[features]
y = df['StudentThetaAtTime']

# Split data
X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, random_state=42
)

# Train Random Forest model
model = RandomForestRegressor(
    n_estimators=100,
    max_depth=10,
    min_samples_split=10,
    random_state=42
)

model.fit(X_train, y_train)

# Evaluate
y_pred = model.predict(X_test)
rmse = np.sqrt(mean_squared_error(y_test, y_pred))
print(f'RMSE: {rmse:.4f}')

# Save model
joblib.dump(model, 'theta_estimation_model.pkl')
```

**3. Create Python API Service:**

```python
from flask import Flask, request, jsonify
import joblib
import pandas as pd

app = Flask(__name__)
model = joblib.load('theta_estimation_model.pkl')

@app.route('/predict_theta', methods=['POST'])
def predict_theta():
    data = request.json
    features = pd.DataFrame([data['features']])
    theta = model.predict(features)[0]
    return jsonify({'theta': float(theta)})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
```

**4. Integrate with PHP API:**

```php
// In submit_answer.php, after recording response:

// Call Python ML service for advanced theta estimation
$mlEndpoint = 'http://localhost:5000/predict_theta';

$features = [
    'DifficultyParam' => $difficulty,
    'DiscriminationParam' => $discrimination,
    'GuessingParam' => $guessing,
    'ResponseTime' => $responseTime,
    'QuestionNumber' => $questionNumber,
    'IsCorrect' => $isCorrect ? 1 : 0,
    // Add sequence features...
];

$ch = curl_init($mlEndpoint);
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode(['features' => $features]));
curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

$response = curl_exec($ch);
$result = json_decode($response, true);

$newTheta = $result['theta'] ?? $calculatedTheta; // Fallback to formula
curl_close($ch);
```

#### Option 2: Database-Based ML (SQL Server ML Services)

SQL Server has built-in ML capabilities:

```sql
-- Install Python in SQL Server
EXEC sp_configure 'external scripts enabled', 1;
RECONFIGURE;

-- Create ML model training procedure
CREATE PROCEDURE dbo.SP_TrainThetaModel
AS
BEGIN
    EXEC sp_execute_external_script
    @language = N'Python',
    @script = N'
from sklearn.ensemble import RandomForestRegressor
import pickle

# Load data
X = InputDataSet[["DifficultyParam", "DiscriminationParam", ...]]
y = InputDataSet["StudentThetaAtTime"]

# Train model
model = RandomForestRegressor(n_estimators=100)
model.fit(X, y)

# Serialize model
trained_model = pickle.dumps(model)
',
    @input_data_1 = N'
        SELECT * FROM StudentResponses
        JOIN AssessmentItems ON ...
        WHERE CreatedAt >= DATEADD(MONTH, -3, GETDATE())
    ',
    @output_data_1_name = N'trained_model';
END
```

---

## Testing Checklist

### Pronunciation System
- [ ] Database schema deployed
- [ ] 30 pronunciation questions loaded
- [ ] API endpoint deployed to XAMPP
- [ ] Android app rebuilt
- [ ] Can record audio successfully
- [ ] Pronunciation scoring works
- [ ] Results saved to PronunciationScores table
- [ ] Progress tracking updated
- [ ] Feedback messages display correctly

### Google Speech API (If Implemented)
- [ ] Google Cloud project created
- [ ] Speech-to-Text API enabled
- [ ] Service account credentials downloaded
- [ ] PHP SDK installed via Composer
- [ ] Credentials file in correct location
- [ ] API test successful
- [ ] Pronunciation accuracy improved vs fallback

### ML Theta Estimation (If Implemented)
- [ ] Training data exported
- [ ] Python environment set up
- [ ] Model trained and saved
- [ ] ML API service running
- [ ] PHP integration tested
- [ ] Theta predictions more accurate than formula
- [ ] Fallback to formula if ML unavailable

---

## Performance Tips

**For Pronunciation:**
- Audio files are stored in cache and auto-deleted
- Limit recording to 10 seconds max
- Compress audio before upload (use AAC or AMR codec)
- Process asynchronously to avoid blocking UI

**For ML:**
- Cache theta predictions for 5-10 questions
- Update ML model weekly with new data
- Use ensemble of models for better accuracy
- Monitor prediction latency (<100ms target)

---

## Troubleshooting

### Pronunciation Issues

**"Audio file too large"**
- Reduce recording quality or duration
- Use more efficient codec (AMR_NB instead of WAV)

**"Speech recognition failed"**
- Check Google Cloud credentials
- Verify API quota not exceeded
- Test with simple words first

**"Low accuracy scores"**
- Background noise affecting quality
- Microphone permission denied
- Audio encoding mismatch

### ML Issues

**"Model prediction error"**
- Check feature format matches training data
- Verify all required features present
- Ensure model file path is correct

**"Python service not responding"**
- Check Flask app is running
- Verify port 5000 not blocked
- Check Python dependencies installed

---

## Next Steps

1. ✅ Deploy pronunciation database and questions
2. ✅ Test basic pronunciation flow
3. ⏭️ (Optional) Set up Google Cloud Speech API
4. ⏭️ (Optional) Implement ML theta estimation
5. ⏭️ Monitor pronunciation progress over time
6. ⏭️ Analyze common pronunciation errors
7. ⏭️ Add targeted pronunciation practice based on weak phonemes

---

**Status:** Pronunciation system ready for deployment!
**Last Updated:** 2026-01-06
