# Fixing ActivityNotFoundException Error

## ⚠️ Current Error
```
ActivityNotFoundException: Unable to find explicit activity class {com.example.literise/com.example.literise.activities.PreAssessmentActivity}
```

## 🔧 Solution: Rebuild the App

The error occurs because the app needs to be rebuilt after code changes. Follow these steps:

### Step 1: Clean the Project
In Android Studio:
```
Build → Clean Project
```
Wait for it to complete.

### Step 2: Rebuild the Project
```
Build → Rebuild Project
```
This will recompile all code and include PreAssessmentActivity in the APK.

### Step 3: Sync Gradle
```
File → Sync Project with Gradle Files
```

### Step 4: Reinstall the App
```
Run → Run 'app'
```
Or uninstall the old version from your device/emulator first, then reinstall.

---

## 🎯 Switching Between Fixed and Adaptive Modes

You now have **TWO assessment modes** available:

### Option 1: Fixed Mode (Current Default)
- **Class**: `PreAssessmentActivity`
- **Questions**: 20 fixed questions (same for all students)
- **Time**: ~25 minutes
- **Use when**: You want consistent assessment across all students

### Option 2: Adaptive Mode (Recommended)
- **Class**: `AdaptivePreAssessmentActivity`
- **Questions**: 10-15 adaptive questions (personalized per student)
- **Time**: ~15 minutes
- **Use when**: You want faster, more accurate assessments

---

## 📝 How to Switch to Adaptive Mode

### Method 1: Update SplashActivity (or wherever assessment starts)

Find where PreAssessmentActivity is launched. Based on your error, it's in `SplashActivity.java` line 24.

**Current code:**
```java
Intent intent = new Intent(this, PreAssessmentActivity.class);
startActivity(intent);
```

**Change to:**
```java
Intent intent = new Intent(this, AdaptivePreAssessmentActivity.class);
startActivity(intent);
```

### Method 2: Create a Settings Toggle

Let teachers/admins choose which mode to use:

```java
public class AssessmentLauncher {

    public enum Mode {
        FIXED,      // 20 questions, traditional
        ADAPTIVE    // 10-15 questions, IRT-based
    }

    public static void startAssessment(Context context, Mode mode) {
        Intent intent;

        if (mode == Mode.ADAPTIVE) {
            intent = new Intent(context, AdaptivePreAssessmentActivity.class);
        } else {
            intent = new Intent(context, PreAssessmentActivity.class);
        }

        context.startActivity(intent);
    }
}

// Usage:
AssessmentLauncher.startAssessment(this, AssessmentLauncher.Mode.ADAPTIVE);
```

---

## 📊 Mode Comparison

| Feature | Fixed Mode | Adaptive Mode |
|---------|-----------|---------------|
| **Class** | `PreAssessmentActivity` | `AdaptivePreAssessmentActivity` |
| **API** | `get_preassessment_items.php` | `get_next_item.php` + `submit_single_response.php` |
| **Questions** | 20 (all at once) | 10-15 (one at a time) |
| **Difficulty** | Mixed | Adapts to student |
| **Feedback** | At end only | After each question |
| **Time** | ~25 min | ~15 min |
| **Accuracy** | Medium | High |

---

## 🚀 Quick Fix Steps

1. **Clean & Rebuild** in Android Studio
2. **Uninstall old app** from device
3. **Run app** again
4. Error should be fixed!

5. **(Optional)** Switch to adaptive mode by changing the Intent in your code

---

## 💡 Recommended Configuration

For best results:

```java
// In your navigation/splash activity
Intent intent = new Intent(this, AdaptivePreAssessmentActivity.class); // Use adaptive!
startActivity(intent);
```

Adaptive mode provides:
- ✅ Faster completion (40% time savings)
- ✅ Better accuracy (higher precision theta estimates)
- ✅ Better student experience (questions are "just right")
- ✅ Lower cheating risk (each student gets different questions)

---

## 🔍 Verifying It Works

After rebuilding, you should see:

**Fixed Mode:**
- "Question 1 of 20"
- All 20 questions shown
- Results at end

**Adaptive Mode:**
- "Question 1 • ~15 remaining"
- Questions appear one at a time
- Feedback after each answer
- Stops early when precision achieved (usually 10-15 questions)

---

## 📝 Files Modified

✅ **AndroidManifest.xml** - Both activities now registered:
- `PreAssessmentActivity` (line 47)
- `AdaptivePreAssessmentActivity` (line 53)

✅ All necessary files committed and pushed.

---

*Need help? Check `/docs/ADAPTIVE_TESTING_GUIDE.md` for complete documentation.*
