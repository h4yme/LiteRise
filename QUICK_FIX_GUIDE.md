# 🔧 QUICK FIX GUIDE - Placement Test Issues

## Issues You're Experiencing:

1. ❌ Reading passage text is not showing (blank teal card)
2. ❌ Category transition popups not appearing
3. ❌ Same pronunciation word repeating
4. ❌ Multiple choice questions in pronunciation category

## Root Cause:

Your app is running **OLD CODE** and database has **OLD DATA**.

---

## ✅ COMPLETE FIX - Follow in Order:

### FIX 1: Update Database (5 minutes)

#### Option A: Run Master Deployment Script (RECOMMENDED)

1. Open **SQL Server Management Studio**
2. Connect to your database
3. Click **File > Open > File**
4. Navigate to: `LiteRise/api/db/DEPLOY_PLACEMENT_28Q.sql`
5. Click **Execute** (or press F5)
6. **Wait for "DEPLOYMENT COMPLETE!" message**

Expected output:
```
✓ ReadingPassage column added
✓ All pronunciation items moved to "Oral Language"
✓ 12 reading comprehension questions inserted
DATABASE IS READY!
```

#### Option B: Check First, Then Deploy

1. Run `CHECK_DATABASE_STATUS.sql` first
2. See what's missing
3. Run `DEPLOY_PLACEMENT_28Q.sql`

---

### FIX 2: Completely Rebuild Android App (10 minutes)

**CRITICAL: You MUST do ALL these steps!**

#### In Android Studio:

```
Step 1: Clean Project
   - Menu: Build > Clean Project
   - Wait until "BUILD SUCCESSFUL" appears in Build tab

Step 2: Rebuild Project
   - Menu: Build > Rebuild Project
   - Wait until "BUILD SUCCESSFUL" appears
   - This may take 2-3 minutes

Step 3: Uninstall Old App
   - On your device: Settings > Apps > LiteRise > Uninstall
   - OR use ADB: adb uninstall com.example.literise

Step 4: Fresh Install
   - Menu: Run > Run 'app'
   - Wait for app to install and launch
```

**Why this is important:**
- Your current app has `questionsPerCategory = 6` (old code)
- New app will have `questionsPerCategory = 7` (new code)
- Old app doesn't know about ReadingPassage field

---

### FIX 3: Clear App Data (If needed)

If issues persist after rebuild:

```
On Device:
Settings > Apps > LiteRise > Storage > Clear Data
```

This clears:
- Cached session data
- Old test progress
- Local preferences

---

### FIX 4: Verify API is Running

Make sure your PHP API is accessible:

```bash
# Test API endpoint
curl http://your-server-ip/api/get_next_question.php
```

Should return authentication error (which is good - means it's running)

---

## ✅ Verification Checklist

After fixes, verify:

### Test 1: Database Ready
Run this query in SQL Server:

```sql
USE LiteRiseDB;
GO

-- Should return 12 rows
SELECT COUNT(*) as ReadingQuestions
FROM dbo.AssessmentItems
WHERE QuestionType = 'Reading'
  AND ReadingPassage IS NOT NULL
  AND ReadingPassage != '';

-- Should return 0
SELECT COUNT(*) as WrongCategory
FROM dbo.AssessmentItems
WHERE QuestionType = 'Pronunciation'
  AND Category != 'Oral Language';
```

**Expected:**
- ReadingQuestions: 12
- WrongCategory: 0

### Test 2: App Rebuilt
Check Android Studio Build tab:
```
BUILD SUCCESSFUL in 45s
```

### Test 3: App Running New Code
In PlacementTestActivity, check Logcat for:
```
PlacementTest: totalQuestions = 28
PlacementTest: questionsPerCategory = 7
```

If you see `25` or `6`, app didn't rebuild properly.

---

## 🎯 Expected Behavior After Fixes:

### Question 1-7: Pronunciation ONLY
- ✅ Question type: "🎤 Pronunciation Practice"
- ✅ Shows: "Say the word: CAT" (or DOG, SUN, etc.)
- ✅ Microphone button appears
- ✅ NO multiple choice options

### Question 8: Category Transition Dialog
- ✅ Popup appears with:
  - Icon: 🔤
  - Title: "Great Job!"
  - Name: "Category 2: Word Knowledge"
  - Message: "Now let's test your word knowledge..."
- ✅ Leo speaks the message
- ✅ Continue button to proceed

### Question 8-14: Word Knowledge
- ✅ Multiple choice questions
- ✅ NO pronunciation questions
- ✅ Questions about vocabulary, phonics, syllables

### Question 15: Category Transition Dialog
- ✅ Popup with Category 3 info
- ✅ Reading Comprehension introduction

### Question 15-21: Reading Comprehension
- ✅ **TEAL CARD** at top with story text (e.g., "Jenny's red balloon slipped from her hand...")
- ✅ Play/Stop buttons below
- ✅ Speed slider
- ✅ Question appears BELOW: "What will probably happen next?"
- ✅ Multiple choice answers

### Question 22-28: Language Structure
- ✅ Grammar questions
- ✅ Sentence construction
- ✅ Punctuation questions

### Question 28: Results
- ✅ Test complete
- ✅ Shows placement level
- ✅ Shows 4 category scores
- ✅ Celebration animation

---

## 🐛 Still Having Issues?

### Issue: Reading passage STILL not showing

**Check 1: Database has data**
```sql
SELECT TOP 1
    ItemID,
    QuestionText,
    LEFT(ReadingPassage, 100) as Passage
FROM dbo.AssessmentItems
WHERE QuestionType = 'Reading'
  AND ReadingPassage IS NOT NULL;
```

Should show actual passage text, NOT NULL.

**Check 2: API returns passage**
Test the API directly:

```bash
# Get auth token first (use your login credentials)
curl -X POST http://your-server/api/login.php \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test"}'

# Then test get_next_question (use token from above)
curl -X POST http://your-server/api/get_next_question.php \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "student_id":1,
    "session_id":12345,
    "current_theta":0.0,
    "assessment_type":"PreAssessment",
    "category":"Reading Comprehension"
  }'
```

Look for `"reading_passage":` in response.

**Check 3: App parses passage**
In Android Studio Logcat, filter for "PlacementTest" and look for:
```
PlacementTest: ReadingPassage = The dog is brown...
```

If NULL or empty, app isn't getting it from API.

---

### Issue: Still seeing old question count (25 instead of 28)

This means app wasn't rebuilt properly.

**Force rebuild:**
```
1. Close Android Studio
2. Delete build folders:
   - rm -rf app/build
   - rm -rf app/.gradle
3. Reopen Android Studio
4. File > Invalidate Caches > Invalidate and Restart
5. Build > Rebuild Project
6. Uninstall app from device
7. Run > Run 'app'
```

---

### Issue: Pronunciation in wrong categories

**Database fix:**
```sql
-- Force update ALL pronunciation to Oral Language
UPDATE dbo.AssessmentItems
SET Category = 'Oral Language'
WHERE QuestionType = 'Pronunciation';

-- Verify
SELECT Category, COUNT(*)
FROM dbo.AssessmentItems
WHERE QuestionType = 'Pronunciation'
GROUP BY Category;
```

Should show ONLY "Oral Language".

---

### Issue: Same word repeating

This happens if SessionID is not being passed correctly.

**Check in Logcat:**
```
PlacementTest: currentSessionId = 1736149200
```

Should be a big number (timestamp), NOT 1 or 0.

If it's 1, the session isn't being created properly.

---

## 📞 Debug Checklist

Before reporting issues, collect this info:

### Database Info:
```sql
-- Run these and share results
SELECT COUNT(*) FROM AssessmentItems WHERE QuestionType = 'Pronunciation' AND Category = 'Oral Language';
SELECT COUNT(*) FROM AssessmentItems WHERE QuestionType = 'Reading' AND ReadingPassage IS NOT NULL;
SELECT TOP 1 ReadingPassage FROM AssessmentItems WHERE QuestionType = 'Reading';
```

### Android Studio Info:
- Build output: Copy the "BUILD SUCCESSFUL" message
- Logcat errors: Filter for "Error" or "Exception"
- App version: Check Build tab for timestamp

### Device Info:
- Android version
- Device model
- Available storage

---

## ✅ Success Criteria

Your app is working correctly when:

1. ✅ Database CHECK script shows "DATABASE IS READY!"
2. ✅ Android Studio shows "BUILD SUCCESSFUL"
3. ✅ Question 1 shows pronunciation
4. ✅ Question 15 shows reading passage text in teal card
5. ✅ Progress bar shows 28 total questions
6. ✅ Category transitions appear at Q8, Q15, Q22
7. ✅ No duplicate questions
8. ✅ Test completes after Q28

---

## 🎯 Next Steps After Everything Works:

1. Test complete flow (Q1-28)
2. Verify IRT scoring
3. Check placement level accuracy
4. Test with multiple students
5. Monitor for any errors

**Good luck! 🚀**

If you've followed ALL steps and still have issues, share:
- Database CHECK output
- Android Studio build log
- Logcat errors
- Screenshots of specific issues
