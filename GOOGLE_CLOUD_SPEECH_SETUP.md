# Google Cloud Speech-to-Text Setup Guide for LiteRise

This guide will help you set up real speech recognition for pronunciation assessment.

## 🎯 Overview

LiteRise uses **Google Cloud Speech-to-Text API** for accurate pronunciation assessment. This provides:
- Real-time speech recognition
- Confidence scores for pronunciation accuracy
- Support for multiple languages
- Professional-grade audio analysis

---

## 📋 Prerequisites

- Google account
- Credit card (Google Cloud requires billing, but offers $300 free credit)
- PHP 7.4+ with Composer installed
- XAMPP running

---

## 🚀 Step 1: Google Cloud Console Setup

### 1.1 Create Google Cloud Project

1. Go to: https://console.cloud.google.com/
2. Sign in with your Google account
3. Click **Select a project** → **New Project**
4. Project name: `LiteRise` (or your preferred name)
5. Click **Create**

### 1.2 Enable Billing

1. Go to: https://console.cloud.google.com/billing
2. Link a billing account (required for API access)
3. **Note:** You get $300 free credit + 60 minutes/month free speech recognition

### 1.3 Enable Speech-to-Text API

1. Go to: https://console.cloud.google.com/apis/library/speech.googleapis.com
2. Make sure your project is selected at the top
3. Click **Enable**
4. Wait for activation (takes ~1 minute)

### 1.4 Create Service Account

1. Go to: https://console.cloud.google.com/iam-admin/serviceaccounts
2. Click **Create Service Account**
3. Service account details:
   - **Name:** `literise-speech-api`
   - **ID:** (auto-generated)
   - Click **Create and Continue**
4. Grant roles:
   - Select **Cloud Speech-to-Text User** role
   - Click **Continue**
5. Click **Done**

### 1.5 Generate Credentials JSON

1. Click on the service account you just created
2. Go to **Keys** tab
3. Click **Add Key** → **Create new key**
4. Key type: **JSON**
5. Click **Create**
6. **Save the downloaded file** - you'll need it next!

---

## 🔧 Step 2: Server Setup

### 2.1 Install Composer (if not already installed)

Download and install Composer from: https://getcomposer.org/download/

**For Windows:**
1. Download and run `Composer-Setup.exe`
2. Follow installation wizard
3. Restart Command Prompt

**Verify installation:**
```cmd
composer --version
```

### 2.2 Install Google Cloud Speech Library

Open Command Prompt and navigate to your API folder:

```cmd
cd C:\xampp\htdocs\api
composer install
```

This will download the Google Cloud Speech library (~5-10 minutes).

### 2.3 Place Credentials File

1. Locate the JSON file you downloaded from Google Cloud (e.g., `literise-speech-api-xxxxx.json`)
2. **Rename it to:** `google-cloud-credentials.json`
3. **Copy it to:** `C:\xampp\htdocs\api\google-cloud-credentials.json`

**⚠️ Security Note:** This file contains sensitive credentials. Never commit it to Git or share it publicly!

### 2.4 Update evaluate_pronunciation.php

The file has already been updated to use Google Cloud Speech API. Just copy it to XAMPP:

```
[Your Project]\api\evaluate_pronunciation.php
→ C:\xampp\htdocs\api\evaluate_pronunciation.php
```

---

## ✅ Step 3: Testing

### 3.1 Test API Connection

Create a test file: `C:\xampp\htdocs\api\test_speech.php`

```php
<?php
require_once __DIR__ . '/vendor/autoload.php';

use Google\Cloud\Speech\V1\SpeechClient;

try {
    $speech = new SpeechClient([
        'credentials' => __DIR__ . '/google-cloud-credentials.json'
    ]);

    echo "✅ Google Cloud Speech API connected successfully!\n";
    echo "Your pronunciation assessment is ready to use.\n";

    $speech->close();
} catch (Exception $e) {
    echo "❌ Error: " . $e->getMessage() . "\n";
}
?>
```

**Run the test:**
```cmd
cd C:\xampp\htdocs\api
php test_speech.php
```

**Expected output:**
```
✅ Google Cloud Speech API connected successfully!
Your pronunciation assessment is ready to use.
```

### 3.2 Test in LiteRise App

1. Rebuild Android app
2. Start placement test
3. Speak a pronunciation word
4. Check result - should show actual pronunciation accuracy!

---

## 📊 Monitoring Usage & Costs

### Check Your Usage

1. Go to: https://console.cloud.google.com/speech
2. View usage dashboard
3. Monitor minutes used

### Pricing

**Free Tier (per month):**
- 0-60 minutes: FREE
- 61+ minutes: $0.006/15 seconds (~$1.44/hour)

**For LiteRise:**
- Average pronunciation: 5 seconds
- 1,000 pronunciations ≈ 83 minutes ≈ $1.38
- With free tier: First 720 pronunciations/month are FREE

---

## 🔍 Troubleshooting

### Error: "credentials file not found"

**Solution:** Make sure `google-cloud-credentials.json` is in `C:\xampp\htdocs\api\` folder

### Error: "API not enabled"

**Solution:** Go to https://console.cloud.google.com/apis/library/speech.googleapis.com and click Enable

### Error: "Permission denied"

**Solution:**
1. Go to IAM & Admin → Service Accounts
2. Verify your service account has "Cloud Speech-to-Text User" role
3. Regenerate credentials if needed

### Error: "INVALID_ARGUMENT: Audio encoding"

**Solution:** This is already fixed - we're using AudioEncoding::AMR for 3GP files

### No speech detected

**Possible causes:**
- Audio too quiet - speak louder/closer to mic
- Background noise - test in quiet environment
- Audio corruption - try recording again

### Check PHP Error Log

Open: `C:\xampp\apache\logs\error.log`

Look for lines containing:
- `ERROR: Speech recognition failed`
- `INFO: Sending audio to Google Cloud`
- `INFO: Speech recognition result`

---

## 🎓 How It Works

### Audio Flow:

1. **Android App:** Records 5 seconds of audio (3GP/AMR format)
2. **Upload:** Sends audio file to `evaluate_pronunciation.php`
3. **Google Cloud:** PHP sends audio to Google Speech API
4. **Recognition:** API returns recognized text + confidence score
5. **Scoring:** PHP calculates pronunciation accuracy by comparing:
   - Recognized word vs. Target word (Levenshtein distance)
   - API confidence score
6. **Response:** Returns score + feedback to app

### Accuracy Calculation:

```
pronunciation_score = (text_match * 0.7) + (confidence * 0.3)
```

Where:
- `text_match`: How closely recognized text matches target (0.0-1.0)
- `confidence`: Google's confidence in recognition (0.0-1.0)

---

## 🔐 Security Best Practices

1. **Never commit credentials file to Git:**
   ```
   # Add to .gitignore:
   google-cloud-credentials.json
   ```

2. **Restrict API Key (Optional but Recommended):**
   - Go to: https://console.cloud.google.com/apis/credentials
   - Restrict key to specific IP addresses or HTTP referrers

3. **Monitor usage regularly** to detect unauthorized access

4. **Rotate credentials periodically** (every 90 days recommended)

---

## 📞 Support

**Google Cloud Documentation:**
https://cloud.google.com/speech-to-text/docs

**LiteRise Issues:**
If you encounter problems, check the PHP error log first, then contact support with:
- Error message from log
- Steps to reproduce
- Audio file size/format

---

## ✨ You're All Set!

Your pronunciation assessment system now uses **real AI-powered speech recognition**! Students will get accurate feedback on their pronunciation. 🎤✨
