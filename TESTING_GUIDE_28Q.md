# 28-Question Balanced Placement Test - Testing Guide

## Overview
This guide helps you test the newly implemented 28-question placement test with proper category separation and IRT scoring.

## Pre-Testing Setup

### 1. Deploy Database Changes
Execute the deployment script in SQL Server Management Studio:

```sql
-- Connect to your database and run:
USE LiteRiseDB;
GO
-- Execute the deployment script
:r /path/to/api/db/DEPLOY_PLACEMENT_28Q.sql
```

**Expected Output:**
- ✓ ReadingPassage column added
- ✓ All pronunciation items moved to "Oral Language" category
- ✓ 12 reading comprehension questions inserted
- ✓ Category distribution verified

### 2. Verify Database Changes
Run this query to verify the setup:

```sql
USE LiteRiseDB;
GO

-- Verify category distribution
SELECT
    Category,
    QuestionType,
    COUNT(*) AS ItemCount
FROM dbo.AssessmentItems
WHERE IsActive = 1
GROUP BY Category, QuestionType
ORDER BY
    CASE Category
        WHEN 'Oral Language' THEN 1
        WHEN 'Word Knowledge' THEN 2
        WHEN 'Reading Comprehension' THEN 3
        WHEN 'Language Structure' THEN 4
    END,
    QuestionType;
```

**Expected Results:**
- Oral Language - Pronunciation: ~30 questions
- Word Knowledge - MultipleChoice: ~10 questions
- Reading Comprehension - Reading: ~12 questions
- Language Structure - MultipleChoice: ~10 questions

### 3. Rebuild Android App
In Android Studio:
1. **Build > Clean Project**
2. **Build > Rebuild Project**
3. Uninstall existing app from device/emulator
4. **Run > Run 'app'**

---

## Test Scenarios

### Test 1: Category Separation ✅
**Objective:** Verify pronunciation ONLY appears in Category 1

**Steps:**
1. Start a new placement test
2. Complete questions 1-7 (Category 1: Oral Language)
3. **Verify:** All questions should be pronunciation ("Say the word: ...")
4. Continue to questions 8-14 (Category 2: Word Knowledge)
5. **Verify:** NO pronunciation questions should appear
6. Continue to questions 15-21 (Category 3: Reading Comprehension)
7. **Verify:** NO pronunciation questions should appear
8. Complete questions 22-28 (Category 4: Language Structure)
9. **Verify:** NO pronunciation questions should appear

**Expected Behavior:**
- ✅ Questions 1-7: Pronunciation ONLY
- ✅ Questions 8-28: NO pronunciation questions

---

### Test 2: Reading Comprehension ✅
**Objective:** Verify reading passages display correctly with karaoke feature

**Steps:**
1. Start a new placement test
2. Skip through to questions 15-21 (Category 3)
3. For each reading question:
   - **Verify:** A reading passage appears above the question
   - **Verify:** Karaoke reading feature works (text highlights as it's read aloud)
   - **Verify:** Comprehension question appears after reading
   - **Verify:** Multiple choice options are shown

**Expected Behavior:**
- ✅ Reading passage visible and separate from question
- ✅ Play/Stop buttons work
- ✅ Text highlights word by word during reading
- ✅ Comprehension question displays after passage
- ✅ Answer options A/B/C/D work correctly

---

### Test 3: 28-Question Flow ✅
**Objective:** Verify exactly 28 questions with proper category transitions

**Steps:**
1. Start a new placement test
2. Track question numbers and category transitions
3. Complete all questions

**Expected Category Transitions:**
- Q1-7: Category 1 (Oral Language) - Pronunciation
- Q8: Category transition dialog → Category 2 (Word Knowledge)
- Q8-14: Category 2 - Vocabulary/Phonics
- Q15: Category transition dialog → Category 3 (Reading Comprehension)
- Q15-21: Category 3 - Reading passages with questions
- Q22: Category transition dialog → Category 4 (Language Structure)
- Q22-28: Category 4 - Grammar
- Q29: Test complete → Results screen

**Expected Behavior:**
- ✅ Exactly 28 questions total
- ✅ 7 questions per category
- ✅ Category transition dialogs appear at Q8, Q15, Q22
- ✅ Results screen appears after Q28

---

### Test 4: IRT Scoring Across Categories ✅
**Objective:** Verify IRT theta updates correctly across all categories

**Steps:**
1. Start a new placement test
2. **Strategy for testing:**
   - Category 1 (Q1-7): Answer all pronunciation questions correctly
   - Category 2 (Q8-14): Answer all vocabulary questions incorrectly
   - Category 3 (Q15-21): Mix correct and incorrect
   - Category 4 (Q22-28): Answer all correctly
3. Complete the test and check results

**Expected Behavior:**
- ✅ Final theta estimate considers ALL 28 questions
- ✅ Category scores show different percentages:
  - Category 1: ~100%
  - Category 2: ~0%
  - Category 3: ~50%
  - Category 4: ~100%
- ✅ Overall placement level is calculated from combined theta
- ✅ Results screen shows category breakdown

**Check Results Screen For:**
- Total questions answered: 28
- Total correct: (varies based on answers)
- Category 1 score: ~100%
- Category 2 score: ~0%
- Category 3 score: ~50%
- Category 4 score: ~100%
- Final placement level: (should be reasonable based on overall performance)

---

### Test 5: Pronunciation Assessment Integration ✅
**Objective:** Verify Google Cloud Speech API integration works in Category 1

**Steps:**
1. Start a new placement test
2. For each pronunciation question in Q1-7:
   - Tap microphone button
   - Speak the word clearly
   - Wait for evaluation
   - Check feedback

**Expected Behavior:**
- ✅ Microphone permission requested (first time)
- ✅ Recording indicator shows during recording
- ✅ Audio is sent to backend for evaluation
- ✅ Pronunciation score returned (0-100%)
- ✅ Feedback shows: "Excellent", "Good", or "Try again"
- ✅ Continue button enables after evaluation
- ✅ Answer is automatically submitted (no duplicate submission)
- ✅ Theta updates based on pronunciation accuracy

---

### Test 6: Adaptive Question Selection ✅
**Objective:** Verify IRT adaptive algorithm selects appropriate difficulty

**Steps:**
1. Start a new placement test
2. **Scenario A - High performer:**
   - Answer first 3 questions correctly
   - **Observe:** Difficulty should increase for next questions
3. **Scenario B - Low performer:**
   - Answer first 3 questions incorrectly
   - **Observe:** Difficulty should decrease for next questions

**Expected Behavior:**
- ✅ Questions get harder after correct answers
- ✅ Questions get easier after incorrect answers
- ✅ Difficulty adapts within each category
- ✅ No duplicate questions shown

---

## Verification Checklist

### Database ✅
- [ ] ReadingPassage column exists in AssessmentItems table
- [ ] All pronunciation items have Category = 'Oral Language'
- [ ] At least 12 reading questions with ReadingPassage populated
- [ ] All 4 categories have sufficient questions

### Android App ✅
- [ ] PlacementTestActivity shows 28 total questions
- [ ] 7 questions per category (verified by progress bar)
- [ ] Category transition dialogs appear at correct points
- [ ] AdaptiveQuestionResponse properly parses reading_passage field

### API Endpoints ✅
- [ ] get_next_question.php returns reading_passage field
- [ ] get_next_question.php filters by category correctly
- [ ] submit_answer.php updates theta after each answer
- [ ] evaluate_pronunciation.php creates StudentResponse correctly

### User Experience ✅
- [ ] Pronunciation questions only in Category 1
- [ ] Reading passages display properly
- [ ] Karaoke reading works smoothly
- [ ] Progress bar shows accurate progress (1-28)
- [ ] Results screen shows all category scores
- [ ] Final placement level is reasonable

---

## Troubleshooting

### Issue: Pronunciation appears in wrong categories
**Solution:** Run `fix_pronunciation_categories.sql`:
```sql
:r /path/to/api/db/fix_pronunciation_categories.sql
```

### Issue: Reading passages not showing
**Solution:**
1. Verify ReadingPassage column exists: `sp_help AssessmentItems`
2. Run migration: `:r /path/to/api/db/add_reading_passage_field.sql`
3. Insert reading questions: `:r /path/to/api/db/sample_reading_comprehension.sql`

### Issue: Only 25 questions appear instead of 28
**Solution:**
1. Rebuild Android app completely
2. Verify PlacementTestActivity has `totalQuestions = 28`
3. Check questionsPerCategory = 7

### Issue: Category scores not calculating
**Solution:**
1. Check IRTEngine.getCategoryScores() implementation
2. Verify all questions have correct category (1-4)
3. Check PlacementResultActivity displays category scores

---

## Success Criteria

✅ **All tests must pass:**
1. Pronunciation ONLY in Category 1 (Q1-7)
2. Reading comprehension with passages works (Q15-21)
3. Exactly 28 questions total (7 per category)
4. IRT theta updates across all categories
5. Category transition dialogs appear correctly
6. Results show accurate category breakdown
7. Final placement level is calculated correctly

---

## Performance Metrics to Monitor

1. **Completion Rate:**
   - Target: >90% of students complete all 28 questions

2. **Average Time:**
   - Pronunciation questions: 15-30 seconds each
   - Multiple choice: 10-20 seconds each
   - Reading questions: 60-90 seconds each
   - Total test time: 8-12 minutes

3. **IRT Theta Range:**
   - Should span from -2.0 to +2.0 for most students
   - Placement levels should distribute across all 4 levels

4. **Category Performance:**
   - Each category should show distinct scores
   - Correlations between categories should be positive but <0.9

---

## Next Steps After Testing

1. **If all tests pass:**
   - Monitor first 20 students through the test
   - Collect feedback on test length and difficulty
   - Analyze placement level distribution

2. **If issues found:**
   - Document specific error messages
   - Check logs in Android Studio Logcat
   - Review SQL Server error logs
   - Verify API responses in network inspector

3. **Optimization opportunities:**
   - Fine-tune IRT parameters based on actual student data
   - Adjust difficulty distributions if needed
   - Add more questions to categories with <20 items
   - Implement ML-based theta estimation (future enhancement)

---

## Contact & Support

For issues or questions:
1. Check Android Studio Logcat for errors
2. Review SQL Server logs for database errors
3. Test API endpoints using Postman/Insomnia
4. Verify network connectivity between app and API

**Good luck with testing! 🚀**
